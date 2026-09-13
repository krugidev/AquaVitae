package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório dos favoritos do utilizador autenticado. */
class FavoritoRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getFavoritos(): Result<List<BebidaSummary>> = runCatching {
        api.getFavoritos()
    }

    suspend fun addFavorito(bebidaId: Long): Result<Unit> = runCatching {
        api.addFavorito(bebidaId)
    }

    suspend fun removeFavorito(bebidaId: Long): Result<Unit> = runCatching {
        api.removeFavorito(bebidaId)
    }
}
