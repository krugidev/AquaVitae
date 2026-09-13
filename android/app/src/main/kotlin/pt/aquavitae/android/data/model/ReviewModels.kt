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
    val bebidaId: Long,
    val utilizadorUsername: String,
    val rating: Double,
    val comment: String?,
    val createdAt: String,
)
