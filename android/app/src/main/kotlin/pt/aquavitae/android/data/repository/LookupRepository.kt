package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** As listas de opções dos formulários (públicas, `GET /api/lookup/...`). */
class LookupRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun nacionalidades(): Result<List<Nacionalidade>> = runCatching { api.getNacionalidades() }

    suspend fun avatarCategorias(): Result<List<LookupItem>> = runCatching { api.getAvatarCategorias() }

    suspend fun avatares(): Result<List<Avatar>> = runCatching { api.getAvatares() }

    suspend fun categoriasBebida(): Result<List<LookupItem>> = runCatching { api.getCategoriasBebida() }

    suspend fun castas(): Result<List<Casta>> = runCatching { api.getCastas() }

    // --- Popup de filtros do catálogo ---

    suspend fun paisesBebida(): Result<List<LookupItem>> = runCatching { api.getPaisesBebida() }

    /** Só as regiões desse país com pelo menos uma bebida (pílulas de "Origem"). */
    suspend fun regioes(paisId: Long): Result<List<LookupItem>> = runCatching { api.getRegioes(paisId) }

    suspend fun vinhoCorpos(): Result<List<LookupItem>> = runCatching { api.getVinhoCorpos() }

    suspend fun vinhoTaninos(): Result<List<LookupItem>> = runCatching { api.getVinhoTaninos() }

    suspend fun vinhoTipos(): Result<List<LookupItem>> = runCatching { api.getVinhoTipos() }
}
