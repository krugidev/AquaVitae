package pt.aquavitae.android.data.network

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import pt.aquavitae.android.data.local.TokenDataStore
import javax.inject.Inject

/**
 * Interceptor OkHttp que anexa automaticamente `Authorization: Bearer <token>`
 * a todos os pedidos, lendo o JWT guardado no [TokenDataStore]. Os endpoints
 * públicos (ex.: login/registo, listagem/pesquisa de bebidas) simplesmente
 * ignoram o header no backend — mais simples do que manter uma lista de
 * exceções aqui.
 *
 * A leitura do DataStore é suspend; como o `Interceptor.intercept` do OkHttp
 * é síncrono, usa-se `runBlocking` — aceitável aqui porque já corremos numa
 * thread de rede do OkHttp, não na thread principal.
 */
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.tokenFlow.firstOrNull() }

        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
