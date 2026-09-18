package pt.aquavitae.api.review.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import pt.aquavitae.api.review.Review
import java.math.BigDecimal

data class ReviewRequest(
    @field:NotNull @field:DecimalMin("0.0") @field:DecimalMax("5.0")
    val rating: BigDecimal,

    val comment: String? = null,
)

data class ReviewResponse(
    val id: Long,
    val bebidaId: Long?,
    val utilizadorUsername: String?,
    val rating: BigDecimal?,
    val comment: String?,
    val createdAt: java.time.Instant?,
) {
    companion object {
        fun from(review: Review) = ReviewResponse(
            id = review.id,
            bebidaId = review.bebida?.id,
            utilizadorUsername = review.utilizador?.username,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.dataCriacao,
        )
    }
}

// Resposta de GET /api/bebidas/{id}/reviews — distribuicao é a contagem de
// reviews por estrela (1-5, arredondado), para o gráfico da tab de reviews
// do popup da bebida.
data class ReviewsResponse(
    val distribuicao: Map<Int, Int>,
    val reviews: List<ReviewResponse>,
)
