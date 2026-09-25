package pt.aquavitae.api.auth.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Size(min = 3, max = 30)
    val username: String,

    @field:NotBlank @field:Email @field:Size(max = 100)
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 72)
    val password: String,

    @field:Size(max = 25)
    val firstName: String? = null,

    @field:Size(max = 40)
    val lastName: String? = null,

    // O popup dos termos e condições do registo: sem aceitar não há conta (400). O momento fica guardado.
    @field:AssertTrue(message = "É preciso aceitar os termos e condições")
    val aceitouTermos: Boolean = false,
)

data class LoginRequest(
    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank
    val password: String,
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String?,
)

data class RecuperarPasswordRequest(
    @field:NotBlank @field:Email
    val email: String,
)

data class VerificarCodigoRequest(
    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank
    val codigo: String,
)

data class RedefinirPasswordRequest(
    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank
    val codigo: String,

    @field:NotBlank @field:Size(min = 8, max = 72)
    val novaPassword: String,
)
