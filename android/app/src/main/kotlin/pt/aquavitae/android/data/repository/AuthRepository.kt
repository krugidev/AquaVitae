package pt.aquavitae.android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.aquavitae.android.data.local.TokenDataStore
import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.LoginRequest
import pt.aquavitae.android.data.model.RegisterRequest
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/**
 * Repositório de autenticação: chama a [AquaVitaeApi] e, em caso de sucesso,
 * persiste a sessão (token/userId/username) no [TokenDataStore].
 */
class AuthRepository @Inject constructor(
    private val api: AquaVitaeApi,
    private val tokenDataStore: TokenDataStore,
) {
    val isLoggedIn: Flow<Boolean> = tokenDataStore.tokenFlow.map { !it.isNullOrBlank() }

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(LoginRequest(email = email, password = password))
        tokenDataStore.saveSession(response.token, response.userId, response.username)
        response
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<AuthResponse> = runCatching {
        val response = api.register(
            RegisterRequest(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
            ),
        )
        tokenDataStore.saveSession(response.token, response.userId, response.username)
        response
    }

    suspend fun logout() {
        tokenDataStore.clearSession()
    }
}
