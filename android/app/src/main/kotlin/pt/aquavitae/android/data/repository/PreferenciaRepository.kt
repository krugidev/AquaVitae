package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório das preferências de onboarding (corpo/acidez/doçura, categorias e castas). */
class PreferenciaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun updatePreferencias(
        acidezMin: Int?,
        acidezMax: Int?,
        docuraMin: Int?,
        docuraMax: Int?,
        categoriaIds: List<Long>,
        castaIds: List<Long>,
    ): Result<Unit> = runCatching {
        api.updatePreferencias(
            PreferenciaRequest(
                acidezMin = acidezMin,
                acidezMax = acidezMax,
                docuraMin = docuraMin,
                docuraMax = docuraMax,
                categoriaIds = categoriaIds,
                castaIds = castaIds,
            ),
        )
    }
}
