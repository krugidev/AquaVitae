package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.CatalogFiltro
import pt.aquavitae.android.data.model.PageResponse
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório de catálogo: pesquisa/listagem de bebidas, sugestões da homepage, detalhe e páginas de produtor. */
class BebidaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun searchBebidas(
        search: String? = null,
        categoriaIds: List<Long>? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String? = null,
    ): Result<PageResponse<BebidaSummary>> = runCatching {
        api.searchBebidas(search = search, categoriaIds = categoriaIds, page = page, size = size, sort = sort)
    }

    /** O popup de filtros do catálogo: todos os campos de [CatalogFiltro] traduzidos para a query da API. */
    suspend fun searchBebidas(
        filtro: CatalogFiltro,
        page: Int = 0,
        size: Int = 20,
        sort: String? = null,
    ): Result<PageResponse<BebidaSummary>> = runCatching {
        api.searchBebidas(
            search = filtro.search?.trim()?.ifBlank { null },
            categoriaIds = filtro.categoriaId?.let { listOf(it) },
            produtorId = filtro.produtorId,
            paisId = filtro.paisId,
            regiaoIds = filtro.regiaoIds.ifEmpty { null }?.toList(),
            ratingMin = filtro.ratingMin,
            acidezMin = filtro.acidezMin,
            acidezMax = filtro.acidezMax,
            docuraMin = filtro.docuraMin,
            docuraMax = filtro.docuraMax,
            corpoId = filtro.corpoId,
            taninoId = filtro.taninoId,
            tipoId = filtro.tipoId,
            castaIds = filtro.castaIds.ifEmpty { null }?.toList(),
            precoMin = filtro.precoMin,
            precoMax = filtro.precoMax,
            page = page,
            size = size,
            sort = sort,
        )
    }

    /** "Escolhido para ti" (homepage): as sugestões do utilizador (auth), ordenadas por rating pelo backend. */
    suspend fun getSugeridas(size: Int = 6): Result<PageResponse<BebidaSummary>> = runCatching {
        api.getBebidasSugeridas(page = 0, size = size)
    }

    suspend fun getBebidaDetail(id: Long): Result<BebidaDetail> = runCatching {
        api.getBebidaDetail(id)
    }

    suspend fun getProdutorDetail(id: Long): Result<ProdutorDetail> = runCatching {
        api.getProdutorDetail(id)
    }

    /** O produtor em destaque da semana (cartão da homepage). */
    suspend fun getProdutorDestaque(): Result<ProdutorDetail> = runCatching {
        api.getProdutorDestaque()
    }

    /** As bebidas de um produtor: a página do produtor pede as 6 primeiras, o catálogo do produtor pagina o resto. */
    suspend fun getBebidasDoProdutor(
        produtorId: Long,
        categoriaId: Long? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String? = null,
    ): Result<PageResponse<BebidaSummary>> = runCatching {
        api.getBebidasDoProdutor(produtorId, categoriaId = categoriaId, page = page, size = size, sort = sort)
    }
}
