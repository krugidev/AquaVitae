package pt.aquavitae.api.admin

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdminModelosTest {

    private fun bebida(
        nome: String? = "Vale Meão",
        categoria: String? = "Vinho",
        produtor: String? = "Quinta do Vale Meão",
        pais: String? = "Portugal",
        ean: String? = "5601234567890",
        imagem: String? = "https://exemplo.pt/a.jpg",
        teor: BigDecimal? = BigDecimal("14.0"),
        volume: BigDecimal? = BigDecimal("750"),
        links: Long = 1,
    ) = BebidaAdminLinha(1, nome, categoria, produtor, pais, ean, imagem, 2016, teor, volume, links)

    @Test
    fun `uma bebida completa nao tem problemas`() {
        assertTrue(bebida().problemas.isEmpty())
    }

    @Test
    fun `os problemas da bebida seguem os criterios dos filtros`() {
        assertEquals(listOf("sem imagem"), bebida(imagem = null).problemas)
        assertEquals(listOf("sem imagem"), bebida(imagem = "   ").problemas)
        assertEquals(listOf("sem produtor"), bebida(produtor = null).problemas)
        assertEquals(listOf("sem EAN"), bebida(ean = null).problemas)
        assertEquals(listOf("sem link ativo"), bebida(links = 0).problemas)
        assertEquals(listOf("dados em falta"), bebida(pais = null).problemas)
        assertEquals(listOf("dados em falta"), bebida(teor = null).problemas)
        assertEquals(listOf("dados em falta"), bebida(volume = null).problemas)
        assertEquals(listOf("dados em falta"), bebida(categoria = null).problemas)
    }

    @Test
    fun `os detalhes usam a virgula decimal e nao mostram zeros a mais`() {
        assertEquals("2016 · 14% · 750 ml", bebida().detalhes)
        assertEquals("2016 · 13,5% · 700 ml", bebida(teor = BigDecimal("13.50"), volume = BigDecimal("700")).detalhes)
        assertEquals("2016", bebida(teor = null, volume = null).detalhes)
    }

    @Test
    fun `os problemas do produtor`() {
        val completo = ProdutorAdminLinha(1, "Esporão", "Portugal", "Alentejo", 1973, "https://x.pt", "https://x.pt/i.jpg", "Rua", BigDecimal("38.5"), BigDecimal("-7.5"), 1, 3)
        assertTrue(completo.problemas.isEmpty())
        val vazio = ProdutorAdminLinha(2, "Novo", "Portugal", null, null, null, null, null, null, null, 0, 0)
        assertEquals(listOf("sem imagem", "sem história", "sem morada", "sem coordenadas", "sem região", "sem bebidas"), vazio.problemas)
        assertEquals(listOf("sem coordenadas"), completo.copy(longitude = null).problemas)
    }

    @Test
    fun `o url da imagem e absoluto ou o caminho de um recurso estatico`() {
        assertEquals("https://loja.pt/a.jpg", urlDeImagem(" https://loja.pt/a.jpg "))
        assertEquals("/icones/x.svg", urlDeImagem("/icones/x.svg"))
        assertEquals("/icones/x.svg", urlDeImagem("icones/x.svg"))
        assertNull(urlDeImagem(null))
        assertNull(urlDeImagem("  "))
    }

    @Test
    fun `os codigos dos filtros conhecem-se e um codigo desconhecido e ignorado`() {
        assertEquals(QualidadeBebida.SEM_EAN, QualidadeBebida.deCodigo("sem-ean"))
        assertNull(QualidadeBebida.deCodigo("qualquer-coisa"))
        assertNull(QualidadeBebida.deCodigo(""))
        assertEquals(QualidadeProdutor.SEM_HISTORIA, QualidadeProdutor.deCodigo("sem-historia"))
        assertEquals(OrdemBebidas.RECENTES, OrdemBebidas.deCodigo("lixo"))
        assertEquals(OrdemProdutores.NOME, OrdemProdutores.deCodigo(null))
    }

    @Test
    fun `a paginacao sabe se ha anterior e seguinte`() {
        val meio = PaginaAdmin(listOf(1), pagina = 1, totalPaginas = 3, total = 60)
        assertTrue(meio.temAnterior && meio.temSeguinte)
        assertEquals(2, meio.paginaParaMostrar)
        val primeira = PaginaAdmin(listOf(1), pagina = 0, totalPaginas = 1, total = 1)
        assertTrue(!primeira.temAnterior && !primeira.temSeguinte)
    }
}
