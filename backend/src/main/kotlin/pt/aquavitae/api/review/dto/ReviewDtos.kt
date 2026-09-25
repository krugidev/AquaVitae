package pt.aquavitae.api.review.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import pt.aquavitae.api.bebida.dto.BebidaSummaryDto
import pt.aquavitae.api.review.Review
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.utilizador.nomeParaMostrar
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
    // Nome e avatar do autor, para cada review da tab "Reviews". `utilizadorNome` é "nome apelido" (o que o
    // autor preencheu no perfil) ou, se não preencheu, o username. `utilizadorAvatar` é o caminho relativo
    // (icones/avatares/...), resolvido como API_BASE_URL + path; null se não escolheu avatar.
    val utilizadorNome: String?,
    val utilizadorAvatar: String?,
    val rating: BigDecimal?,
    val comment: String?,
    val createdAt: java.time.Instant?,
) {
    companion object {
        // `autor.avatar` é LAZY: o autor tem de vir com o avatar carregado (ReviewRepository.findByBebidaIdComAutor,
        // UtilizadorRepository.findByIdWithProfile) — nunca o utilizador do token, que vem sem ele.
        fun from(review: Review, autor: Utilizador) = ReviewResponse(
            id = review.id,
            bebidaId = review.bebida?.id,
            utilizadorUsername = autor.username,
            utilizadorNome = autor.nomeParaMostrar(),
            utilizadorAvatar = autor.avatar?.pathImage,
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

// Item de GET /api/users/me/reviews: uma review do próprio utilizador com a bebida avaliada (mesmo cartão
// do catálogo). Não repete o autor: é sempre o próprio.
data class MinhaReviewDto(
    val id: Long,
    val rating: BigDecimal?,
    val comment: String?,
    val createdAt: java.time.Instant?,
    val bebida: BebidaSummaryDto,
)
