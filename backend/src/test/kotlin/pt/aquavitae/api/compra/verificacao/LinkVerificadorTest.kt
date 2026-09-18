package pt.aquavitae.api.compra.verificacao

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.Disponivel
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.Inconclusivo
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.LinkInvalido
import pt.aquavitae.api.compra.verificacao.ResultadoVerificacao.SemStock
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LinkVerificadorTest {

    private lateinit var server: HttpServer
    private val verificador = LinkVerificador(intervaloMs = 0)
    private var userAgentRecebido: String? = null

    private fun base() = "http://localhost:${server.address.port}"

    private fun produto(disponibilidade: String) =
        """<html><head><script type="application/ld+json">
           {"@type":"Product","offers":{"availability":"https://schema.org/$disponibilidade"}}
           </script></head></html>"""

    private fun responder(ex: HttpExchange, codigo: Int, corpo: String = "") {
        val bytes = corpo.toByteArray()
        ex.sendResponseHeaders(codigo, if (bytes.isEmpty()) -1 else bytes.size.toLong())
        if (bytes.isNotEmpty()) ex.responseBody.use { it.write(bytes) }
        ex.close()
    }

    private fun redirecionar(ex: HttpExchange, para: String) {
        ex.responseHeaders.add("Location", para)
        ex.sendResponseHeaders(302, -1)
        ex.close()
    }

    @BeforeTest
    fun arrancar() {
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.createContext("/em-stock") { ex ->
            userAgentRecebido = ex.requestHeaders.getFirst("User-Agent")
            responder(ex, 200, produto("InStock"))
        }
        server.createContext("/esgotado") { ex -> responder(ex, 200, produto("OutOfStock")) }
        server.createContext("/sem-dados") { ex -> responder(ex, 200, "<html><body>Produto</body></html>") }
        server.createContext("/retirado") { ex -> responder(ex, 404) }
        server.createContext("/gone") { ex -> responder(ex, 410) }
        server.createContext("/bloqueado") { ex -> responder(ex, 403) }
        server.createContext("/erro") { ex -> responder(ex, 500) }
        server.createContext("/produto/antigo") { ex -> redirecionar(ex, "/") }
        server.createContext("/novo-url") { ex -> redirecionar(ex, "/em-stock") }
        server.createContext("/") { ex -> responder(ex, 200, "<html><body>Homepage</body></html>") }
        server.start()
    }

    @AfterTest
    fun parar() {
        server.stop(0)
    }

    @Test
    fun `pagina com stock`() {
        assertEquals(Disponivel(stockConfirmado = true), verificador.verificar("${base()}/em-stock"))
    }

    @Test
    fun `pagina esgotada`() {
        assertEquals(SemStock, verificador.verificar("${base()}/esgotado"))
    }

    @Test
    fun `pagina sem dados estruturados esta disponivel mas sem stock confirmado`() {
        assertEquals(Disponivel(stockConfirmado = false), verificador.verificar("${base()}/sem-dados"))
    }

    @Test
    fun `404 e 410 sao links invalidos`() {
        assertIs<LinkInvalido>(verificador.verificar("${base()}/retirado"))
        assertIs<LinkInvalido>(verificador.verificar("${base()}/gone"))
    }

    @Test
    fun `403 e 500 sao inconclusivos, nunca links invalidos`() {
        assertIs<Inconclusivo>(verificador.verificar("${base()}/bloqueado"))
        assertIs<Inconclusivo>(verificador.verificar("${base()}/erro"))
    }

    @Test
    fun `redirecionar para a homepage e um soft 404`() {
        val resultado = verificador.verificar("${base()}/produto/antigo")
        assertIs<LinkInvalido>(resultado)
        assertTrue(resultado.detalhe.contains("inicial"))
    }

    @Test
    fun `redirecionar para outra pagina de produto segue o redirect`() {
        assertEquals(Disponivel(stockConfirmado = true), verificador.verificar("${base()}/novo-url"))
    }

    @Test
    fun `ligacao recusada e inconclusiva`() {
        val portaFechada = server.address.port
        server.stop(0)
        assertIs<Inconclusivo>(verificador.verificar("http://localhost:$portaFechada/qualquer"))
        // o @AfterTest volta a parar o servidor — parar duas vezes é inofensivo
    }

    @Test
    fun `urls invalidos sao inconclusivos`() {
        assertIs<Inconclusivo>(verificador.verificar("ftp://exemplo.pt/x"))
        assertIs<Inconclusivo>(verificador.verificar("isto nao e um url"))
    }

    @Test
    fun `identifica-se com um user-agent proprio`() {
        verificador.verificar("${base()}/em-stock")
        assertTrue(userAgentRecebido?.startsWith("AquaVitaeLinkChecker") == true)
    }
}
