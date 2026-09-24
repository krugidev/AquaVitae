package pt.aquavitae.api.compra

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliquePerguntaTest {

    private val agora = Instant.parse("2026-09-24T12:00:00Z")

    @Test
    fun `a janela vai de 72 horas atras ate 2 minutos atras`() {
        val (desde, ate) = CliquePergunta.janela(agora, aposMinutos = 2, ateHoras = 72)
        assertEquals(Instant.parse("2026-09-21T12:00:00Z"), desde)
        assertEquals(Instant.parse("2026-09-24T11:58:00Z"), ate)
    }

    @Test
    fun `um clique de ha segundos fica de fora e um de ha uma hora entra`() {
        val (desde, ate) = CliquePergunta.janela(agora, aposMinutos = 2, ateHoras = 72)
        fun elegivel(clique: Instant) = clique in desde..ate
        assertFalse(elegivel(agora.minusSeconds(30)))
        assertFalse(elegivel(agora.minusSeconds(119)))
        assertTrue(elegivel(agora.minusSeconds(121)))
        assertTrue(elegivel(agora.minusSeconds(3600)))
        assertFalse(elegivel(agora.minusSeconds(73 * 3600)))
    }

    @Test
    fun `os valores da janela sao configuraveis`() {
        val (desde, ate) = CliquePergunta.janela(agora, aposMinutos = 10, ateHoras = 24)
        assertEquals(Instant.parse("2026-09-23T12:00:00Z"), desde)
        assertEquals(Instant.parse("2026-09-24T11:50:00Z"), ate)
    }
}
