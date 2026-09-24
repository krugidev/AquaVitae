package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * A resposta (202) de `POST /api/auth/recuperar-password`: quanto tempo vale o código e quanto falta para se poder pedir outro.
 * Iguais para todas as contas (o servidor não revela se a conta existe): a app arranca os dois contadores quando a recebe.
 */
@JsonClass(generateAdapter = true)
data class CodigoInfo(
    val validadeSegundos: Long,
    val novoPedidoEmSegundos: Long,
)

/** `POST /api/users/me/apagar`: apagar a conta pede a password dela. */
@JsonClass(generateAdapter = true)
data class ApagarContaRequest(
    val password: String,
)

/** Uma linha de `GET /api/users/me/reviews`: a review e a bebida a que pertence (mais recentes primeiro). */
@JsonClass(generateAdapter = true)
data class MinhaReview(
    val id: Long,
    val rating: Double?,
    val comment: String?,
    val createdAt: String?,
    val bebida: BebidaSummary,
)
