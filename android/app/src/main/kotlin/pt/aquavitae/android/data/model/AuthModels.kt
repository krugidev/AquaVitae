package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * Registo em 2 fases: aqui só o essencial (o nome, nacionalidade, avatar e preferências vêm depois, já autenticado, por
 * `PUT /api/users/me`). `aceitouTermos` é obrigatório e tem de ser `true` (o popup dos termos).
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val aceitouTermos: Boolean,
)

/** `identificador` é o username OU o email (o campo único "Username ou Email" do ecrã de login). */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val identificador: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String,
)

// Recuperação de password: a conta identifica-se como no login, por "Username ou Email" (`identificador`).
@JsonClass(generateAdapter = true)
data class RecuperarPasswordRequest(
    val identificador: String,
)

@JsonClass(generateAdapter = true)
data class VerificarCodigoRequest(
    val identificador: String,
    val codigo: String,
)

@JsonClass(generateAdapter = true)
data class RedefinirPasswordRequest(
    val identificador: String,
    val codigo: String,
    val novaPassword: String,
)

/** Corpo de erro da API: `{ "status": 409, "error": "Conflict", "message": "..." }`. */
@JsonClass(generateAdapter = true)
data class ApiError(
    val status: Int,
    val error: String?,
    val message: String?,
)
