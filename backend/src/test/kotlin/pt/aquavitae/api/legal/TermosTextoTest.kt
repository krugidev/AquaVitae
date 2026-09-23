package pt.aquavitae.api.legal

import org.springframework.core.io.ClassPathResource
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TermosTextoTest {

    @Test
    fun `titulos e paragrafos separam-se pelas linhas em branco`() {
        val blocos = TermosTexto.analisar(
            """
            # 1. Aceitação

            Primeiro parágrafo.

            Segundo parágrafo.

            # 2. Conta

            Terceiro.
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                TermosBloco("titulo", "1. Aceitação"),
                TermosBloco("paragrafo", "Primeiro parágrafo."),
                TermosBloco("paragrafo", "Segundo parágrafo."),
                TermosBloco("titulo", "2. Conta"),
                TermosBloco("paragrafo", "Terceiro."),
            ),
            blocos,
        )
    }

    @Test
    fun `as quebras de linha dentro de um paragrafo contam como espacos`() {
        val blocos = TermosTexto.analisar("uma linha\noutra linha\r\nterceira")
        assertEquals(listOf(TermosBloco("paragrafo", "uma linha outra linha terceira")), blocos)
    }

    @Test
    fun `um titulo colado ao paragrafo seguinte tambem se separa`() {
        val blocos = TermosTexto.analisar("# Título\nTexto logo a seguir.")
        assertEquals(listOf(TermosBloco("titulo", "Título"), TermosBloco("paragrafo", "Texto logo a seguir.")), blocos)
    }

    @Test
    fun `um cardinal sem espaco nao e um titulo`() {
        assertEquals(listOf(TermosBloco("paragrafo", "#semespaco")), TermosTexto.analisar("#semespaco"))
    }

    @Test
    fun `os acentos ficam como estao na pagina html`() {
        val html = TermosTexto.paraHtml("Termos e Condições", listOf(TermosBloco("paragrafo", "Aceitação")))
        assertContains(html, "<h1>Termos e Condições</h1>")
        assertContains(html, "<p>Aceitação</p>")
    }

    @Test
    fun `texto vazio nao da blocos`() {
        assertTrue(TermosTexto.analisar("").isEmpty())
        assertTrue(TermosTexto.analisar("\n\n   \n").isEmpty())
    }

    @Test
    fun `a pagina html escapa o texto`() {
        val html = TermosTexto.paraHtml("Termos", listOf(TermosBloco("titulo", "<b>x</b>"), TermosBloco("paragrafo", "a & b <script>")))
        assertFalse(html.contains("<script>"))
        assertContains(html, "<h2>&lt;b&gt;x&lt;/b&gt;</h2>")
        assertContains(html, "<p>a &amp; b &lt;script&gt;</p>")
    }

    // O ficheiro que vai mesmo para a app tem de estar num formato que o analisador percebe.
    @Test
    fun `o ficheiro dos termos existe e tem titulos e paragrafos`() {
        val texto = ClassPathResource("legal/termos.txt").inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val blocos = TermosTexto.analisar(texto)
        assertTrue(blocos.any { it.tipo == TermosTexto.TIPO_TITULO })
        assertTrue(blocos.count { it.tipo == TermosTexto.TIPO_PARAGRAFO } >= 2)
    }
}
