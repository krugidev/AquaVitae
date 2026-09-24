package pt.aquavitae.api.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.bebida.PesquisaTexto
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.lookup.PAIS_EM_DESTAQUE
import pt.aquavitae.api.lookup.ProdutorPaisRepository
import pt.aquavitae.api.lookup.RegiaoRepository
import pt.aquavitae.api.lookup.ordenadoPorNome
import pt.aquavitae.api.lookup.ordenadoPorNomeComDestaques
import pt.aquavitae.api.produtor.Produtor
import pt.aquavitae.api.produtor.ProdutorRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/** Uma opção de uma lista do formulário (país, região). */
data class OpcaoLookup(val id: Long, val nome: String)

/** Um produtor já guardado, para a página de edição: o formulário preenchido e o que se mostra ao lado. */
data class ProdutorParaEditar(val id: Long, val nome: String, val formulario: ProdutorFormulario, val totalBebidas: Long)

sealed interface ResultadoGravacao {
    data class Guardado(val id: Long) : ResultadoGravacao

    /** Campo do formulário → mensagem. */
    data class Invalido(val erros: Map<String, String>) : ResultadoGravacao
}

/**
 * Criar e editar produtores no painel (fatia 2 — **a primeira escrita do painel**: até aqui o conteúdo do catálogo só se mudava
 * por SQL). As regras que não precisam da BD estão em [ProdutorValidacao]; aqui as que precisam: o país e a região existem, a
 * região é **do país escolhido** (a BD recusaria com uma FK composta, mas com um erro Oracle em vez de uma frase) e o nome não
 * repete o de outro produtor (sem contar acentos nem maiúsculas: "Esporão" e "ESPORAO" são o mesmo).
 */
@Service
class AdminProdutorService(
    private val produtores: ProdutorRepository,
    private val listas: AdminProdutorRepository,
    private val paises: ProdutorPaisRepository,
    private val regioes: RegiaoRepository,
    private val bebidas: BebidaRepository,
    private val clock: Clock,
) {

    // Portugal primeiro (o mercado da app), o resto por ordem alfabética.
    @Transactional(readOnly = true)
    fun paises(): List<OpcaoLookup> =
        paises.findAll().ordenadoPorNomeComDestaques(listOf(PAIS_EM_DESTAQUE)) { it.value }.map { OpcaoLookup(it.id, it.value.orEmpty()) }

    // Todas as regiões do país (não só as que têm bebidas, como o filtro da app): um produtor novo pode ser a 1.ª bebida da região.
    @Transactional(readOnly = true)
    fun regioesDoPais(paisId: Long?): List<OpcaoLookup> =
        if (paisId == null) emptyList() else regioes.findByPais_Id(paisId).ordenadoPorNome { it.nome }.map { OpcaoLookup(it.id, it.nome.orEmpty()) }

    @Transactional(readOnly = true)
    fun carregar(id: Long): ProdutorParaEditar {
        val produtor = produtores.findByIdWithPais(id).orElseThrow { ResourceNotFoundException("Produtor $id não encontrado") }
        return ProdutorParaEditar(
            id = produtor.id,
            nome = produtor.nome.orEmpty(),
            formulario = ProdutorFormulario(
                nome = produtor.nome.orEmpty(),
                paisId = produtor.pais?.id?.toString().orEmpty(),
                regiaoId = produtor.regiao?.id?.toString().orEmpty(),
                anoFundacao = produtor.anoFundacao?.toString().orEmpty(),
                website = produtor.website.orEmpty(),
                imagem = produtor.pathImagem.orEmpty(),
                morada = produtor.morada.orEmpty(),
                latitude = produtor.latitude?.toPlainString().orEmpty(),
                longitude = produtor.longitude?.toPlainString().orEmpty(),
                historia = produtor.historia.orEmpty(),
                permiteVisitas = produtor.permiteVisitas,
            ),
            totalBebidas = bebidas.countByProdutor_Id(id),
        )
    }

    @Transactional
    fun criar(form: ProdutorFormulario): ResultadoGravacao = gravar(form, id = null)

    @Transactional
    fun atualizar(id: Long, form: ProdutorFormulario): ResultadoGravacao {
        if (!produtores.existsById(id)) throw ResourceNotFoundException("Produtor $id não encontrado")
        return gravar(form, id)
    }

    private fun gravar(form: ProdutorFormulario, id: Long?): ResultadoGravacao {
        val dados = when (val validacao = ProdutorValidacao.validar(form, LocalDate.now(clock).year)) {
            is ValidacaoProdutor.Invalido -> return ResultadoGravacao.Invalido(validacao.erros)
            is ValidacaoProdutor.Valido -> validacao.dados
        }

        // O que só a BD sabe. Cada verificação acrescenta o seu erro, para o formulário mostrar tudo de uma vez.
        val erros = linkedMapOf<String, String>()
        val pais = paises.findById(dados.paisId).orElse(null)
        if (pais == null) erros["paisId"] = "País inválido."

        val regiao = dados.regiaoId?.let { regioes.findById(it).orElse(null) }
        if (dados.regiaoId != null && regiao == null) {
            erros["regiaoId"] = "Região inválida."
        } else if (regiao != null && pais != null && regiao.pais?.id != pais.id) {
            erros["regiaoId"] = "Esta região não é do país escolhido."
        }

        val repetidos = listas.idsComNome(PesquisaTexto.normalizar(dados.nome), ignorarId = id ?: SEM_ID)
        if (repetidos.isNotEmpty()) erros["nome"] = "Já existe um produtor com este nome (id ${repetidos.first()})."

        if (erros.isNotEmpty() || pais == null) return ResultadoGravacao.Invalido(erros)

        val produtor = if (id == null) {
            Produtor(dataCriacao = Instant.now(clock))
        } else {
            produtores.findById(id).orElseThrow { ResourceNotFoundException("Produtor $id não encontrado") }
        }
        produtor.nome = dados.nome
        produtor.pais = pais
        produtor.regiao = regiao
        produtor.anoFundacao = dados.anoFundacao
        produtor.website = dados.website
        produtor.pathImagem = dados.imagem
        produtor.morada = dados.morada
        produtor.latitude = dados.latitude
        produtor.longitude = dados.longitude
        produtor.historia = dados.historia
        produtor.permiteVisitas = dados.permiteVisitas
        return ResultadoGravacao.Guardado(produtores.save(produtor).id)
    }

    private companion object {
        // "Nenhum" para o `ignorarId` da query do nome repetido (uma criação não exclui ninguém); um id real nunca é negativo.
        const val SEM_ID = -1L
    }
}
