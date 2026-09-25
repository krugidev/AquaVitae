package pt.aquavitae.api.utilizador.dto

import jakarta.validation.constraints.NotBlank

// Apagar a conta pede a password da própria conta (a confirmação do popup "Apagar conta").
data class ApagarContaRequest(
    @field:NotBlank
    val password: String,
)
