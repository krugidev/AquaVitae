package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.PageResponse
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório de catálogo: pesquisa/listagem de bebidas, detalhe e páginas de produtor. */
class BebidaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun searchBebidas(
        search: String? = null,
        categoriaId: Long? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<PageResponse<BebidaSummary>> = runCatching {
        api.searchBebidas(search = search, categoriaId = categoriaId, page = page, size = size)
    }

    suspend fun getBebidaDetail(id: Long): Result<BebidaDetail> = runCatching {
        api.getBebidaDetail(id)
    }

    suspend fun getProdutorDetail(id: Long): Result<ProdutorDetail> = runCatching {
        api.getProdutorDetail(id)
    }
}
