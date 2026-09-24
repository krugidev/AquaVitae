package pt.aquavitae.api.bebida

import java.text.Normalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PesquisaTextoTest {

    @Test
    fun `os dois mapas tem o mesmo tamanho porque o translate troca carater a carater`() {
        assertEquals(PesquisaTexto.COM_ACENTO.length, PesquisaTexto.SEM_ACENTO.length)
    }

    @Test
    fun `cada letra base do mapa e mesmo a base da acentuada`() {
        // Confere o mapa contra a decomposição Unicode: "Ã" decompõe-se em "A" + til, etc.
        PesquisaTexto.COM_ACENTO.forEachIndexed { i, acentuada ->
            val base = Normalizer.normalize(acentuada.toString(), Normalizer.Form.NFD).first()
            assertEquals(base, PesquisaTexto.SEM_ACENTO[i], "$acentuada deve mapear para $base")
        }
    }

    @Test
    fun `tira os acentos e poe em maiusculas`() {
        assertEquals("ESPORAO", PesquisaTexto.normalizar("Esporão"))
        assertEquals("ESPORAO", PesquisaTexto.normalizar("esporao"))
        assertEquals("VALE MEAO", PesquisaTexto.normalizar("Vale Meão"))
        assertEquals("BEIRAO", PesquisaTexto.normalizar("beirão"))
        assertEquals("SAO JOAO DA PESQUEIRA", PesquisaTexto.normalizar("São João da Pesqueira"))
        assertEquals("CHATEAU LAFITE ROTHSCHILD", PesquisaTexto.normalizar("Château Lafite Rothschild"))
        assertEquals("ANO 1990", PesquisaTexto.normalizar("Año 1990"))
    }

    @Test
    fun `nao mexe no que nao tem acento nem inventa letras`() {
        assertEquals("GIN 44°", PesquisaTexto.normalizar("gin 44°"))
        assertEquals("BARCA VELHA 2015", PesquisaTexto.normalizar("Barca Velha 2015"))
        // "ß" fica como está (o UPPER do Oracle não o expande para "SS", ao contrário de String.uppercase()).
        assertEquals("STRAßE", PesquisaTexto.normalizar("straße"))
        assertEquals("", PesquisaTexto.normalizar(""))
    }

    @Test
    fun `escapa o ponto de exclamacao e os curingas do like`() {
        assertEquals("100!%", PesquisaTexto.escaparLike("100%"))
        assertEquals("A!_B", PesquisaTexto.escaparLike("A_B"))
        assertEquals("!!", PesquisaTexto.escaparLike("!"))
        assertEquals("a!%!_!!z", PesquisaTexto.escaparLike("a%_!z"))
        assertEquals("sem curingas", PesquisaTexto.escaparLike("sem curingas"))
    }

    @Test
    fun `o padrao e contem sem acentos em maiusculas`() {
        assertEquals("%ESPORAO%", PesquisaTexto.padraoContem("Esporão"))
        assertEquals("%ESPORAO%", PesquisaTexto.padraoContem("  esporao  "))
    }

    @Test
    fun `um percentagem ou sublinhado escritos na pesquisa sao texto e nao curingas`() {
        assertEquals("%!%%", PesquisaTexto.padraoContem("%"))
        assertEquals("%!_%", PesquisaTexto.padraoContem("_"))
        assertEquals("%14,5!% VOL%", PesquisaTexto.padraoContem("14,5% vol"))
    }

    @Test
    fun `sem texto nao ha filtro`() {
        assertNull(PesquisaTexto.padraoContem(null))
        assertNull(PesquisaTexto.padraoContem(""))
        assertNull(PesquisaTexto.padraoContem("   "))
    }

    @Test
    fun `til e circunflexo sozinhos sao letras como as outras e nao dao erro`() {
        assertEquals("%~%", PesquisaTexto.padraoContem("~"))
        assertEquals("%^%", PesquisaTexto.padraoContem("^"))
        assertTrue(PesquisaTexto.padraoContem("~^`´")!!.startsWith("%"))
    }
}
