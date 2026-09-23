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
import pt.aquavitae.api.utilizador.findByIdentificador
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
        // O teclado do telemóvel deixa espaços no fim: guarda-se sempre sem eles.
        val username = request.username.trim()
        val email = request.email.trim()
        if (utilizadorRepository.existsByEmailIgnoreCase(email)) {
            throw ConflictException("Já existe uma conta com o email $email")
        }
        if (utilizadorRepository.existsByUsernameIgnoreCase(username)) {
            throw ConflictException("O username $username já está em uso")
        }

        val defaultRole = utilizadorRoleRepository.findByValue(DEFAULT_ROLE).orElse(null)

        val agora = Instant.now()
        val utilizador = Utilizador(
            username = username,
            email = email,
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
        val utilizador = utilizadorRepository.findByIdentificador(request.identificador)
            ?: throw InvalidCredentialsException("Username/email ou password inválidos")

        val passwordHash = utilizador.password
            ?: throw InvalidCredentialsException("Username/email ou password inválidos")

        if (!passwordEncoder.matches(request.password, passwordHash)) {
            throw InvalidCredentialsException("Username/email ou password inválidos")
        }

        val token = jwtService.generateToken(utilizador.id, utilizador.email)
        return AuthResponse(token = token, userId = utilizador.id, username = utilizador.username)
    }
}
