package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReviewRequest(
    val rating: Double,
    val comment: String?,
)

@JsonClass(generateAdapter = true)
data class ReviewResponse(
    val id: Long,
    val bebidaId: Long?,
    val utilizadorUsername: String?,
    // "Nome apelido" do perfil do autor, ou o username se não preencheu; ver `Utilizador.nomeParaMostrar()` no backend.
    val utilizadorNome: String?,
    val utilizadorAvatar: String?,
    val rating: Double?,
    val comment: String?,
    val createdAt: String?,
)

/** `GET /api/bebidas/{id}/reviews`: a distribuição (chaves "1".."5", a estrela — vêm como texto no JSON) para o
 * gráfico de barras da tab "Reviews" do popup da bebida, e a lista de reviews (mais recentes primeiro). */
@JsonClass(generateAdapter = true)
data class ReviewsResponse(
    val distribuicao: Map<String, Int>,
    val reviews: List<ReviewResponse>,
)
