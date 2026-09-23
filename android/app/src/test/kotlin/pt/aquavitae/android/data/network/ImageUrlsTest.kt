package pt.aquavitae.android.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlsTest {

    private val base = "http://10.0.2.2:8080/"

    @Test
    fun `um caminho relativo leva o endereco da API`() {
        assertEquals("http://10.0.2.2:8080/icones/avatares/casta-bago.svg", resolveImageUrl("icones/avatares/casta-bago.svg", base))
    }

    @Test
    fun `nao duplica nem perde a barra entre o endereco e o caminho`() {
        val esperado = "http://10.0.2.2:8080/icones/x.svg"
        assertEquals(esperado, resolveImageUrl("/icones/x.svg", base))
        assertEquals(esperado, resolveImageUrl("icones/x.svg", base.trimEnd('/')))
    }

    @Test
    fun `um URL absoluto usa-se tal como esta`() {
        assertEquals("https://loja.pt/img/vinho.jpg", resolveImageUrl("https://loja.pt/img/vinho.jpg", base))
        assertEquals("HTTP://loja.pt/a.png", resolveImageUrl("HTTP://loja.pt/a.png", base))
    }

    @Test
    fun `sem caminho nao ha URL`() {
        assertNull(resolveImageUrl(null, base))
        assertNull(resolveImageUrl("   ", base))
    }
}
