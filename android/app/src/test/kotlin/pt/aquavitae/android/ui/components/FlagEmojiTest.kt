package pt.aquavitae.android.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlagEmojiTest {

    @Test
    fun `PT vira a bandeira de Portugal`() {
        // U+1F1F5 U+1F1F9 (P e T em "indicador regional") — o Android desenha o par como 🇵🇹.
        assertEquals("🇵🇹", flagEmoji("PT"))
    }

    @Test
    fun `minusculas e espacos sao aceites`() {
        assertEquals(flagEmoji("GB"), flagEmoji(" gb "))
    }

    @Test
    fun `codigos invalidos nao dao bandeira`() {
        assertNull(flagEmoji(null))
        assertNull(flagEmoji(""))
        assertNull(flagEmoji("P"))
        assertNull(flagEmoji("POR"))
        assertNull(flagEmoji("P1"))
    }
}
