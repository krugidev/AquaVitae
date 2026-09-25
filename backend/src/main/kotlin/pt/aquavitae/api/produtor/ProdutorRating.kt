package pt.aquavitae.api.produtor

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * O "rating geral" de um produtor (página do produtor, pedido do utilizador em 2026-09-24): a média de TODAS as reviews
 * das bebidas dele. Como cada bebida só guarda a sua média (`bebida_rating_medio`) e o nº de reviews
 * (`bebida_total_reviews`, ambos mantidos pelo trigger de `review`), o rating geral é a média das médias **ponderada
 * pelo nº de reviews** — `SUM(rating × reviews) / SUM(reviews)`. Sem ponderar, uma bebida com 1 review de 5,0 pesaria
 * tanto como outra com 200 reviews de 4,0. Bebidas sem reviews não pesam (peso 0). Sem nenhuma review, não há rating
 * (`null` — a app mostra "sem reviews ainda", nunca um 0,0 que pareça uma nota).
 *
 * Nota: o "produtor da semana" (`ProdutorDestaque`) continua a ordenar pela média simples das bebidas avaliadas
 * (decisão aprovada em 2026-09-19) — são duas medidas diferentes de propósito.
 */
object ProdutorRating {

    fun media(somaPonderada: BigDecimal?, totalReviews: Long): Double? {
        if (somaPonderada == null || totalReviews <= 0) return null
        return somaPonderada.divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP).toDouble()
    }
}
