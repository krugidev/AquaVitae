package pt.aquavitae.api.review.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import pt.aquavitae.api.review.Review

data class ReviewRequest(
    @field:NotNull @field:DecimalMin("0.0") @field:DecimalMax("5.0")
    val rating: Double,

    val comment: String? = null,
)

data class ReviewResponse(
    val id: Long,
    val bebidaId: Long?,
    val utilizadorUsername: String?,
    val rating: Double?,
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
