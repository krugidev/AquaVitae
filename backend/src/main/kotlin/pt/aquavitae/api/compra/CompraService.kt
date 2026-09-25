package pt.aquavitae.api.compra

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.cave.CaveBebidaRepository
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.compra.dto.CliquePendenteDto
import pt.aquavitae.api.compra.dto.LinkCompraDto
import pt.aquavitae.api.utilizador.Utilizador
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant

@Service
class CompraService(
    private val linkCompraRepository: BebidaLinkCompraRepository,
    private val cliqueCompraRepository: CliqueCompraRepository,
    private val caveBebidaRepository: CaveBebidaRepository,
    private val clock: Clock,
    @Value("\${aquavitae.compra.pergunta-apos-minutos:2}") private val perguntaAposMinutos: Long,
    @Value("\${aquavitae.compra.pergunta-ate-horas:72}") private val perguntaAteHoras: Long,
) {

    // Secção "Onde comprar": disponíveis primeiro (mais barato primeiro), indisponíveis no fim.
    fun listByBebida(bebidaId: Long): List<LinkCompraDto> =
        linkCompraRepository.findByBebida_IdAndRetalhista_IsAtivoTrue(bebidaId)
            .sortedWith(
                compareByDescending<BebidaLinkCompra> { it.isAtivo }
                    .thenBy(nullsLast()) { it.precoAtual },
            )
            .map { LinkCompraDto.from(it) }

    // Usado pelo BebidaSummaryAssembler para o "precoDesde" do catálogo/homepage —
    // uma query só para todas as bebidas da página, em vez de uma por bebida.
    // Só conta links disponíveis: uma bebida sem nenhum fica sem preço/botão de compra.
    fun cheapestByBebidaIds(bebidaIds: Collection<Long>): Map<Long, LinkCompraDto> {
        if (bebidaIds.isEmpty()) return emptyMap()
        return linkCompraRepository.findByBebida_IdInAndIsAtivoTrueAndRetalhista_IsAtivoTrue(bebidaIds)
            .filter { it.precoAtual != null }
            .groupBy { it.bebida?.id }
            .mapNotNull { (bebidaId, links) ->
                bebidaId?.let { id -> id to LinkCompraDto.from(links.minBy { it.precoAtual!! }) }
            }
            .toMap()
    }

    @Transactional
    fun registarClique(linkId: Long, utilizador: Utilizador) {
        val link = linkCompraRepository.findById(linkId)
            .orElseThrow { ResourceNotFoundException("Link de compra $linkId não encontrado") }
        if (!link.isAtivo) {
            throw ConflictException("Este link de compra já não está disponível")
        }
        cliqueCompraRepository.save(
            CliqueCompra(utilizador = utilizador, linkCompra = link, dataClique = Instant.now()),
        )
    }

    // "Compraste?": os cliques deste utilizador por perguntar, dentro da janela (ver CliquePergunta), o mais recente de
    // cada bebida. Fica de fora a bebida que o utilizador já adicionou a uma cave desde o dia do clique (não vale a pena
    // perguntar o que ele já disse por outro caminho).
    @Transactional(readOnly = true)
    fun pendentes(utilizador: Utilizador): List<CliquePendenteDto> {
        val (desde, ate) = CliquePergunta.janela(clock.instant(), perguntaAposMinutos, perguntaAteHoras)
        return cliqueCompraRepository.findPendentes(utilizador.id, desde, ate)
            .distinctBy { it.linkCompra?.bebida?.id }
            .filterNot { clique ->
                val bebidaId = clique.linkCompra?.bebida?.id ?: return@filterNot true
                val dia = clique.dataClique?.atZone(clock.zone)?.toLocalDate() ?: return@filterNot true
                caveBebidaRepository.existsByCave_Utilizador_IdAndBebida_IdAndDataAquisicaoGreaterThanEqual(utilizador.id, bebidaId, dia)
            }
            .mapNotNull { clique ->
                val link = clique.linkCompra ?: return@mapNotNull null
                val bebida = link.bebida ?: return@mapNotNull null
                CliquePendenteDto(
                    id = clique.id,
                    bebidaId = bebida.id,
                    bebidaNome = bebida.nome,
                    bebidaImagePath = bebida.pathImage,
                    retalhistaNome = link.retalhista?.nome,
                    preco = link.precoAtual,
                    dataClique = clique.dataClique ?: Instant.EPOCH,
                )
            }
    }

    // Responder a um clique responde a todos os que o utilizador ainda tinha por perguntar na mesma bebida (senão a
    // pergunta voltava por cada clique antigo).
    @Transactional
    fun responder(cliqueId: Long, resposta: RespostaClique, utilizador: Utilizador) {
        val clique = cliqueCompraRepository.findByIdAndUtilizador_Id(cliqueId, utilizador.id)
            ?: throw ResourceNotFoundException("Clique $cliqueId não encontrado")
        val bebidaId = clique.linkCompra?.bebida?.id
        val mesmaBebida = if (bebidaId != null) cliqueCompraRepository.findPorPerguntarDaBebida(utilizador.id, bebidaId) else emptyList()
        (mesmaBebida + clique).distinctBy { it.id }.forEach {
            it.isPerguntado = true
            it.resposta = resposta.name
        }
    }

    private fun nullsLast(): Comparator<BigDecimal?> = Comparator.nullsLast(Comparator.naturalOrder())
}
