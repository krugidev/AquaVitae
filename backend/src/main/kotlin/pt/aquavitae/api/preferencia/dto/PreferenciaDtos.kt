package pt.aquavitae.api.preferencia.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PreferenciaRequest(
    @field:Min(1) @field:Max(5) val acidezMin: Int? = null,
    @field:Min(1) @field:Max(5) val acidezMax: Int? = null,
    @field:Min(1) @field:Max(5) val docuraMin: Int? = null,
    @field:Min(1) @field:Max(5) val docuraMax: Int? = null,
    val categoriaIds: List<Long> = emptyList(),
    val castaIds: List<Long> = emptyList(),
)

data class PreferenciaResponse(
    val acidezMin: Int?,
    val acidezMax: Int?,
    val docuraMin: Int?,
    val docuraMax: Int?,
    val categoriaIds: List<Long>,
    val castaIds: List<Long>,
)
