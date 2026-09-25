package pt.aquavitae.api.utilizador

import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TermosRegrasTest {

    private val publicacao = Instant.parse("2026-10-01T00:00:00Z")
    private val lisboa = ZoneId.of("Europe/Lisbon")

    @Test
    fun `quem nunca aceitou precisa de aceitar`() {
        assertTrue(precisaAceitarTermos(aceitesEm = null, emVigorDesde = null))
        assertTrue(precisaAceitarTermos(aceitesEm = null, emVigorDesde = publicacao))
    }

    @Test
    fun `quem aceitou nao precisa de voltar a aceitar se os termos nao mudaram`() {
        assertFalse(precisaAceitarTermos(aceitesEm = Instant.parse("2026-09-21T10:00:00Z"), emVigorDesde = null))
    }

    @Test
    fun `quem aceitou antes da publicacao dos termos em vigor volta a aceitar`() {
        assertTrue(precisaAceitarTermos(aceitesEm = Instant.parse("2026-09-30T23:59:59Z"), emVigorDesde = publicacao))
    }

    @Test
    fun `quem aceitou depois da publicacao nao volta a aceitar`() {
        assertFalse(precisaAceitarTermos(aceitesEm = Instant.parse("2026-10-01T00:00:01Z"), emVigorDesde = publicacao))
    }

    @Test
    fun `aceitar no instante exato da publicacao conta como aceite`() {
        assertFalse(precisaAceitarTermos(aceitesEm = publicacao, emVigorDesde = publicacao))
    }

    @Test
    fun `a data em vigor conta a partir do inicio do dia em Portugal`() {
        // Verão (UTC+1): a meia-noite de Lisboa é uma hora antes, em UTC. Inverno (UTC+0): coincide.
        assertEquals(Instant.parse("2026-09-30T23:00:00Z"), emVigorDesdeInstant("2026-10-01", lisboa))
        assertEquals(Instant.parse("2026-12-01T00:00:00Z"), emVigorDesdeInstant("2026-12-01", lisboa))
    }

    @Test
    fun `valor vazio ou em branco nao define data`() {
        assertNull(emVigorDesdeInstant("", lisboa))
        assertNull(emVigorDesdeInstant("   ", lisboa))
    }

    @Test
    fun `espacos a volta da data sao ignorados`() {
        assertEquals(Instant.parse("2026-12-01T00:00:00Z"), emVigorDesdeInstant("  2026-12-01 ", lisboa))
    }

    @Test
    fun `data mal escrita rebenta em vez de ser ignorada`() {
        assertFailsWith<DateTimeException> { emVigorDesdeInstant("01/10/2026", lisboa) }
        assertFailsWith<DateTimeException> { emVigorDesdeInstant("amanha", lisboa) }
    }
}
