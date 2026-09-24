package pt.aquavitae.android.data.network

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.aquavitae.android.data.local.TokenDataStore
import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.RefreshRequest
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/** A renovação da sessão, de ponta a ponta contra um servidor local: o que o `Authenticator` faz a cada 401. */
class TokenAuthenticatorTest {

    private lateinit var servidor: MockWebServer
    private lateinit var tokens: TokenDataStore
    private lateinit var ficheiro: File
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val eventos = SessionEvents()

    /** A API de renovação de faz-de-conta: conta as chamadas e responde (ou falha) como o teste mandar. */
    private class RefreshFalso(var resposta: () -> AuthResponse) : AuthRefreshApi {
        var chamadas = 0
        var ultimoRefreshToken: String? = null

        override suspend fun refresh(request: RefreshRequest): AuthResponse {
            chamadas++
            ultimoRefreshToken = request.refreshToken
            return resposta()
        }
    }

    private val sessaoNova = AuthResponse(token = "novo", refreshToken = "refresh-novo", userId = 1, username = "ana")

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        ficheiro = File.createTempFile("sessao", ".preferences_pb").also { it.delete() }
        tokens = TokenDataStore(PreferenceDataStoreFactory.create(scope = escopo, produceFile = { ficheiro }))
    }

    @After
    fun limpar() {
        servidor.shutdown()
        escopo.cancel()
        ficheiro.delete()
    }

    private fun sessao(token: String = "velho", refresh: String = "refresh-velho") =
        runBlocking { tokens.saveSession(token, refresh, userId = 1, username = "ana") }

    private fun cliente(refresh: AuthRefreshApi, comInterceptor: Boolean = true): OkHttpClient = OkHttpClient.Builder()
        .apply { if (comInterceptor) addInterceptor(AuthInterceptor(tokens)) }
        .authenticator(TokenAuthenticator(tokens, eventos, refresh))
        .build()

    private fun pedir(cliente: OkHttpClient, caminho: String = "/api/users/me", cabecalho: String? = null): Response {
        val pedido = Request.Builder().url(servidor.url(caminho)).apply { cabecalho?.let { header("Authorization", it) } }.build()
        return cliente.newCall(pedido).execute()
    }

    private fun proximoPedido() = servidor.takeRequest(2, TimeUnit.SECONDS)

    private fun erroHttp(codigo: Int) = HttpException(retrofit2.Response.error<Any>(codigo, "{}".toResponseBody("application/json".toMediaType())))

    @Test
    fun `um 401 com sessao renova e repete o pedido com o token novo`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(200))
        val refresh = RefreshFalso { sessaoNova }

        val resposta = pedir(cliente(refresh))

        assertEquals(200, resposta.code)
        assertEquals("Bearer velho", proximoPedido()?.getHeader("Authorization"))
        assertEquals("Bearer novo", proximoPedido()?.getHeader("Authorization"))
        assertEquals(1, refresh.chamadas)
        assertEquals("refresh-velho", refresh.ultimoRefreshToken)
        assertEquals("novo" to "refresh-novo", runBlocking { tokens.tokens() })
        assertFalse(eventos.avisoNoLogin.value)
    }

    @Test
    fun `se a renovacao for recusada apaga a sessao e avisa a interface`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { throw erroHttp(401) }

        val resposta = pedir(cliente(refresh))

        assertEquals(401, resposta.code)
        assertEquals(null to null, runBlocking { tokens.tokens() })
        assertTrue(eventos.avisoNoLogin.value)
    }

    @Test
    fun `sem rede a sessao mantem-se e o pedido falha com o 401`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { throw IOException("sem rede") }

        val resposta = pedir(cliente(refresh))

        assertEquals(401, resposta.code)
        assertEquals("velho" to "refresh-velho", runBlocking { tokens.tokens() })
        assertFalse(eventos.avisoNoLogin.value)
    }

    @Test
    fun `um erro do servidor na renovacao tambem nao apaga a sessao`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { throw erroHttp(500) }

        pedir(cliente(refresh))

        assertEquals("velho" to "refresh-velho", runBlocking { tokens.tokens() })
        assertFalse(eventos.avisoNoLogin.value)
    }

    @Test
    fun `um 401 num pedido anonimo nao renova nada`() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { sessaoNova }

        val resposta = pedir(cliente(refresh))

        assertEquals(401, resposta.code)
        assertEquals(0, refresh.chamadas)
        assertFalse(eventos.avisoNoLogin.value)
    }

    @Test
    fun `os pedidos de autenticacao nunca se renovam`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { sessaoNova }

        val resposta = pedir(cliente(refresh), caminho = "/api/auth/login")

        assertEquals(401, resposta.code)
        assertEquals(0, refresh.chamadas)
        assertEquals("velho" to "refresh-velho", runBlocking { tokens.tokens() })
    }

    @Test
    fun `se outro pedido ja renovou repete com o token guardado sem gastar o refresh token`() {
        sessao(token = "atual", refresh = "refresh-atual")
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(200))
        val refresh = RefreshFalso { sessaoNova }

        // Um pedido que partiu com o token antigo (sem o interceptor, que leria o novo) e falhou depois de outro já ter renovado.
        val resposta = pedir(cliente(refresh, comInterceptor = false), cabecalho = "Bearer velho")

        assertEquals(200, resposta.code)
        assertEquals("Bearer velho", proximoPedido()?.getHeader("Authorization"))
        assertEquals("Bearer atual", proximoPedido()?.getHeader("Authorization"))
        assertEquals(0, refresh.chamadas)
        assertEquals("atual" to "refresh-atual", runBlocking { tokens.tokens() })
    }

    @Test
    fun `uma sessao antiga sem refresh token termina a sessao`() {
        sessao(refresh = "")
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { sessaoNova }

        val resposta = pedir(cliente(refresh))

        assertEquals(401, resposta.code)
        assertEquals(0, refresh.chamadas)
        assertNull(runBlocking { tokens.tokens() }.first)
        assertTrue(eventos.avisoNoLogin.value)
    }

    @Test
    fun `nao insiste em ciclo se o servidor continua a recusar o token novo`() {
        sessao()
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(401))
        val refresh = RefreshFalso { sessaoNova }

        val resposta = pedir(cliente(refresh))

        assertEquals(401, resposta.code)
        assertEquals(1, refresh.chamadas)
    }

    @Test
    fun `o aviso do login limpa-se ao voltar a entrar`() {
        eventos.notificarSessaoExpirada()
        assertTrue(eventos.avisoNoLogin.value)
        eventos.limparAviso()
        assertFalse(eventos.avisoNoLogin.value)
    }
}
