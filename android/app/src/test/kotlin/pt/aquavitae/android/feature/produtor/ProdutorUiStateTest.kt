package pt.aquavitae.android.feature.produtor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.ProdutorDetail

private fun produtor(totalProdutos: Int) = ProdutorDetail(
    id = 1,
    nome = "Casa Ferreirinha",
    paisNome = "Portugal",
    regiaoId = null,
    regiao = null,
    historia = null,
    anoFundacao = null,
    website = null,
    imagePath = null,
    latitude = null,
    longitude = null,
    permiteVisitas = false,
    totalProdutos = totalProdutos,
)

private fun bebidas(n: Int) = (1..n).map {
    BebidaSummary(id = it.toLong(), nome = "Bebida $it", categoriaNome = "Vinho", produtorNome = null, ratingMedio = 0.0, totalReviews = 0, imagePath = null)
}

private fun pronto(garrafas: Int, total: Int = garrafas, expandido: Boolean = false) =
    ProdutorUiState.Ready(produtor(total), bebidas(minOf(garrafas, GARRAFAS_MAXIMO)), expandido = expandido)

class ProdutorUiStateTest {

    @Test
    fun `ate 3 garrafas mostra tudo e nao ha carregar mais`() {
        assertEquals(3, pronto(3).visiveis.size)
        assertEquals(0, pronto(3).maisParaCarregar)
        assertEquals(1, pronto(1).visiveis.size)
        assertEquals(0, pronto(0).visiveis.size)
        assertEquals(0, pronto(0).maisParaCarregar)
    }

    @Test
    fun `com mais de 3 mostra 3 e oferece as que faltam ate 6`() {
        val quatro = pronto(4)
        assertEquals(3, quatro.visiveis.size)
        assertEquals(1, quatro.maisParaCarregar)
        assertEquals(2, pronto(5).maisParaCarregar)
        assertEquals(3, pronto(6).maisParaCarregar)
    }

    @Test
    fun `o carregar mais nunca passa de 3 mesmo com muitas garrafas`() {
        // O produtor tem 40, a página só pediu as primeiras 6.
        val muitas = pronto(garrafas = 40, total = 40)
        assertEquals(6, muitas.bebidas.size)
        assertEquals(3, muitas.maisParaCarregar)
    }

    @Test
    fun `depois de carregar mais mostra ate 6 e o botao desaparece`() {
        val expandido = pronto(garrafas = 6, expandido = true)
        assertEquals(6, expandido.visiveis.size)
        assertEquals(0, expandido.maisParaCarregar)
    }

    @Test
    fun `o resto so se ve no catalogo quando o produtor tem mais de 6`() {
        assertFalse(pronto(garrafas = 6, total = 6).temMaisNoCatalogo)
        assertTrue(pronto(garrafas = 6, total = 7).temMaisNoCatalogo)
        assertFalse(pronto(garrafas = 2).temMaisNoCatalogo)
    }
}
