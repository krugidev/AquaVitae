package pt.aquavitae.api.admin

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ProdutorValidacaoTest {

    private val anoAtual = 2026

    private fun valido(form: ProdutorFormulario): ProdutorDados =
        when (val r = ProdutorValidacao.validar(form, anoAtual)) {
            is ValidacaoProdutor.Valido -> r.dados
            is ValidacaoProdutor.Invalido -> fail("devia ser válido, mas: ${r.erros}")
        }

    private fun erros(form: ProdutorFormulario): Map<String, String> =
        when (val r = ProdutorValidacao.validar(form, anoAtual)) {
            is ValidacaoProdutor.Valido -> fail("devia ser inválido")
            is ValidacaoProdutor.Invalido -> r.erros
        }

    private val base = ProdutorFormulario(nome = "Herdade do Esporão", paisId = "1")

    @Test
    fun `so o nome e o pais sao obrigatorios`() {
        val d = valido(base)
        assertEquals("Herdade do Esporão", d.nome)
        assertEquals(1L, d.paisId)
        assertNull(d.regiaoId)
        assertNull(d.anoFundacao)
        assertNull(d.website)
        assertNull(d.imagem)
        assertNull(d.morada)
        assertNull(d.latitude)
        assertNull(d.historia)
        assertEquals(false, d.permiteVisitas)
    }

    @Test
    fun `o nome e o pais em falta sao erros`() {
        val e = erros(ProdutorFormulario())
        assertEquals(setOf("nome", "paisId"), e.keys)
        assertTrue(erros(base.copy(nome = "   ")).containsKey("nome"))
        assertTrue(erros(base.copy(paisId = "abc")).containsKey("paisId"))
    }

    @Test
    fun `o nome perde os espacos a mais e tem um limite`() {
        assertEquals("Quinta do Vale Meão", valido(base.copy(nome = "  Quinta   do  Vale\tMeão ")).nome)
        assertTrue(erros(base.copy(nome = "a".repeat(151))).containsKey("nome"))
        assertEquals(150, valido(base.copy(nome = "a".repeat(150))).nome.length)
    }

    @Test
    fun `a regiao e opcional mas tem de ser um numero`() {
        assertEquals(12L, valido(base.copy(regiaoId = "12")).regiaoId)
        assertNull(valido(base.copy(regiaoId = "  ")).regiaoId)
        assertTrue(erros(base.copy(regiaoId = "douro")).containsKey("regiaoId"))
    }

    @Test
    fun `o ano de fundacao tem de ser plausivel`() {
        assertEquals(1973, valido(base.copy(anoFundacao = " 1973 ")).anoFundacao)
        assertEquals(anoAtual, valido(base.copy(anoFundacao = "$anoAtual")).anoFundacao)
        assertTrue(erros(base.copy(anoFundacao = "${anoAtual + 1}")).containsKey("anoFundacao"))
        assertTrue(erros(base.copy(anoFundacao = "999")).containsKey("anoFundacao"))
        assertTrue(erros(base.copy(anoFundacao = "19x3")).containsKey("anoFundacao"))
    }

    @Test
    fun `o website ganha o esquema que faltar e recusa o que nao e http`() {
        assertEquals("https://www.esporao.com", valido(base.copy(website = "www.esporao.com")).website)
        assertEquals("http://esporao.com/pt", valido(base.copy(website = " http://esporao.com/pt ")).website)
        assertTrue(erros(base.copy(website = "javascript:alert(1)")).containsKey("website"))
        assertTrue(erros(base.copy(website = "ftp://esporao.com")).containsKey("website"))
        assertTrue(erros(base.copy(website = "data://x.pt/a")).containsKey("website"))
        assertTrue(erros(base.copy(website = "dois espaços.pt")).containsKey("website"))
        assertTrue(erros(base.copy(website = "semponto")).containsKey("website"))
        assertTrue(erros(base.copy(website = "https://" + "a".repeat(250) + ".pt")).containsKey("website"))
    }

    @Test
    fun `a imagem e um url absoluto ou um caminho do backend`() {
        assertEquals("https://cdn.exemplo.pt/i.jpg", valido(base.copy(imagem = "https://cdn.exemplo.pt/i.jpg")).imagem)
        assertEquals("/icones/x.svg", valido(base.copy(imagem = "/icones/x.svg")).imagem)
        assertTrue(erros(base.copy(imagem = "cdn.exemplo.pt/i.jpg")).containsKey("imagem"), "sem esquema não é um URL")
        assertTrue(erros(base.copy(imagem = "//cdn.exemplo.pt/i.jpg")).containsKey("imagem"))
        assertTrue(erros(base.copy(imagem = "javascript:alert(1)")).containsKey("imagem"))
        assertTrue(erros(base.copy(imagem = "https://a.pt/" + "x".repeat(250))).containsKey("imagem"), "a coluna tem 255")
    }

    @Test
    fun `as coordenadas aceitam virgula e ponto e vao com 6 casas decimais`() {
        val d = valido(base.copy(latitude = "41,1621", longitude = "-7.7891"))
        assertEquals(BigDecimal("41.162100"), d.latitude)
        assertEquals(BigDecimal("-7.789100"), d.longitude)
    }

    @Test
    fun `as coordenadas vao as duas ou nenhuma e dentro dos limites`() {
        assertTrue(erros(base.copy(latitude = "41,1")).containsKey("longitude"))
        assertTrue(erros(base.copy(longitude = "-7,7")).containsKey("latitude"))
        assertTrue(erros(base.copy(latitude = "91", longitude = "0")).containsKey("latitude"))
        assertTrue(erros(base.copy(latitude = "0", longitude = "-181")).containsKey("longitude"))
        assertTrue(erros(base.copy(latitude = "abc", longitude = "0")).containsKey("latitude"))
        assertTrue(erros(base.copy(latitude = "1e5", longitude = "0")).containsKey("latitude"), "sem notação científica")
        valido(base.copy(latitude = "-90", longitude = "180"))
    }

    @Test
    fun `a historia mantem as linhas, perde os espacos das pontas e tem um limite`() {
        assertEquals("Primeira.\n\nSegunda.", valido(base.copy(historia = "  Primeira.\r\n\r\nSegunda.  \r\n")).historia)
        assertNull(valido(base.copy(historia = " \r\n ")).historia)
        assertTrue(erros(base.copy(historia = "a".repeat(HISTORIA_MAX + 1))).containsKey("historia"))
    }

    @Test
    fun `a morada tem um limite`() {
        assertTrue(erros(base.copy(morada = "a".repeat(301))).containsKey("morada"))
        assertEquals("Rua Direita 12, Régua", valido(base.copy(morada = " Rua  Direita 12,   Régua ")).morada)
    }

    @Test
    fun `os erros vem todos de uma vez`() {
        val e = erros(ProdutorFormulario(nome = "", paisId = "", anoFundacao = "x", website = "??", latitude = "9"))
        assertTrue(e.keys.containsAll(listOf("nome", "paisId", "anoFundacao", "website", "longitude")), "$e")
    }
}
