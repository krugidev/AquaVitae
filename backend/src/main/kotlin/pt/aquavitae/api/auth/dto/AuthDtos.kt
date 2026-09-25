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
    // "Username ou Email" (o campo único do ecrã de login): tenta primeiro o email, depois o username.
    @field:NotBlank
    val identificador: String,

    @field:NotBlank
    val password: String,
)

// `token` = o JWT de acesso (60 min); `refreshToken` = o token opaco que o troca por um par novo em `POST /api/auth/refresh`
// quando o de acesso expira (30 dias, rodado a cada renovação).
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val userId: Long,
    val username: String?,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

// Os 3 passos da recuperação identificam a conta como o login: "Username ou Email" (`identificador`).
data class RecuperarPasswordRequest(
    @field:NotBlank
    val identificador: String,
)

data class VerificarCodigoRequest(
    @field:NotBlank
    val identificador: String,

    @field:NotBlank
    val codigo: String,
)

data class RedefinirPasswordRequest(
    @field:NotBlank
    val identificador: String,

    @field:NotBlank
    val codigo: String,

    @field:NotBlank @field:Size(min = 8, max = 72)
    val novaPassword: String,
)

// A resposta (202) de `POST /api/auth/recuperar-password`: quanto tempo vale o código e quanto falta para se poder pedir outro.
// **Iguais para todas as contas, existam ou não** (não revelam nada): a app arranca os dois contadores quando recebe a resposta.
// Se o pedido chegou dentro do intervalo mínimo de um anterior, o código em vigor pode expirar até esse intervalo mais cedo do
// que a contagem da app diz (o servidor recusa então com "Código expirado").
data class CodigoInfoResponse(
    val validadeSegundos: Long,
    val novoPedidoEmSegundos: Long,
)
