package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PreferenciaRequest(
    val acidezMin: Int?,
    val acidezMax: Int?,
    val docuraMin: Int?,
    val docuraMax: Int?,
    val categoriaIds: List<Long>,
    val castaIds: List<Long>,
)
