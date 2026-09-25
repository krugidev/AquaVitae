package pt.aquavitae.api.review

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.bebida.BebidaSummaryAssembler
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException
import pt.aquavitae.api.provada.BebidaProvadaRepository
import pt.aquavitae.api.review.dto.MinhaReviewDto
import pt.aquavitae.api.review.dto.ReviewRequest
import pt.aquavitae.api.review.dto.ReviewResponse
import pt.aquavitae.api.review.dto.ReviewsResponse
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.utilizador.UtilizadorRepository
import java.math.RoundingMode
import java.time.Instant

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val bebidaRepository: BebidaRepository,
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val utilizadorRepository: UtilizadorRepository,
    private val bebidaSummaryAssembler: BebidaSummaryAssembler,
) {

    @Transactional(readOnly = true)
    fun listByBebida(bebidaId: Long): ReviewsResponse {
        val reviews = reviewRepository.findByBebidaIdComAutor(bebidaId)
        val distribuicao = (1..5).associateWith { estrela -> 0 }.toMutableMap()
        reviews.forEach { review ->
            review.rating?.let { rating ->
                val estrela = rating.setScale(0, RoundingMode.HALF_UP).toInt().coerceIn(1, 5)
                distribuicao[estrela] = (distribuicao[estrela] ?: 0) + 1
            }
        }
        return ReviewsResponse(
            distribuicao = distribuicao,
            reviews = reviews.mapNotNull { review -> review.utilizador?.let { ReviewResponse.from(review, it) } },
        )
    }

    // Histórico de reviews do perfil ("terá outra tela"): as do próprio utilizador, mais recentes primeiro.
    @Transactional(readOnly = true)
    fun listByUtilizador(utilizador: Utilizador): List<MinhaReviewDto> {
        val reviews = reviewRepository.findByUtilizadorIdComBebida(utilizador.id)
        val cartoes = bebidaSummaryAssembler.assemble(reviews.mapNotNull { it.bebida }, utilizador).associateBy { it.id }
        return reviews.mapNotNull { review ->
            review.bebida?.id?.let { cartoes[it] }?.let {
                MinhaReviewDto(id = review.id, rating = review.rating, comment = review.comment, createdAt = review.dataCriacao, bebida = it)
            }
        }
    }

    @Transactional
    fun create(bebidaId: Long, utilizador: Utilizador, request: ReviewRequest): ReviewResponse {
        val bebida = bebidaRepository.findById(bebidaId)
            .orElseThrow { ResourceNotFoundException("Bebida $bebidaId não encontrada") }

        // Só pode avaliar quem já marcou a bebida como provada (ver mockup da
        // tab de reviews: "só poderá fazê-lo se a bebida estiver marcada como
        // já provada").
        if (bebidaProvadaRepository.findByUtilizador_IdAndBebida_Id(utilizador.id, bebidaId) == null) {
            throw ConflictException("Só podes avaliar bebidas que já marcaste como provadas")
        }

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
        return ReviewResponse.from(saved, autorComAvatar(utilizador))
    }

    @Transactional
    fun update(reviewId: Long, utilizador: Utilizador, request: ReviewRequest): ReviewResponse {
        val review = findOwned(reviewId, utilizador)
        review.rating = request.rating
        review.comment = request.comment
        review.dataAtualizacao = Instant.now()
        return ReviewResponse.from(reviewRepository.save(review), autorComAvatar(utilizador))
    }

    @Transactional
    fun delete(reviewId: Long, utilizador: Utilizador) {
        val review = findOwned(reviewId, utilizador)
        reviewRepository.delete(review)
    }

    // O utilizador do token vem do JwtAuthenticationFilter só com o papel carregado: o avatar (LAZY) já não se
    // consegue ler dele — recarrega-se com o perfil.
    private fun autorComAvatar(utilizador: Utilizador): Utilizador =
        utilizadorRepository.findByIdWithProfile(utilizador.id)
            .orElseThrow { ResourceNotFoundException("Utilizador ${utilizador.id} não encontrado") }

    private fun findOwned(reviewId: Long, utilizador: Utilizador): Review {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ResourceNotFoundException("Review $reviewId não encontrada") }
        if (review.utilizador?.id != utilizador.id) {
            throw UnauthorizedActionException("Esta review não pertence a este utilizador")
        }
        return review
    }
}
