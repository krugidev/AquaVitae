package pt.aquavitae.android.feature.perfil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.MinhaReview
import pt.aquavitae.android.data.model.UtilizadorMe

class MinhasReviewsRegrasTest {

    private fun review(id: Long, data: String?, comentario: String? = null, nota: Double? = 4.0) = MinhaReview(
        id = id,
        rating = nota,
        comment = comentario,
        createdAt = data,
        bebida = BebidaSummary(id = id, nome = "Bebida $id", categoriaNome = "Vinho", produtorNome = null, ratingMedio = 0.0, totalReviews = 0, imagePath = null),
    )

    @Test
    fun `um comentario curto fica como esta`() {
        assertEquals("Muito bom.", excertoDaReview("  Muito bom.  "))
    }

    @Test
    fun `um comentario sem texto nao tem excerto`() {
        assertNull(excertoDaReview(null))
        assertNull(excertoDaReview("   "))
        assertNull(excertoDaReview(""))
    }

    @Test
    fun `um comentario longo corta-se numa palavra inteira, com reticencias`() {
        val texto = "Citrino, salino, com aquela acidez que pede ameijoas. Bebido com o mar à frente, o que fez toda a diferença."
        val excerto = excertoDaReview(texto)!!
        assertTrue(excerto.endsWith("…"))
        assertTrue("passou do limite: ${excerto.length}", excerto.length <= EXCERTO_MAX + 1)
        // Não parte uma palavra a meio: o que ficou antes do "…" é um prefixo do texto que acaba no fim de uma palavra.
        val base = excerto.removeSuffix("…")
        assertTrue(texto.startsWith(base))
        assertTrue(texto[base.length] == ' ' || texto[base.length] == ',' || texto[base.length] == '.')
    }

    @Test
    fun `as quebras de linha e os espacos repetidos colapsam`() {
        assertEquals("Uma linha. Outra linha.", excertoDaReview("Uma  linha.\n\nOutra   linha."))
    }

    @Test
    fun `uma palavra enorme sem espacos corta a direito`() {
        val excerto = excertoDaReview("a".repeat(200))!!
        assertEquals(EXCERTO_MAX + 1, excerto.length)
    }

    @Test
    fun `o mes conta-se na hora de Portugal`() {
        assertEquals(MesDeReviews(2026, 9), mesDaReview("2026-09-18T10:00:00Z"))
        // 23:30 UTC de 31 de agosto já é 1 de setembro em Lisboa (horário de verão).
        assertEquals(MesDeReviews(2026, 9), mesDaReview("2026-08-31T23:30:00Z"))
        // No inverno Lisboa está em UTC+0: a mesma hora não muda de mês.
        assertEquals(MesDeReviews(2026, 12), mesDaReview("2026-12-31T23:30:00Z"))
        assertNull(mesDaReview(null))
        assertNull(mesDaReview("isto não é uma data"))
    }

    @Test
    fun `o cabecalho e a pilula de um mes`() {
        val setembro = MesDeReviews(2026, 9)
        assertEquals("SETEMBRO 2026", setembro.cabecalho)
        assertEquals("Setembro", setembro.pilula(anoDeReferencia = 2026))
        assertEquals("Setembro 2026", setembro.pilula(anoDeReferencia = 2027))
        assertEquals("MARÇO 2025", MesDeReviews(2025, 3).cabecalho)
    }

    @Test
    fun `a data curta tem o dia e o mes abreviado em maiusculas`() {
        assertEquals("18 SET", dataCurtaDaReview("2026-09-18T10:00:00Z"))
        assertEquals("4 AGO", dataCurtaDaReview("2026-08-04T12:00:00Z"))
        assertEquals("", dataCurtaDaReview(null))
    }

    @Test
    fun `agrupa por mes, do mais recente para o mais antigo, e deixa as sem data no fim`() {
        val reviews = listOf(
            review(1, "2026-09-18T10:00:00Z"),
            review(2, "2026-09-02T10:00:00Z"),
            review(3, "2026-08-27T10:00:00Z"),
            review(4, null),
            review(5, "2025-12-25T10:00:00Z"),
        )
        val grupos = agruparPorMes(reviews)
        assertEquals(listOf("SETEMBRO 2026", "AGOSTO 2026", "DEZEMBRO 2025", "SEM DATA"), grupos.map { it.cabecalho })
        assertEquals(listOf(1L, 2L), grupos[0].reviews.map { it.id })
        assertEquals(listOf(4L), grupos.last().reviews.map { it.id })
    }

    @Test
    fun `os meses das pilulas sao os que tem reviews, do mais recente para o mais antigo`() {
        val reviews = listOf(review(1, "2025-12-25T10:00:00Z"), review(2, "2026-09-18T10:00:00Z"), review(3, "2026-09-02T10:00:00Z"), review(4, null))
        assertEquals(listOf(MesDeReviews(2026, 9), MesDeReviews(2025, 12)), mesesComReviews(reviews))
    }

    @Test
    fun `sem reviews nao ha grupos nem meses`() {
        assertTrue(agruparPorMes(emptyList()).isEmpty())
        assertTrue(mesesComReviews(emptyList()).isEmpty())
    }

    // ---- "Apagar conta": o que se diz que se perde ----

    private fun utilizador(caves: Int = 0, garrafas: Int = 0, favoritos: Int = 0, wishlist: Int = 0, provadas: Int = 0, reviews: Int = 0) = UtilizadorMe(
        id = 1, username = "u", email = "u@x.pt", firstName = null, lastName = null,
        totalCaves = caves, totalGarrafas = garrafas, totalFavoritos = favoritos, totalWishlist = wishlist, totalProvadas = provadas, totalReviews = reviews,
        termosAceitesEm = null, precisaAceitarTermos = false,
    )

    @Test
    fun `o resumo do que se perde so tem o que existe, com singular e plural`() {
        assertEquals(
            listOf("2 caves (5 garrafas)", "1 favorito", "3 na wishlist", "4 bebidas provadas", "1 review"),
            resumoDoQueSePerde(utilizador(caves = 2, garrafas = 5, favoritos = 1, wishlist = 3, provadas = 4, reviews = 1)),
        )
        assertEquals(listOf("1 cave (1 garrafa)"), resumoDoQueSePerde(utilizador(caves = 1, garrafas = 1)))
        assertEquals(listOf("1 cave"), resumoDoQueSePerde(utilizador(caves = 1)))
        assertEquals(listOf("1 bebida provada"), resumoDoQueSePerde(utilizador(provadas = 1)))
    }

    @Test
    fun `sem nada guardado, ou sem perfil carregado, o resumo e vazio`() {
        assertTrue(resumoDoQueSePerde(utilizador()).isEmpty())
        assertTrue(resumoDoQueSePerde(null).isEmpty())
    }
}
