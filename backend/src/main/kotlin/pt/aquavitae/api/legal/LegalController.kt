package pt.aquavitae.api.legal

import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Os termos e condições, públicos (têm de se poder ler antes de haver conta, no popup do registo). O texto está em
 * `src/main/resources/legal/termos.txt` (formato em [TermosTexto]); lê-se uma vez, ao 1.º pedido.
 */
@RestController
class LegalController {

    private val termos: TermosDto by lazy {
        val texto = ClassPathResource("legal/termos.txt").inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        TermosDto(TermosTexto.TITULO, TermosTexto.analisar(texto))
    }

    private val termosHtml: String by lazy { TermosTexto.paraHtml(termos.titulo, termos.blocos) }

    // O que a app mostra no popup dos termos.
    @GetMapping("/api/legal/termos")
    fun termos(): TermosDto = termos

    // A página pública (a mesma fonte de texto), para quem quiser ler no browser ou para ligar a partir de um site/loja.
    @GetMapping("/legal/termos.html", produces = ["text/html;charset=UTF-8"])
    fun termosHtml(): String = termosHtml
}
