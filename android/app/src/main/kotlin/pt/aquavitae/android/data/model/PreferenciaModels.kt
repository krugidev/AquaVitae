package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * `PUT /api/users/me/preferencias`. Doçura e acidez são intervalos de 1 a 5 (1 = muito seca / muito macia, 5 = muito doce /
 * muito fresca); o onboarding pede um só nível e envia `min = max`. `null` = não respondeu.
 */
@JsonClass(generateAdapter = true)
data class PreferenciaRequest(
    val acidezMin: Int? = null,
    val acidezMax: Int? = null,
    val docuraMin: Int? = null,
    val docuraMax: Int? = null,
    val categoriaIds: List<Long> = emptyList(),
    val castaIds: List<Long> = emptyList(),
)
