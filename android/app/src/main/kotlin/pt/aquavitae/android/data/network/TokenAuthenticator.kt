package pt.aquavitae.android.data.network

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import pt.aquavitae.android.data.local.TokenDataStore
import pt.aquavitae.android.data.model.RefreshRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Sessão renovável (2026-09-24): o token de acesso do backend dura 60 minutos e, antes disto, uma resposta 401 a meio da
 * utilização deixava a app com erros sem explicação. O OkHttp chama este `Authenticator` quando um pedido volta com 401:
 *
 * 1. só renova pedidos que levavam sessão (um 401 num pedido anónimo, ex. o login com a password errada, é a resposta certa)
 *    e nunca os de `/api/auth/` (login, registo, renovação, recuperação: evita ciclos);
 * 2. **uma renovação de cada vez** (`synchronized`): se vários pedidos falham juntos, o primeiro renova e os outros repetem
 *    com o token novo que já está guardado — o refresh token roda a cada uso, por isso usá-lo duas vezes seguidas falharia;
 * 3. renovação com sucesso: guarda o par novo e repete o pedido com o token novo;
 * 4. token recusado (400/401/403: expirou, foi revogado ou a password mudou): a sessão morreu — apaga-a e avisa a UI
 *    ([SessionEvents]) para levar ao login;
 * 5. **sem rede ou erro do servidor: a sessão mantém-se** e este pedido falha com o 401; o utilizador tenta de novo. Apagar a
 *    sessão por uma falha de rede seria pôr toda a gente com má cobertura a fazer login a toda a hora.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val sessionEvents: SessionEvents,
    private val refreshApi: AuthRefreshApi,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val pedido = response.request
        val enviado = pedido.header("Authorization")?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() } ?: return null
        if (pedido.url.encodedPath.contains("/api/auth/")) return null
        // Este pedido já é uma repetição (depois de uma renovação ou de repetir com o token novo) e voltou a falhar: o 401 não
        // é da sessão (um recurso proibido, por exemplo). Uma renovação por pedido chega — sem isto renovava-se duas vezes.
        if (tentativasAnteriores(response) >= 1) return null

        return synchronized(lock) {
            val (atual, refresh) = runBlocking { tokenDataStore.tokens() }
            when {
                // Já não há sessão (terminou entretanto): nada a renovar.
                atual.isNullOrBlank() -> null
                // Outro pedido já renovou enquanto este esperava: repete com o token novo, sem tocar no refresh token.
                atual != enviado -> pedido.comToken(atual)
                // Sessão anterior a esta funcionalidade (sem refresh token): não há como renovar.
                refresh.isNullOrBlank() -> {
                    encerrar()
                    null
                }
                else -> renovar(pedido, refresh)
            }
        }
    }

    private fun renovar(pedido: Request, refreshToken: String): Request? = try {
        val nova = runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }
        runBlocking { tokenDataStore.updateTokens(nova.token, nova.refreshToken) }
        pedido.comToken(nova.token)
    } catch (erro: HttpException) {
        if (erro.code() in 400..403) encerrar()
        null
    } catch (_: IOException) {
        null
    }

    private fun encerrar() {
        runBlocking { tokenDataStore.clearSession() }
        sessionEvents.notificarSessaoExpirada()
    }

    private fun Request.comToken(token: String): Request = newBuilder().header("Authorization", "Bearer $token").build()

    private fun tentativasAnteriores(response: Response): Int = generateSequence(response.priorResponse) { it.priorResponse }.count()
}
