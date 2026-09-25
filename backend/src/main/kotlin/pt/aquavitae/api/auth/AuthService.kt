package pt.aquavitae.api.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.auth.dto.AuthResponse
import pt.aquavitae.api.auth.dto.LoginRequest
import pt.aquavitae.api.auth.dto.RegisterRequest
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.InvalidCredentialsException
import pt.aquavitae.api.lookup.UtilizadorRoleRepository
import pt.aquavitae.api.security.JwtService
import pt.aquavitae.api.utilizador.Utilizador
import pt.aquavitae.api.utilizador.UtilizadorRepository
import java.time.Instant

private const val DEFAULT_ROLE = "Utilizador"

@Service
class AuthService(
    private val utilizadorRepository: UtilizadorRepository,
    private val utilizadorRoleRepository: UtilizadorRoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (utilizadorRepository.existsByEmail(request.email)) {
            throw ConflictException("Já existe uma conta com o email ${request.email}")
        }
        if (utilizadorRepository.existsByUsername(request.username)) {
            throw ConflictException("O username ${request.username} já está em uso")
        }

        val defaultRole = utilizadorRoleRepository.findByValue(DEFAULT_ROLE).orElse(null)

        val agora = Instant.now()
        val utilizador = Utilizador(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            accountCreatedAt = agora,
            role = defaultRole,
            // `aceitouTermos` já foi validado como verdadeiro (Bean Validation): a conta nasce com os termos aceites.
            termosAceitesEm = agora,
        )
        val saved = utilizadorRepository.save(utilizador)

        val token = jwtService.generateToken(saved.id, saved.email)
        return AuthResponse(token = token, userId = saved.id, username = saved.username)
    }

    fun login(request: LoginRequest): AuthResponse {
        val utilizador = utilizadorRepository.findByEmail(request.email)
            .orElseThrow { InvalidCredentialsException("Email ou password inválidos") }

        val passwordHash = utilizador.password
            ?: throw InvalidCredentialsException("Email ou password inválidos")

        if (!passwordEncoder.matches(request.password, passwordHash)) {
            throw InvalidCredentialsException("Email ou password inválidos")
        }

        val token = jwtService.generateToken(utilizador.id, utilizador.email)
        return AuthResponse(token = token, userId = utilizador.id, username = utilizador.username)
    }
}
