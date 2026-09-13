package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CaveRequest(
    val nome: String,
    val descricao: String?,
)

@JsonClass(generateAdapter = true)
data class CaveResponse(
    val id: Long,
    val nome: String,
    val descricao: String?,
)

@JsonClass(generateAdapter = true)
data class CaveDetailResponse(
    val id: Long,
    val nome: String,
    val descricao: String?,
    val bebidas: List<CaveBebidaResponse>,
)

@JsonClass(generateAdapter = true)
data class CaveBebidaRequest(
    val bebidaId: Long,
    val quantidade: Int,
    val precoPago: Double?,
    val dataAquisicao: String?,
)

/** Todos os campos são opcionais: PATCH parcial (só o que o utilizador alterou é enviado). */
@JsonClass(generateAdapter = true)
data class CaveBebidaUpdateRequest(
    val isConsumida: Boolean?,
    val dataConsumo: String?,
    val notas: String?,
)

@JsonClass(generateAdapter = true)
data class CaveBebidaResponse(
    val id: Long,
    val bebidaId: Long,
    val bebidaNome: String,
    val quantidade: Int,
    val precoPago: Double?,
    val dataAquisicao: String?,
    val isConsumida: Boolean,
    val dataConsumo: String?,
    val notas: String?,
)
