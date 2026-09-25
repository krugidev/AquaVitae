package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório da wishlist do utilizador autenticado (distinta da Cave e dos Favoritos). */
class WishlistRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getWishlist(sort: String? = null): Result<List<BebidaRelacao>> = runCatching {
        api.getWishlist(sort)
    }

    suspend fun addToWishlist(bebidaId: Long): Result<Unit> = runCatching {
        api.addWishlist(bebidaId)
    }

    suspend fun removeFromWishlist(bebidaId: Long): Result<Unit> = runCatching {
        api.removeWishlist(bebidaId)
    }
}
