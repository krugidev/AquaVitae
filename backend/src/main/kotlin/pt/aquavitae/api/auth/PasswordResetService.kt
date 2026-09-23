package pt.aquavitae.api.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.common.InvalidCredentialsException
import pt.aquavitae.api.email.EmailService
import pt.aquavitae.api.utilizador.UtilizadorRepository
import pt.aquavitae.api.utilizador.findByIdentificador
import pt.aquavitae.api.utilizador.nomeParaMostrar
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val CODIGO_VALIDADE_MINUTOS = 15L

// O código vai por email (EmailService; sem servidor configurado, em dev fica no log da API). O 202 em recuperarPassword é
// sempre o mesmo, exista ou não a conta, para não revelar a terceiros que contas (emails ou usernames) estão registadas —
// por isso o email também se envia à parte (@Async), para o tempo de resposta não denunciar a conta.
// A conta identifica-se por "Username ou Email" (`identificador`), como no login.
@Service
class PasswordResetService(
    private val utilizadorRepository: UtilizadorRepository,
    private val passwordResetRepository: UtilizadorPasswordResetRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    @Value("\${aquavitae.recuperacao.intervalo-minimo-segundos}") private val intervaloMinimoSegundos: Long,
) {
    private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
    private val random = SecureRandom()

    @Transactional
    fun recuperarPassword(identificador: String) {
        val utilizador = utilizadorRepository.findByIdentificador(identificador) ?: return
        val agora = Instant.now()

        // Anti-abuso: um pedido demasiado a seguir ao anterior não gera outro código nem outro email (a resposta é a mesma).
        val ultimo = passwordResetRepository.findFirstByUtilizador_IdOrderByDataCriacaoDesc(utilizador.id)
        if (!podeGerarNovoCodigo(ultimo?.dataCriacao, agora, intervaloMinimoSegundos)) {
            logger.debug("Pedido de recuperação ignorado (intervalo mínimo) para o utilizador {}", utilizador.id)
            return
        }

        // Só o código mais recente serve: os anteriores, ainda por usar, deixam de valer.
        passwordResetRepository.invalidarPendentes(utilizador.id)

        val codigo = (0 until 6).joinToString("") { random.nextInt(10).toString() }
        passwordResetRepository.save(
            UtilizadorPasswordReset(
                utilizador = utilizador,
                codigo = codigo,
                expiraEm = agora.plus(CODIGO_VALIDADE_MINUTOS, ChronoUnit.MINUTES),
                usado = false,
                dataCriacao = agora,
            ),
        )

        val destinatario = utilizador.email?.takeIf { it.isNotBlank() }
        if (destinatario == null) {
            logger.warn("O utilizador {} não tem email: não se enviou o código de recuperação", utilizador.id)
            return
        }
        emailService.enviarCodigoRecuperacao(utilizador.id, destinatario, utilizador.nomeParaMostrar(), codigo, CODIGO_VALIDADE_MINUTOS)
    }

    fun verificarCodigo(identificador: String, codigo: String) {
        findCodigoValido(identificador, codigo)
    }

    @Transactional
    fun redefinirPassword(identificador: String, codigo: String, novaPassword: String) {
        val reset = findCodigoValido(identificador, codigo)
        val utilizador = reset.utilizador ?: throw InvalidCredentialsException("Código inválido")

        utilizador.password = passwordEncoder.encode(novaPassword)
        utilizadorRepository.save(utilizador)

        reset.usado = true
        passwordResetRepository.save(reset)
    }

    private fun findCodigoValido(identificador: String, codigo: String): UtilizadorPasswordReset {
        val utilizador = utilizadorRepository.findByIdentificador(identificador)
            ?: throw InvalidCredentialsException("Código inválido")

        val reset = passwordResetRepository.findByUtilizador_IdAndCodigoAndUsadoFalse(utilizador.id, codigo)
            ?: throw InvalidCredentialsException("Código inválido")

        if (reset.expiraEm?.isBefore(Instant.now()) != false) {
            throw InvalidCredentialsException("Código expirado")
        }
        return reset
    }
}
