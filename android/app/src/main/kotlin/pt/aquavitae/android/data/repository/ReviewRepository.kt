package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.MinhaReview
import pt.aquavitae.android.data.model.ReviewRequest
import pt.aquavitae.android.data.model.ReviewResponse
import pt.aquavitae.android.data.model.ReviewsResponse
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/** Repositório de reviews de uma bebida (listar, submeter, editar, apagar). */
class ReviewRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getReviews(bebidaId: Long): Result<ReviewsResponse> = runCatching {
        api.getReviews(bebidaId)
    }

    /** As reviews do utilizador (o ecrã "As minhas reviews"), com a bebida de cada uma, mais recentes primeiro. */
    suspend fun getMinhasReviews(): Result<List<MinhaReview>> = runCatching { api.getMinhasReviews() }

    suspend fun submitReview(bebidaId: Long, rating: Double, comment: String?): Result<ReviewResponse> =
        runCatching {
            api.postReview(bebidaId, ReviewRequest(rating = rating, comment = comment))
        }

    suspend fun updateReview(reviewId: Long, rating: Double, comment: String?): Result<ReviewResponse> =
        runCatching {
            api.updateReview(reviewId, ReviewRequest(rating = rating, comment = comment))
        }

    suspend fun deleteReview(reviewId: Long): Result<Unit> = runCatching {
        api.deleteReview(reviewId)
    }
}
