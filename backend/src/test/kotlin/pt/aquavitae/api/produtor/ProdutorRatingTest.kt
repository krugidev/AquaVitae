package pt.aquavitae.api.produtor

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProdutorRatingTest {

    @Test
    fun `sem reviews nao ha rating`() {
        assertNull(ProdutorRating.media(somaPonderada = null, totalReviews = 0))
        assertNull(ProdutorRating.media(somaPonderada = BigDecimal.ZERO, totalReviews = 0))
    }

    @Test
    fun `uma so bebida devolve o rating dela`() {
        // 4,50 de média × 12 reviews.
        assertEquals(4.5, ProdutorRating.media(BigDecimal("54.00"), 12))
    }

    @Test
    fun `a media pondera pelo numero de reviews de cada bebida`() {
        // 4,9 com 12 reviews, 4,5 com 28 reviews e 4,2 com 41 reviews (o exemplo do mockup): soma = 58,8 + 126 + 172,2.
        val soma = BigDecimal("4.90") * BigDecimal(12) + BigDecimal("4.50") * BigDecimal(28) + BigDecimal("4.20") * BigDecimal(41)
        assertEquals(4.41, ProdutorRating.media(soma, 81))
    }

    @Test
    fun `uma bebida com uma review nao pesa como uma com muitas`() {
        // 5,0 com 1 review e 4,0 com 99: a média simples seria 4,5, a ponderada é 4,01.
        val soma = BigDecimal("5.00") * BigDecimal(1) + BigDecimal("4.00") * BigDecimal(99)
        assertEquals(4.01, ProdutorRating.media(soma, 100))
    }

    @Test
    fun `arredonda a duas casas`() {
        // 10 / 3 = 3,333...
        assertEquals(3.33, ProdutorRating.media(BigDecimal("10.00"), 3))
    }
}
