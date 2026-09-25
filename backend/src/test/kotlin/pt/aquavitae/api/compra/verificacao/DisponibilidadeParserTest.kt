package pt.aquavitae.api.compra.verificacao

import kotlin.test.Test
import kotlin.test.assertEquals

class DisponibilidadeParserTest {

    private fun jsonLd(json: String) =
        """<html><head><script type="application/ld+json">$json</script></head><body></body></html>"""

    @Test
    fun `produto em stock`() {
        val html = jsonLd(
            """{"@context":"https://schema.org","@type":"Product","name":"Vinho",
               "offers":{"@type":"Offer","price":"12.5","availability":"https://schema.org/InStock"}}""",
        )
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `produto sem stock`() {
        val html = jsonLd(
            """{"@type":"Product","offers":{"@type":"Offer","availability":"http://schema.org/OutOfStock"}}""",
        )
        assertEquals(Disponibilidade.SEM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `produto dentro de @graph`() {
        val html = jsonLd(
            """{"@context":"https://schema.org","@graph":[{"@type":"WebSite","name":"Loja"},
               {"@type":"Product","offers":{"availability":"https://schema.org/OutOfStock"}}]}""",
        )
        assertEquals(Disponibilidade.SEM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `tipo como lista`() {
        val html = jsonLd(
            """{"@type":["Product","Wine"],"offers":{"availability":"https://schema.org/InStock"}}""",
        )
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `varias ofertas, basta uma em stock`() {
        val html = jsonLd(
            """{"@type":"Product","offers":[{"availability":"https://schema.org/OutOfStock"},
               {"availability":"https://schema.org/InStock"}]}""",
        )
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `availability como objeto com @id`() {
        val html = jsonLd("""{"@type":"Product","offers":{"availability":{"@id":"https://schema.org/InStock"}}}""")
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `pre-encomenda conta como disponivel`() {
        val html = jsonLd("""{"@type":"Product","offers":{"availability":"https://schema.org/PreOrder"}}""")
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `so conta o primeiro produto, nao os relacionados`() {
        val html = jsonLd(
            """[{"@type":"Product","offers":{"availability":"https://schema.org/InStock"}},
               {"@type":"Product","offers":{"availability":"https://schema.org/OutOfStock"}}]""",
        )
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar(html))
    }

    @Test
    fun `sem dados estruturados nao se assume sem stock`() {
        assertEquals(Disponibilidade.DESCONHECIDA, DisponibilidadeParser.analisar("<html><body>Olá</body></html>"))
    }

    @Test
    fun `json-ld mal formado e ignorado`() {
        assertEquals(Disponibilidade.DESCONHECIDA, DisponibilidadeParser.analisar(jsonLd("""{"@type":"Product",,}""")))
    }

    @Test
    fun `fallback para meta tag, com atributos em qualquer ordem`() {
        val esgotado = """<meta content="out of stock" property="product:availability">"""
        val emStock = """<meta property="og:availability" content="instock" />"""
        assertEquals(Disponibilidade.SEM_STOCK, DisponibilidadeParser.analisar("<head>$esgotado</head>"))
        assertEquals(Disponibilidade.EM_STOCK, DisponibilidadeParser.analisar("<head>$emStock</head>"))
    }
}
