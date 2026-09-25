package pt.aquavitae.android.data.network

import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.RefreshRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * A renovação da sessão, à parte da [AquaVitaeApi] de propósito: tem o seu próprio cliente OkHttp, **sem** o
 * [AuthInterceptor] nem o [TokenAuthenticator] (o próprio `Authenticator` chama isto — com o mesmo cliente seria um ciclo).
 */
interface AuthRefreshApi {

    /** Troca o refresh token por um par novo (token de acesso + novo refresh token, rodado). 401 se já não serve. */
    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse
}
