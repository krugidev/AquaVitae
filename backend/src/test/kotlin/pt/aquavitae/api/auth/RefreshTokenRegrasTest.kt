package pt.aquavitae.api.auth

import pt.aquavitae.api.auth.RefreshTokenRegras.Estado
import pt.aquavitae.api.auth.RefreshTokenRegras.Motivo
import java.security.SecureRandom
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RefreshTokenRegrasTest {

    private val agora = Instant.parse("2026-09-24T12:00:00Z")
    private val depois = agora.plusSeconds(86_400)
    private val tolerancia = 60L

    private fun avaliar(expiraEm: Instant = depois, revogadoEm: Instant? = null, motivo: Motivo? = null) =
        RefreshTokenRegras.avaliar(expiraEm, revogadoEm, motivo, agora, tolerancia)

    @Test
    fun `um token por usar e dentro da validade e valido`() {
        assertEquals(Estado.VALIDO, avaliar())
    }

    @Test
    fun `passada a validade esta expirado mesmo sem ter sido revogado`() {
        assertEquals(Estado.EXPIRADO, avaliar(expiraEm = agora.minusSeconds(1)))
        // No instante exato da expiração já conta como expirado.
        assertEquals(Estado.EXPIRADO, avaliar(expiraEm = agora))
    }

    @Test
    fun `um token rodado ha muito tempo e reutilizacao`() {
        assertEquals(Estado.REVOGADO, avaliar(revogadoEm = agora.minusSeconds(3600), motivo = Motivo.ROTACAO))
    }

    @Test
    fun `um token rodado ha poucos segundos ainda se aceita porque a resposta pode ter-se perdido`() {
        assertEquals(Estado.VALIDO, avaliar(revogadoEm = agora.minusSeconds(5), motivo = Motivo.ROTACAO))
        assertEquals(Estado.VALIDO, avaliar(revogadoEm = agora.minusSeconds(60), motivo = Motivo.ROTACAO))
        assertEquals(Estado.REVOGADO, avaliar(revogadoEm = agora.minusSeconds(61), motivo = Motivo.ROTACAO))
    }

    @Test
    fun `a tolerancia so vale para a rotacao e nunca para uma revogacao em massa`() {
        // Apanhado ao testar ao vivo: com a tolerância para todos os motivos, uma revogação em massa (reutilização, password nova)
        // ainda deixava usar os tokens durante 60 segundos.
        listOf(Motivo.LOGOUT, Motivo.PASSWORD, Motivo.REUTILIZACAO).forEach { motivo ->
            assertEquals(Estado.REVOGADO, avaliar(revogadoEm = agora, motivo = motivo), "$motivo revogado agora mesmo")
            assertEquals(Estado.REVOGADO, avaliar(revogadoEm = agora.minusSeconds(5), motivo = motivo), "$motivo revogado há 5 s")
        }
    }

    @Test
    fun `um token revogado sem motivo conhecido nao tem tolerancia`() {
        assertEquals(Estado.REVOGADO, avaliar(revogadoEm = agora.minusSeconds(5), motivo = null))
    }

    @Test
    fun `a tolerancia nunca ressuscita um token expirado`() {
        assertEquals(Estado.EXPIRADO, avaliar(expiraEm = agora.minusSeconds(1), revogadoEm = agora.minusSeconds(5), motivo = Motivo.ROTACAO))
    }

    @Test
    fun `os tokens gerados tem 43 caracteres url safe e nao se repetem`() {
        val random = SecureRandom()
        val tokens = (1..200).map { RefreshTokenRegras.gerar(random) }
        assertEquals(200, tokens.toSet().size)
        tokens.forEach { token ->
            assertEquals(43, token.length)
            assertTrue(token.all { it.isLetterOrDigit() || it == '-' || it == '_' }, "só Base64 URL: $token")
        }
    }

    @Test
    fun `o hash e sha256 em hexadecimal e deterministico`() {
        // SHA-256("abc") — vetor de teste conhecido.
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", RefreshTokenRegras.hash("abc"))
        assertEquals(RefreshTokenRegras.hash("x"), RefreshTokenRegras.hash("x"))
        assertNotEquals(RefreshTokenRegras.hash("x"), RefreshTokenRegras.hash("y"))
        assertEquals(64, RefreshTokenRegras.hash("qualquer coisa").length)
    }
}
