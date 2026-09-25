package pt.aquavitae.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

private val AGORA: Instant = Instant.parse("2026-09-24T12:00:00Z")

private fun oferta(
    id: Long,
    preco: Double? = 10.0,
    disponivel: Boolean = true,
    url: String? = "https://loja.exemplo/produto",
    atualizadoEm: String? = null,
) = OfertaCompra(
    id = id,
    retalhistaNome = "Loja $id",
    retalhistaLogo = null,
    url = url,
    preco = preco,
    precoAtualizadoEm = atualizadoEm,
    disponivel = disponivel,
    motivoIndisponivel = if (disponivel) null else "Sem stock",
    indisponivelDesde = null,
)

class CompraFormatacaoTest {

    @Test
    fun `a oferta principal e a mais barata das disponiveis`() {
        val ofertas = listOf(oferta(1, 12.5), oferta(2, 9.9), oferta(3, 11.0))
        assertEquals(2L, ofertas.maisBarataDisponivel()?.id)
    }

    @Test
    fun `as indisponiveis nunca sao a oferta principal mesmo sendo mais baratas`() {
        val ofertas = listOf(oferta(1, 5.0, disponivel = false), oferta(2, 20.0))
        assertEquals(2L, ofertas.maisBarataDisponivel()?.id)
    }

    @Test
    fun `sem link nao ha botao de comprar`() {
        val ofertas = listOf(oferta(1, 5.0, url = null), oferta(2, 6.0, url = "   "), oferta(3, 20.0))
        assertEquals(3L, ofertas.maisBarataDisponivel()?.id)
        assertNull(listOf(oferta(1, url = null)).maisBarataDisponivel())
    }

    @Test
    fun `sem nenhuma oferta disponivel nao ha oferta principal`() {
        assertNull(emptyList<OfertaCompra>().maisBarataDisponivel())
        assertNull(listOf(oferta(1, disponivel = false), oferta(2, disponivel = false)).maisBarataDisponivel())
    }

    @Test
    fun `uma oferta sem preco fica atras das que tem preco`() {
        val ofertas = listOf(oferta(1, preco = null), oferta(2, 30.0))
        assertEquals(2L, ofertas.maisBarataDisponivel()?.id)
    }

    @Test
    fun `so ha comparacao com mais de uma oferta disponivel`() {
        assertFalse(listOf(oferta(1)).temComparacao)
        assertFalse(listOf(oferta(1), oferta(2, disponivel = false)).temComparacao)
        assertTrue(listOf(oferta(1), oferta(2)).temComparacao)
    }

    @Test
    fun `ha quanto tempo em portugues`() {
        fun ha(segundos: Long) = textoHa(AGORA.minusSeconds(segundos).toString(), AGORA)
        assertEquals("agora mesmo", ha(20))
        assertEquals("há 5 min", ha(5 * 60))
        assertEquals("há 1 hora", ha(3600))
        assertEquals("há 5 horas", ha(5 * 3600))
        assertEquals("ontem", ha(30 * 3600))
        assertEquals("há 3 dias", ha(3 * 86400))
        assertEquals("há 3 semanas", ha(22 * 86400))
        assertEquals("há 3 meses", ha(95 * 86400))
    }

    @Test
    fun `uma data invalida ou em falta nao da texto`() {
        assertNull(textoHa(null, AGORA))
        assertNull(textoHa("ontem a noite", AGORA))
    }

    @Test
    fun `o futuro conta como agora mesmo e nunca da negativo`() {
        assertEquals("agora mesmo", textoHa(AGORA.plusSeconds(600).toString(), AGORA))
    }

    @Test
    fun `atualizado hoje ou ha dias`() {
        assertEquals("atualizado hoje", oferta(1, atualizadoEm = AGORA.minusSeconds(3600).toString()).atualizadoTexto(AGORA))
        assertEquals("atualizado há 3 dias", oferta(1, atualizadoEm = AGORA.minusSeconds(3 * 86400).toString()).atualizadoTexto(AGORA))
        assertNull(oferta(1, atualizadoEm = null).atualizadoTexto(AGORA))
    }
}
