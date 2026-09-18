package pt.aquavitae.api.compra.verificacao

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import pt.aquavitae.api.compra.BebidaLinkCompra
import pt.aquavitae.api.compra.BebidaLinkCompraRepository
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.Disponivel
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.Inconclusivo
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.LinkInvalido
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.SemStock
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

data class ResumoVerificacao(
    val inicio: Instant,
    val fim: Instant,
    val total: Int,
    // Links sem url_verificacao de um retalhista com rede de afiliados: não se pedem (evita cliques).
    val semUrlDeVerificacao: Int,
    val disponiveis: Int,
    val disponiveisSemStockConfirmado: Int,
    val semStock: Int,
    val linksInvalidos: Int,
    val inconclusivos: Int,
    val desativados: Int,
    val reativados: Int,
    // true = demasiadas falhas de uma vez (rede em baixo? bloqueio?): nenhum link foi desativado.
    val execucaoSuspeita: Boolean,
)

data class EstadoVerificacao(val emCurso: Boolean, val ultimaExecucao: ResumoVerificacao?)

private const val MIN_INVALIDOS_SUSPEITOS = 5
private const val MOTIVO_SEM_STOCK = "Sem stock"
private const val MOTIVO_LINK_INVALIDO = "Página indisponível"

// Verifica diariamente (ver LinkVerificacaoScheduler) se os links de compra continuam utilizáveis.
// Um link que fica sem stock ou cuja página desaparece passa a is_ativo = false (esconde o botão de
// compra na app), mas a linha fica como histórico e volta a ativo sozinha se a página voltar.
@Service
class LinkVerificacaoService(
    private val linkRepository: BebidaLinkCompraRepository,
    private val verificador: LinkVerificador,
    @Value("\${aquavitae.links.verificacao.falhas-para-desativar:2}") private val falhasParaDesativar: Int,
) {
    private val logger = LoggerFactory.getLogger(LinkVerificacaoService::class.java)
    private val emCurso = AtomicBoolean(false)
    private val ultimaExecucao = AtomicReference<ResumoVerificacao?>(null)
    private val executor = Executors.newSingleThreadExecutor { tarefa ->
        Thread(tarefa, "link-verificacao").apply { isDaemon = true }
    }

    fun estado() = EstadoVerificacao(emCurso.get(), ultimaExecucao.get())

    // Usado pelo endpoint de admin: responde logo e corre em segundo plano (pode demorar minutos).
    // false = já havia uma execução em curso.
    fun iniciarEmBackground(): Boolean {
        if (!emCurso.compareAndSet(false, true)) return false
        executor.execute { correr() }
        return true
    }

    // Usado pelo agendamento diário.
    fun executarAgendado() {
        if (!emCurso.compareAndSet(false, true)) {
            logger.warn("Verificação de links já em curso, a saltar esta execução")
            return
        }
        correr()
    }

    @PreDestroy
    fun parar() {
        executor.shutdownNow()
    }

    private fun correr() {
        try {
            ultimaExecucao.set(verificarTodos())
        } catch (e: Exception) {
            logger.error("Falha na verificação de links de compra", e)
        } finally {
            emCurso.set(false)
        }
    }

    fun verificarTodos(): ResumoVerificacao {
        val inicio = Instant.now()
        val links = linkRepository.findAllParaVerificacao()

        var semUrl = 0
        val resultados = LinkedHashMap<BebidaLinkCompra, ResultadoVerificacao>()
        for (link in links) {
            val url = urlAlvo(link)
            if (url == null) {
                semUrl++
                continue
            }
            val resultado = verificador.verificar(url)
            if (resultado !is Disponivel) logger.info("Link {} ({}): {}", link.id, url, resultado)
            resultados[link] = resultado
        }

        val invalidos = resultados.values.count { it is LinkInvalido }
        val conclusivos = resultados.values.count { it !is Inconclusivo }
        val suspeita = invalidos >= MIN_INVALIDOS_SUSPEITOS && invalidos * 2 > conclusivos
        if (suspeita) {
            logger.warn("Verificação suspeita: {} de {} respostas conclusivas são links inválidos — nada foi desativado", invalidos, conclusivos)
        }

        var disponiveis = 0
        var semConfirmacao = 0
        var semStock = 0
        var inconclusivos = 0
        var desativados = 0
        var reativados = 0
        val agora = Instant.now()

        for ((link, resultado) in resultados) {
            when (resultado) {
                is Disponivel -> {
                    disponiveis++
                    if (!resultado.stockConfirmado) semConfirmacao++
                    if (!link.isAtivo) reativados++
                    guardar(link, ativo = true, falhas = 0, indisponivelDesde = null, motivo = null, agora)
                }
                SemStock -> {
                    semStock++
                    if (link.isAtivo) desativados++
                    guardar(link, ativo = false, falhas = 0, indisponivelDesde = link.dataIndisponivel ?: agora, MOTIVO_SEM_STOCK, agora)
                }
                is LinkInvalido -> {
                    if (suspeita) {
                        inconclusivos++
                    } else {
                        val falhas = link.falhasSeguidas + 1
                        if (falhas >= falhasParaDesativar) {
                            if (link.isAtivo) desativados++
                            guardar(link, ativo = false, falhas = falhas, indisponivelDesde = link.dataIndisponivel ?: agora, MOTIVO_LINK_INVALIDO, agora)
                        } else {
                            guardar(link, ativo = link.isAtivo, falhas = falhas, indisponivelDesde = link.dataIndisponivel, link.motivo, agora)
                        }
                    }
                }
                is Inconclusivo -> inconclusivos++
            }
        }

        val resumo = ResumoVerificacao(
            inicio = inicio,
            fim = Instant.now(),
            total = links.size,
            semUrlDeVerificacao = semUrl,
            disponiveis = disponiveis,
            disponiveisSemStockConfirmado = semConfirmacao,
            semStock = semStock,
            linksInvalidos = invalidos,
            inconclusivos = inconclusivos,
            desativados = desativados,
            reativados = reativados,
            execucaoSuspeita = suspeita,
        )
        logger.info("Verificação de links concluída: {}", resumo)
        return resumo
    }

    // Um link de retalhista com rede de afiliados tem tracking no url: pedi-lo todos os dias
    // contaria como cliques. Por isso só se usa o url_verificacao (página do produto sem tracking);
    // sem ele, o link não é verificado. Retalhistas sem rede (link direto) usam o próprio url.
    private fun urlAlvo(link: BebidaLinkCompra): String? =
        link.urlVerificacao?.takeIf { it.isNotBlank() }
            ?: link.url?.takeIf { it.isNotBlank() && link.retalhista?.redeAfiliados.isNullOrBlank() }

    private fun guardar(
        link: BebidaLinkCompra,
        ativo: Boolean,
        falhas: Int,
        indisponivelDesde: Instant?,
        motivo: String?,
        verificadoEm: Instant,
    ) {
        linkRepository.atualizarEstado(link.id, ativo, falhas, indisponivelDesde, motivo, verificadoEm)
    }
}
