package pt.aquavitae.android.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarBadgeTest {

    @Test
    fun `nome e apelido dao as duas iniciais`() {
        assertEquals("AS", iniciaisDe("Ana", "Silva", "ana_silva"))
    }

    @Test
    fun `so o nome da so uma inicial`() {
        assertEquals("A", iniciaisDe("Ana", null, "ana_silva"))
        assertEquals("A", iniciaisDe("Ana", "", "ana_silva"))
    }

    @Test
    fun `sem nome usa as 2 primeiras letras do username`() {
        assertEquals("AN", iniciaisDe(null, null, "ana_silva"))
        assertEquals("AN", iniciaisDe(null, "Silva", "ana_silva"))
    }

    @Test
    fun `sem nada fica um ponto de interrogacao`() {
        assertEquals("?", iniciaisDe(null, null, null))
        assertEquals("?", iniciaisDe(null, null, ""))
    }

    @Test
    fun `fica sempre em maiusculas`() {
        assertEquals("AS", iniciaisDe("ana", "silva", null))
        assertEquals("DE", iniciaisDe(null, null, "demo_user2"))
    }

    @Test
    fun `ignora espacos nas pontas`() {
        assertEquals("AS", iniciaisDe(" Ana ", " Silva ", null))
    }
}
