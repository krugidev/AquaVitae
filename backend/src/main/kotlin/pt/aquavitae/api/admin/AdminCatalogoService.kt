package pt.aquavitae.api.admin

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.PesquisaTexto
import pt.aquavitae.api.lookup.BebidaCategoria
import pt.aquavitae.api.lookup.BebidaCategoriaRepository
import pt.aquavitae.api.utilizador.UtilizadorRepository

enum class OrdemBebidas(val codigo: String, val rotulo: String, val sort: Sort) {
    RECENTES("recentes", "Mais recentes primeiro", Sort.by(Sort.Direction.DESC, "id")),
    NOME("nome", "Nome (A-Z)", Sort.by("nome").and(Sort.by("id"))),
    ;

    companion object {
        fun deCodigo(codigo: String?): OrdemBebidas = entries.firstOrNull { it.codigo == codigo } ?: RECENTES
    }
}

enum class OrdemProdutores(val codigo: String, val rotulo: String, val sort: Sort) {
    NOME("nome", "Nome (A-Z)", Sort.by("nome").and(Sort.by("id"))),
    RECENTES("recentes", "Mais recentes primeiro", Sort.by(Sort.Direction.DESC, "id")),
    ;

    companion object {
        fun deCodigo(codigo: String?): OrdemProdutores = entries.firstOrNull { it.codigo == codigo } ?: NOME
    }
}

data class ContagemQualidade(val codigo: String, val rotulo: String, val total: Long)

/** O que o painel inicial mostra: totais e, por cada critério de qualidade, quantas bebidas/produtores ainda o falham. */
data class ResumoAdmin(
    val totalBebidas: Long,
    val totalProdutores: Long,
    val totalUtilizadores: Long,
    val linksAtivos: Long,
    val linksInativos: Long,
    val problemasBebidas: List<ContagemQualidade>,
    val problemasProdutores: List<ContagemQualidade>,
)

/**
 * As leituras do painel (fatia 1: só listas e contagens). Sem entidades pelo caminho: as listas vêm de queries com projeção, por
 * isso não há N+1 nem dependência do Open-Session-In-View.
 */
@Service
@Transactional(readOnly = true)
class AdminCatalogoService(
    private val bebidas: AdminBebidaRepository,
    private val produtores: AdminProdutorRepository,
    private val categorias: BebidaCategoriaRepository,
    private val utilizadores: UtilizadorRepository,
) {

    fun categorias(): List<BebidaCategoria> = categorias.findAll().sortedBy { it.id }

    fun listarBebidas(
        texto: String?,
        categoriaId: Long?,
        qualidade: QualidadeBebida?,
        ordem: OrdemBebidas,
        pagina: Int,
    ): PaginaAdmin<BebidaAdminLinha> = paginar(pagina) { p ->
        bebidas.listar(PesquisaTexto.padraoContem(texto), categoriaId, qualidade?.codigo, PageRequest.of(p, TAMANHO_PAGINA_ADMIN, ordem.sort))
    }

    fun listarProdutores(
        texto: String?,
        qualidade: QualidadeProdutor?,
        ordem: OrdemProdutores,
        pagina: Int,
    ): PaginaAdmin<ProdutorAdminLinha> = paginar(pagina) { p ->
        produtores.listar(PesquisaTexto.padraoContem(texto), qualidade?.codigo, PageRequest.of(p, TAMANHO_PAGINA_ADMIN, ordem.sort))
    }

    fun resumo(): ResumoAdmin {
        val linksAtivos = bebidas.contarLinks(true)
        return ResumoAdmin(
            totalBebidas = bebidas.count(),
            totalProdutores = produtores.count(),
            totalUtilizadores = utilizadores.count(),
            linksAtivos = linksAtivos,
            linksInativos = bebidas.contarLinks(false),
            problemasBebidas = QualidadeBebida.entries.map { q ->
                ContagemQualidade(q.codigo, q.rotulo, listarBebidas(null, null, q, OrdemBebidas.RECENTES, 0).total)
            },
            problemasProdutores = QualidadeProdutor.entries.map { q ->
                ContagemQualidade(q.codigo, q.rotulo, listarProdutores(null, q, OrdemProdutores.NOME, 0).total)
            },
        )
    }

    // Uma página pedida para lá da última (a lista encolheu entretanto, ou um URL antigo) volta à última em vez de aparecer vazia.
    private fun <T> paginar(pedida: Int, buscar: (Int) -> Page<T>): PaginaAdmin<T> {
        var pagina = buscar(pedida.coerceAtLeast(0))
        if (pagina.content.isEmpty() && pagina.totalPages > 0 && pagina.number >= pagina.totalPages) {
            pagina = buscar(pagina.totalPages - 1)
        }
        return PaginaAdmin(pagina.content, pagina.number, pagina.totalPages, pagina.totalElements)
    }
}
