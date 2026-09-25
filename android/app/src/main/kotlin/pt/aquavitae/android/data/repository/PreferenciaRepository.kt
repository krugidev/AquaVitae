package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.PreferenciaResponse
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório das preferências do utilizador (doçura/acidez, tipos de bebida e castas) — onboarding e ecrã de perfil. */
class PreferenciaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getPreferencias(): Result<PreferenciaResponse> = runCatching { api.getPreferencias() }

    suspend fun updatePreferencias(request: PreferenciaRequest): Result<Unit> = runCatching {
        api.updatePreferencias(request)
    }
}
