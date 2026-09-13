package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProdutorDetail(
    val id: Long,
    val nome: String,
    val paisNome: String?,
    val regiao: String?,
    val historia: String?,
    val anoFundacao: Int?,
    val website: String?,
    val imagePath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val permiteVisitas: Boolean,
)
