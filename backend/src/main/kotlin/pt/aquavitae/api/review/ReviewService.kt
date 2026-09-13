package pt.aquavitae.api.review

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException
import pt.aquavitae.api.review.dto.ReviewRequest
import pt.aquavitae.api.review.dto.ReviewResponse
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val bebidaRepository: BebidaRepository,
) {

    fun listByBebida(bebidaId: Long): List<ReviewResponse> =
        reviewRepository.findByBebida_IdOrderByDataCriacaoDesc(bebidaId).map { ReviewResponse.from(it) }

    @Transactional
    fun create(bebidaId: Long, utilizador: Utilizador, request: ReviewRequest): ReviewResponse {
        val bebida = bebidaRepository.findById(bebidaId)
            .orElseThrow { ResourceNotFoundException("Bebida $bebidaId não encontrada") }

        // UNIQUE(bebida_id, utilizador_id) — serve também para verificar,
        // aqui, se o utilizador já avaliou esta bebida (ver briefing secção 5).
        reviewRepository.findByBebida_IdAndUtilizador_Id(bebidaId, utilizador.id).ifPresent {
            throw ConflictException("Já existe uma review deste utilizador para esta bebida")
        }

        val review = Review(
            bebida = bebida,
            utilizador = utilizador,
            rating = request.rating,
            comment = request.comment,
            dataCriacao = Instant.now(),
        )
        // A trigger trg_review_sync_bebida_stats (ver database/ddl/03_triggers.sql)
        // recalcula bebida_rating_medio / bebida_total_reviews — não é feito aqui.
        val saved = reviewRepository.save(review)
        return ReviewResponse.from(saved)
    }

    @Transactional
    fun update(reviewId: Long, utilizador: Utilizador, request: ReviewRequest): ReviewResponse {
        val review = findOwned(reviewId, utilizador)
        review.rating = request.rating
        review.comment = request.comment
        review.dataAtualizacao = Instant.now()
        return ReviewResponse.from(reviewRepository.save(review))
    }

    @Transactional
    fun delete(reviewId: Long, utilizador: Utilizador) {
        val review = findOwned(reviewId, utilizador)
        reviewRepository.delete(review)
    }

    private fun findOwned(reviewId: Long, utilizador: Utilizador): Review {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ResourceNotFoundException("Review $reviewId não encontrada") }
        if (review.utilizador?.id != utilizador.id) {
            throw UnauthorizedActionException("Esta review não pertence a este utilizador")
        }
        return review
    }
}
