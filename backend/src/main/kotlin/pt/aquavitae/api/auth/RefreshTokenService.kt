package pt.aquavitae.api.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.common.InvalidCredentialsException
import pt.aquavitae.api.utilizador.Utilizador
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration

/**
 * Sessão renovável: emite, roda e revoga refresh tokens (ver RefreshTokenRegras). O token de acesso (JWT) continua a durar
 * `jwt.expiration-minutes`; quando expira a app apresenta o refresh token a `POST /api/auth/refresh` e recebe um par novo,
 * sem pedir a password. **Rotação:** cada renovação revoga o token usado e emite outro.
 */
@Service
class RefreshTokenService(
    private val repository: RefreshTokenRepository,
    private val clock: Clock,
    @Value("\${jwt.refresh-expiration-days:30}") private val validadeDias: Long,
    @Value("\${jwt.refresh-tolerance-seconds:60}") private val toleranciaSegundos: Long,
) {
    private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)
    private val random = SecureRandom()

    /** Emite um refresh token novo para o utilizador; devolve o token **em claro** (só existe aqui e na resposta). */
    @Transactional
    fun emitir(utilizador: Utilizador): String {
        val plano = RefreshTokenRegras.gerar(random)
        val agora = clock.instant()
        repository.save(
            RefreshToken(
                utilizador = utilizador,
                tokenHash = RefreshTokenRegras.hash(plano),
                criadoEm = agora,
                expiraEm = agora.plus(Duration.ofDays(validadeDias)),
            ),
        )
        return plano
    }

    /**
     * Troca um refresh token por um novo: valida, revoga o usado e emite outro; devolve o utilizador e o token novo.
     * Um token **revogado** (fora da tolerância) revoga todos os do utilizador — alguém guardou um token que já tinha sido
     * rodado — e a renovação falha. `noRollbackFor`: essa revogação em massa tem de se gravar mesmo com a exceção.
     */
    @Transactional(noRollbackFor = [InvalidCredentialsException::class])
    fun rodar(tokenPlano: String): Pair<Utilizador, String> {
        val token = repository.findByHashComUtilizador(RefreshTokenRegras.hash(tokenPlano))
            ?: throw InvalidCredentialsException("Sessão inválida. Entra novamente.")
        val utilizador = token.utilizador ?: throw InvalidCredentialsException("Sessão inválida. Entra novamente.")
        val agora = clock.instant()
        val motivo = token.revogadoMotivo?.let { runCatching { RefreshTokenRegras.Motivo.valueOf(it) }.getOrNull() }
        when (RefreshTokenRegras.avaliar(token.expiraEm, token.revogadoEm, motivo, agora, toleranciaSegundos)) {
            RefreshTokenRegras.Estado.VALIDO -> Unit
            RefreshTokenRegras.Estado.EXPIRADO -> throw InvalidCredentialsException("A sessão expirou. Entra novamente.")
            RefreshTokenRegras.Estado.REVOGADO -> {
                // Um token já rodado voltou a aparecer (sinal de roubo), ou é de uma sessão que já terminou: nenhuma sessão
                // deste utilizador fica de pé, e (por não ser ROTACAO) nada disto passa pela tolerância.
                logger.warn("Refresh token revogado ({}) voltou a aparecer (utilizador {}): todas as sessões dele foram revogadas", token.revogadoMotivo, utilizador.id)
                repository.revogarTodosDoUtilizador(utilizador.id, agora, RefreshTokenRegras.Motivo.REUTILIZACAO.name)
                throw InvalidCredentialsException("A sessão terminou. Entra novamente.")
            }
        }
        if (token.revogadoEm == null) {
            token.revogadoEm = agora
            token.revogadoMotivo = RefreshTokenRegras.Motivo.ROTACAO.name
        }
        return utilizador to emitir(utilizador)
    }

    /** Termina a sessão do dispositivo: revoga esse refresh token. Idempotente (um token desconhecido não é erro). */
    @Transactional
    fun revogar(tokenPlano: String) {
        val token = repository.findByHashComUtilizador(RefreshTokenRegras.hash(tokenPlano)) ?: return
        if (token.revogadoEm == null) {
            token.revogadoEm = clock.instant()
            token.revogadoMotivo = RefreshTokenRegras.Motivo.LOGOUT.name
        }
    }

    /** Termina todas as sessões do utilizador (password redefinida): recusam-se logo, sem tolerância. */
    @Transactional
    fun revogarTodos(utilizadorId: Long) {
        repository.revogarTodosDoUtilizador(utilizadorId, clock.instant(), RefreshTokenRegras.Motivo.PASSWORD.name)
    }

    // Todos os dias às 04:30 (Europe/Lisbon): apaga os expirados há mais de 7 dias.
    @Scheduled(cron = "\${jwt.refresh-limpeza-cron:0 30 4 * * *}", zone = "Europe/Lisbon")
    @Transactional
    fun limparExpirados() {
        val apagados = repository.apagarExpiradosAntesDe(clock.instant().minus(Duration.ofDays(7)))
        if (apagados > 0) logger.info("Limpeza de refresh tokens: {} expirados apagados", apagados)
    }
}
