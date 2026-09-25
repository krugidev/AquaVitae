package pt.aquavitae.android.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {

    private val ok = Triple("ana_silva", "ana@exemplo.pt", "password123")

    private fun validar(username: String = ok.first, email: String = ok.second, password: String = ok.third) =
        validateRegister(username, email, password)

    @Test
    fun `dados validos nao tem problema`() {
        assertNull(validar())
    }

    @Test
    fun `username curto demais e recusado`() {
        val problema = validar(username = "ab")
        assertEquals(AuthField.Username, problema?.field)
    }

    @Test
    fun `username com 30 caracteres passa e com 31 nao`() {
        assertNull(validar(username = "a".repeat(30)))
        assertEquals(AuthField.Username, validar(username = "a".repeat(31))?.field)
    }

    @Test
    fun `username nao pode ter espacos nem arroba`() {
        assertEquals(AuthField.Username, validar(username = "ana silva")?.field)
        assertEquals(AuthField.Username, validar(username = "ana@silva")?.field)
    }

    @Test
    fun `emails invalidos sao recusados`() {
        for (email in listOf("", "ana", "ana@", "@exemplo.pt", "ana@exemplo", "ana @exemplo.pt", "ana@exemplo .pt")) {
            assertEquals("'$email'", AuthField.Email, validar(email = email)?.field)
        }
    }

    @Test
    fun `emails validos passam`() {
        for (email in listOf("ana@exemplo.pt", "ana.silva+vinhos@sub.exemplo.com", "A@B.CO")) {
            assertNull("'$email'", validar(email = email))
        }
    }

    @Test
    fun `password com menos de 8 caracteres e recusada`() {
        assertEquals(AuthField.Password, validar(password = "1234567")?.field)
        assertNull(validar(password = "12345678"))
    }

    @Test
    fun `password com mais de 72 caracteres e recusada`() {
        assertNull(validar(password = "a".repeat(72)))
        assertEquals(AuthField.Password, validar(password = "a".repeat(73))?.field)
    }

    @Test
    fun `devolve o primeiro problema por ordem dos campos`() {
        // username, email e password inválidos: o username vem primeiro
        assertEquals(AuthField.Username, validar(username = "a", email = "x", password = "1")?.field)
        assertEquals(AuthField.Email, validar(email = "x", password = "1")?.field)
    }

    @Test
    fun `a mensagem do problema esta preenchida`() {
        assertNotNull(validar(username = "a")?.message?.takeIf { it.isNotBlank() })
    }
}
