package pt.aquavitae.android.feature.recovery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryRulesTest {

    @Test
    fun `um identificador com arroba e um email e sem arroba e um username`() {
        assertTrue(looksLikeEmail("ana@exemplo.pt"))
        assertFalse(looksLikeEmail("ana_silva"))
    }

    @Test
    fun `o email fica tapado menos o fim do nome e o dominio`() {
        assertEquals("******user@gmail.com", maskEmail("demouser@gmail.com"))
        assertEquals("******ol@exemplo.pt", maskEmail("carol@exemplo.pt"))
    }

    @Test
    fun `nomes curtos mostram pouco ou nada do nome`() {
        assertEquals("******a@x.pt", maskEmail("ana@x.pt"))
        assertEquals("******@x.pt", maskEmail("a@x.pt"))
    }

    @Test
    fun `mostra no maximo 4 caracteres do fim do nome`() {
        assertEquals("******rido@exemplo.pt", maskEmail("nomemuitocomprido@exemplo.pt"))
    }

    @Test
    fun `texto sem arroba nao e alterado`() {
        assertEquals("ana_silva", maskEmail("ana_silva"))
    }

    @Test
    fun `password valida nao tem problema`() {
        assertNull(validateNewPassword("password123", "password123"))
    }

    @Test
    fun `password curta e recusada no campo da nova password`() {
        val problema = validateNewPassword("curta", "curta")
        assertEquals(RecoveryField.NovaPassword, problema?.field)
    }

    @Test
    fun `password com 8 caracteres passa e com 72 tambem mas 73 nao`() {
        assertNull(validateNewPassword("a".repeat(8), "a".repeat(8)))
        assertNull(validateNewPassword("a".repeat(72), "a".repeat(72)))
        assertEquals(RecoveryField.NovaPassword, validateNewPassword("a".repeat(73), "a".repeat(73))?.field)
    }

    @Test
    fun `repeticao diferente e recusada no campo da repeticao`() {
        val problema = validateNewPassword("password123", "password124")
        assertEquals(RecoveryField.RepetirPassword, problema?.field)
    }

    @Test
    fun `o codigo tem 6 digitos`() {
        assertEquals(6, CODE_LENGTH)
    }
}
