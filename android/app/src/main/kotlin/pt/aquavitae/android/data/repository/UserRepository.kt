package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.ApagarContaRequest
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

    /** Apaga a conta e tudo o que é dela (não se desfaz). `Password incorreta.` (403) se a password não for a da conta. */
    suspend fun apagarConta(password: String): Result<Unit> = runCatching { api.apagarConta(ApagarContaRequest(password)) }
}
