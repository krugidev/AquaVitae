package pt.aquavitae.api.auth

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.auth.dto.AuthResponse
import pt.aquavitae.api.auth.dto.CodigoInfoResponse
import pt.aquavitae.api.auth.dto.LoginRequest
import pt.aquavitae.api.auth.dto.RecuperarPasswordRequest
import pt.aquavitae.api.auth.dto.RedefinirPasswordRequest
import pt.aquavitae.api.auth.dto.RefreshRequest
import pt.aquavitae.api.auth.dto.RegisterRequest
import pt.aquavitae.api.auth.dto.VerificarCodigoRequest

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        authService.login(request)

    // A app troca o refresh token por um par novo quando o JWT de acesso expira.
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): AuthResponse = authService.refresh(request)

    // Termina a sessão deste dispositivo. Idempotente: um token desconhecido também dá 204.
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshRequest): ResponseEntity<Void> {
        authService.logout(request)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/recuperar-password")
    fun recuperarPassword(@Valid @RequestBody request: RecuperarPasswordRequest): ResponseEntity<CodigoInfoResponse> {
        passwordResetService.recuperarPassword(request.identificador)
        // Sempre 202 com o mesmo corpo, exista ou não a conta (não revela que contas existem).
        return ResponseEntity.accepted().body(passwordResetService.codigoInfo())
    }

    @PostMapping("/verificar-codigo")
    fun verificarCodigo(@Valid @RequestBody request: VerificarCodigoRequest): ResponseEntity<Void> {
        passwordResetService.verificarCodigo(request.identificador, request.codigo)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/redefinir-password")
    fun redefinirPassword(@Valid @RequestBody request: RedefinirPasswordRequest): ResponseEntity<Void> {
        passwordResetService.redefinirPassword(request.identificador, request.codigo, request.novaPassword)
        return ResponseEntity.ok().build()
    }
}
