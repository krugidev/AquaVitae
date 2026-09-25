package pt.aquavitae.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextoSemAcentosTest {

    @Test
    fun `tira til agudo circunflexo cedilha e trema`() {
        assertEquals("Esporao", "Esporão".semAcentos())
        assertEquals("Alvarinho Ines", "Alvarinho Inês".semAcentos())
        assertEquals("Chateau Lafite", "Château Lafite".semAcentos())
        assertEquals("Aguardente Sao Joao", "Aguardente São João".semAcentos())
        assertEquals("Cacao", "Cacaó".semAcentos())
        assertEquals("Muller Thurgau", "Müller Thurgau".semAcentos())
        assertEquals("Ca Ca", "Çá Cã".semAcentos())
    }

    @Test
    fun `nao mexe no que nao tem acento`() {
        assertEquals("Touriga Nacional", "Touriga Nacional".semAcentos())
        assertEquals("Gin 44°", "Gin 44°".semAcentos())
        assertEquals("", "".semAcentos())
    }

    @Test
    fun `encontra com ou sem acento e com qualquer maiuscula`() {
        assertTrue("Tinta Roriz (Aragonez)".contemSemAcentos("aragonez"))
        assertTrue("Fernão Pires".contemSemAcentos("fernao"))
        assertTrue("Fernão Pires".contemSemAcentos("FERNÃO"))
        assertTrue("Fernão Pires".contemSemAcentos("pires"))
        assertTrue("Fernao Pires".contemSemAcentos("fernão"))
    }

    @Test
    fun `nao encontra o que nao esta la`() {
        assertFalse("Touriga Nacional".contemSemAcentos("franca"))
        assertFalse("Alvarinho".contemSemAcentos("avarinho2"))
    }

    @Test
    fun `uma pesquisa em branco encontra tudo como o contains normal`() {
        assertTrue("Touriga".contemSemAcentos(""))
    }
}
