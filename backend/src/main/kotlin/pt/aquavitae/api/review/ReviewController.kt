package pt.aquavitae.api.review

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.review.dto.MinhaReviewDto
import pt.aquavitae.api.review.dto.ReviewRequest
import pt.aquavitae.api.review.dto.ReviewResponse
import pt.aquavitae.api.review.dto.ReviewsResponse
import pt.aquavitae.api.utilizador.Utilizador

@RestController
class ReviewController(
    private val reviewService: ReviewService,
) {

    @GetMapping("/api/bebidas/{bebidaId}/reviews")
    fun listByBebida(@PathVariable bebidaId: Long): ReviewsResponse =
        reviewService.listByBebida(bebidaId)

    // Histórico de reviews do perfil (autenticado): as minhas, mais recentes primeiro, cada uma com a bebida.
    @GetMapping("/api/users/me/reviews")
    fun listMinhas(@AuthenticationPrincipal utilizador: Utilizador): List<MinhaReviewDto> =
        reviewService.listByUtilizador(utilizador)

    @PostMapping("/api/bebidas/{bebidaId}/reviews")
    fun create(
        @PathVariable bebidaId: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: ReviewRequest,
    ): ResponseEntity<ReviewResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(bebidaId, utilizador, request))

    @PutMapping("/api/reviews/{id}")
    fun update(
        @PathVariable id: Long,
        @AuthenticationPrincipal utilizador: Utilizador,
        @Valid @RequestBody request: ReviewRequest,
    ): ReviewResponse = reviewService.update(id, utilizador, request)

    @DeleteMapping("/api/reviews/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal utilizador: Utilizador): ResponseEntity<Void> {
        reviewService.delete(id, utilizador)
        return ResponseEntity.noContent().build()
    }
}
