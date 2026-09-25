package pt.aquavitae.api.auth

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordResetRegrasTest {

    private val agora = Instant.parse("2026-09-22T12:00:00Z")

    @Test
    fun `uma conta que nunca pediu pode pedir`() {
        assertTrue(podeGerarNovoCodigo(null, agora, 60))
    }

    @Test
    fun `um pedido dentro do intervalo minimo e travado`() {
        assertFalse(podeGerarNovoCodigo(agora.minusSeconds(10), agora, 60))
        assertFalse(podeGerarNovoCodigo(agora.minusSeconds(59), agora, 60))
        // No mesmo instante (dois toques seguidos) também.
        assertFalse(podeGerarNovoCodigo(agora, agora, 60))
    }

    @Test
    fun `passado o intervalo minimo pode pedir de novo`() {
        assertTrue(podeGerarNovoCodigo(agora.minusSeconds(60), agora, 60))
        assertTrue(podeGerarNovoCodigo(agora.minusSeconds(3600), agora, 60))
    }
}
