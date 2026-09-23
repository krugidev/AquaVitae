package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.data.model.UtilizadorUpdateRequest
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** O perfil do utilizador autenticado e a aceitação dos termos e condições. */
class UserRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getMe(): Result<UtilizadorMe> = runCatching { api.getMe() }

    /** Guarda o perfil preenchido no onboarding (nome, nacionalidade, descrição, avatar). */
    suspend fun updatePerfil(request: UtilizadorUpdateRequest): Result<UtilizadorMe> = runCatching { api.updateMe(request) }

    suspend fun aceitarTermos(): Result<UtilizadorMe> = runCatching { api.aceitarTermos() }
}
