package pt.aquavitae.android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.aquavitae.android.data.local.TokenDataStore
import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.LoginRequest
import pt.aquavitae.android.data.model.RecuperarPasswordRequest
import pt.aquavitae.android.data.model.RedefinirPasswordRequest
import pt.aquavitae.android.data.model.RefreshRequest
import pt.aquavitae.android.data.model.RegisterRequest
import pt.aquavitae.android.data.model.VerificarCodigoRequest
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

    /** `identificador` = username ou email. */
    suspend fun login(identificador: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(LoginRequest(identificador = identificador, password = password))
        tokenDataStore.saveSession(response.token, response.refreshToken, response.userId, response.username)
        response
    }

    /** Fase 1 do registo. O nome, a nacionalidade, o avatar e as preferências vêm depois (`PUT /api/users/me`). */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        aceitouTermos: Boolean,
    ): Result<AuthResponse> = runCatching {
        val response = api.register(
            RegisterRequest(username = username, email = email, password = password, aceitouTermos = aceitouTermos),
        )
        tokenDataStore.saveSession(response.token, response.refreshToken, response.userId, response.username)
        response
    }

    /**
     * Passo 1 da recuperação: pede o código. A API responde sempre 202, exista ou não a conta (não revela que contas existem),
     * por isso um sucesso aqui não quer dizer que a conta existe. `identificador` = username ou email.
     */
    suspend fun recuperarPassword(identificador: String): Result<Unit> = runCatching {
        api.recuperarPassword(RecuperarPasswordRequest(identificador))
    }

    /** Passo 2: confere o código (401 "Código inválido" ou "Código expirado" se não servir). */
    suspend fun verificarCodigo(identificador: String, codigo: String): Result<Unit> = runCatching {
        api.verificarCodigo(VerificarCodigoRequest(identificador, codigo))
    }

    /** Passo 3: define a nova password. Não inicia sessão: o utilizador entra a seguir, no login. */
    suspend fun redefinirPassword(identificador: String, codigo: String, novaPassword: String): Result<Unit> = runCatching {
        api.redefinirPassword(RedefinirPasswordRequest(identificador, codigo, novaPassword))
    }

    /**
     * Termina a sessão: revoga o refresh token no servidor (melhor esforço — sem rede, ele expira sozinho ao fim de 30 dias) e
     * apaga a sessão do telemóvel, que é o que conta.
     */
    suspend fun logout() {
        val refreshToken = tokenDataStore.tokens().second
        if (!refreshToken.isNullOrBlank()) runCatching { api.logout(RefreshRequest(refreshToken)) }
        tokenDataStore.clearSession()
    }
}
