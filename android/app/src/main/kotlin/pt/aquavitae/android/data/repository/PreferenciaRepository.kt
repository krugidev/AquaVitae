package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório das preferências de onboarding (doçura/acidez, tipos de bebida e castas). */
class PreferenciaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun updatePreferencias(request: PreferenciaRequest): Result<Unit> = runCatching {
        api.updatePreferencias(request)
    }
}
