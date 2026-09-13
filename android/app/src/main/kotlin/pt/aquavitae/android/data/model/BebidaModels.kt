package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BebidaSummary(
    val id: Long,
    val nome: String,
    val categoriaNome: String,
    val produtorNome: String?,
    val ratingMedio: Double,
    val totalReviews: Int,
    val imagePath: String?,
)

@JsonClass(generateAdapter = true)
data class BebidaDetail(
    val id: Long,
    val nome: String,
    val categoriaNome: String,
    val produtorId: Long?,
    val produtorNome: String?,
    val paisOrigemNome: String?,
    val anoProducao: Int?,
    val teorAlcoolico: Double?,
    val volumeMl: Int?,
    val imagePath: String?,
    val ratingMedio: Double,
    val totalReviews: Int,
    // Só vem preenchido quando categoriaNome == "Vinho".
    val vinhoDetalhe: VinhoDetalhe?,
)

@JsonClass(generateAdapter = true)
data class VinhoDetalhe(
    val corpo: String?,
    val nivelAcidez: Int?,
    val nivelDocura: Int?,
    val tanino: String?,
    val tipo: String?,
    val castas: List<CastaPercentagem>,
)

@JsonClass(generateAdapter = true)
data class CastaPercentagem(
    val casta: String,
    val percentagem: Double,
)
