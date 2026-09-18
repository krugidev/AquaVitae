package pt.aquavitae.api.auth

import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.common.InvalidCredentialsException
import pt.aquavitae.api.utilizador.UtilizadorRepository
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val CODIGO_VALIDADE_MINUTOS = 15L

// Envio do código por email fica para quando o resto dos endpoints estiver
// pronto (decisão do utilizador, ver PLANO.md) — em dev, o código só fica no
// log. O 202 em recuperarPassword é sempre o mesmo, exista ou não a conta,
// para não revelar a terceiros que emails estão registados.
@Service
class PasswordResetService(
    private val utilizadorRepository: UtilizadorRepository,
    private val passwordResetRepository: UtilizadorPasswordResetRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
    private val random = SecureRandom()

    @Transactional
    fun recuperarPassword(email: String) {
        val utilizador = utilizadorRepository.findByEmail(email).orElse(null) ?: return

        val codigo = (0 until 6).joinToString("") { random.nextInt(10).toString() }
        passwordResetRepository.save(
            UtilizadorPasswordReset(
                utilizador = utilizador,
                codigo = codigo,
                expiraEm = Instant.now().plus(CODIGO_VALIDADE_MINUTOS, ChronoUnit.MINUTES),
                usado = false,
                dataCriacao = Instant.now(),
            ),
        )
        logger.info("Código de recuperação de password para {}: {} (válido {} min)", email, codigo, CODIGO_VALIDADE_MINUTOS)
    }

    fun verificarCodigo(email: String, codigo: String) {
        findCodigoValido(email, codigo)
    }

    @Transactional
    fun redefinirPassword(email: String, codigo: String, novaPassword: String) {
        val reset = findCodigoValido(email, codigo)
        val utilizador = reset.utilizador ?: throw InvalidCredentialsException("Código inválido")

        utilizador.password = passwordEncoder.encode(novaPassword)
        utilizadorRepository.save(utilizador)

        reset.usado = true
        passwordResetRepository.save(reset)
    }

    private fun findCodigoValido(email: String, codigo: String): UtilizadorPasswordReset {
        val utilizador = utilizadorRepository.findByEmail(email)
            .orElseThrow { InvalidCredentialsException("Código inválido") }

        val reset = passwordResetRepository.findByUtilizador_IdAndCodigoAndUsadoFalse(utilizador.id, codigo)
            ?: throw InvalidCredentialsException("Código inválido")

        if (reset.expiraEm?.isBefore(Instant.now()) != false) {
            throw InvalidCredentialsException("Código expirado")
        }
        return reset
    }
}
