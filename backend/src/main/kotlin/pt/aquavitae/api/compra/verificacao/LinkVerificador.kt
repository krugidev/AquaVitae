package pt.aquavitae.api.compra.verificacao

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

sealed interface ResultadoVerificacao {
    // A página existe e (stockConfirmado) declara stock, ou (não confirmado) não declara nada.
    data class Disponivel(val stockConfirmado: Boolean) : ResultadoVerificacao
    data object SemStock : ResultadoVerificacao
    data class LinkInvalido(val detalhe: String) : ResultadoVerificacao

    // Timeouts, bloqueios anti-bot (403/429), erros 5xx, falhas de rede: não diz nada sobre o link.
    data class Inconclusivo(val detalhe: String) : ResultadoVerificacao
}

private const val USER_AGENT = "AquaVitaeLinkChecker/1.0 (+https://krugidev.github.io/AquaVitae/)"
private const val MAX_BYTES_RESPOSTA = 2_000_000

@Component
class LinkVerificador(
    @Value("\${aquavitae.links.verificacao.intervalo-ms:1000}") private val intervaloMs: Long,
) {
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val ultimoPedidoPorHost = ConcurrentHashMap<String, Long>()

    fun verificar(url: String): ResultadoVerificacao {
        val uri = try {
            URI.create(url.trim())
        } catch (e: IllegalArgumentException) {
            return ResultadoVerificacao.Inconclusivo("URL inválido")
        }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host == null) {
            return ResultadoVerificacao.Inconclusivo("URL inválido")
        }

        esperarVez(uri.host)

        val resposta = try {
            val pedido = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "pt-PT,pt;q=0.9,en;q=0.5")
                .GET()
                .build()
            client.send(pedido, HttpResponse.BodyHandlers.ofInputStream())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return ResultadoVerificacao.Inconclusivo("Interrompido")
        } catch (e: IOException) {
            return ResultadoVerificacao.Inconclusivo(e.javaClass.simpleName)
        } catch (e: IllegalArgumentException) {
            return ResultadoVerificacao.Inconclusivo("Pedido inválido")
        }

        resposta.body().use { corpo ->
            val estado = resposta.statusCode()
            return when {
                estado == 404 || estado == 410 -> ResultadoVerificacao.LinkInvalido("HTTP $estado")
                estado in 200..299 -> {
                    // Lê-se no máximo MAX_BYTES_RESPOSTA: os dados estruturados vêm no HTML, e assim
                    // uma página gigante (ou um ficheiro) não é carregada inteira para memória.
                    val html = try {
                        corpo.readNBytes(MAX_BYTES_RESPOSTA).toString(Charsets.UTF_8)
                    } catch (e: IOException) {
                        return ResultadoVerificacao.Inconclusivo(e.javaClass.simpleName)
                    }
                    analisarPagina(uri, resposta.uri(), html)
                }
                else -> ResultadoVerificacao.Inconclusivo("HTTP $estado")
            }
        }
    }

    private fun analisarPagina(original: URI, final: URI, html: String): ResultadoVerificacao {
        // "Soft 404": muitas lojas redirecionam um produto retirado para a homepage com 200.
        val caminhoOriginal = original.path.orEmpty()
        val caminhoFinal = final.path.orEmpty()
        if (caminhoOriginal.length > 1 && (caminhoFinal.isEmpty() || caminhoFinal == "/")) {
            return ResultadoVerificacao.LinkInvalido("Redireciona para a página inicial")
        }

        return when (DisponibilidadeParser.analisar(html)) {
            Disponibilidade.SEM_STOCK -> ResultadoVerificacao.SemStock
            Disponibilidade.EM_STOCK -> ResultadoVerificacao.Disponivel(stockConfirmado = true)
            Disponibilidade.DESCONHECIDA -> ResultadoVerificacao.Disponivel(stockConfirmado = false)
        }
    }

    // No máximo 1 pedido por intervalo a cada host — tráfego mínimo para não incomodar os retalhistas.
    private fun esperarVez(host: String) {
        if (intervaloMs <= 0) return
        val espera = synchronized(ultimoPedidoPorHost) {
            val agora = System.currentTimeMillis()
            val proximo = (ultimoPedidoPorHost[host] ?: 0L) + intervaloMs
            val inicio = maxOf(agora, proximo)
            ultimoPedidoPorHost[host] = inicio
            inicio - agora
        }
        if (espera > 0) {
            try {
                Thread.sleep(espera)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
