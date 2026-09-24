package pt.aquavitae.api.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

/**
 * As regras (puras, sem BD) do "refresh token" — o token opaco de longa duração que a app troca por um novo par de tokens
 * quando o de acesso (JWT, 60 min) expira, sem pedir a password outra vez.
 */
object RefreshTokenRegras {

    enum class Estado {
        /** Pode usar-se: não expirou e não foi revogado (ou foi rodado há tão pouco tempo que conta como o mesmo pedido repetido). */
        VALIDO,

        /** Passou a data de validade: a sessão acabou, o utilizador tem de entrar de novo. */
        EXPIRADO,

        /** Já foi usado (rodado) há mais do que a tolerância, ou foi revogado por outro motivo: sinal de token roubado ou de sessão terminada. */
        REVOGADO,
    }

    /** Porque é que um token foi revogado (a coluna `refresh_token_revogado_motivo`). */
    enum class Motivo {
        /** Usado numa renovação: emitiu-se outro no seu lugar. É o único que a tolerância aceita. */
        ROTACAO,

        /** O utilizador terminou a sessão neste dispositivo. */
        LOGOUT,

        /** A password mudou: todas as sessões antigas deixam de servir. */
        PASSWORD,

        /** Um token já rodado voltou a aparecer (sinal de roubo): todas as sessões do utilizador foram revogadas. */
        REUTILIZACAO,
    }

    /**
     * Estado de um token. **Tolerância** (`toleranciaSegundos`): um token **rodado** ([Motivo.ROTACAO]) há poucos segundos ainda
     * se aceita — a renovação roda o token, e se a resposta se perde (a app perdeu rede a meio) a app repete o pedido com o
     * token *antigo*; sem isto isso passava por "reutilização" e apagava a sessão de quem só estava com má rede. **Só vale para
     * a rotação**: um token revogado por logout, por password nova ou por reutilização recusa-se logo (apanhado ao testar: com
     * a tolerância para tudo, uma revogação em massa ainda deixava usar os tokens durante 60 segundos).
     */
    fun avaliar(expiraEm: Instant, revogadoEm: Instant?, motivo: Motivo?, agora: Instant, toleranciaSegundos: Long): Estado {
        if (!agora.isBefore(expiraEm)) return Estado.EXPIRADO
        if (revogadoEm == null) return Estado.VALIDO
        val rodadoHaPouco = motivo == Motivo.ROTACAO && Duration.between(revogadoEm, agora).seconds <= toleranciaSegundos
        return if (rodadoHaPouco) Estado.VALIDO else Estado.REVOGADO
    }

    /** 32 bytes aleatórios (256 bits) em Base64 URL sem padding (43 caracteres): impossível de adivinhar. */
    fun gerar(random: SecureRandom): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** SHA-256 em hexadecimal (64 caracteres): é o que se guarda — a tabela sozinha não serve para usar os tokens. */
    fun hash(tokenPlano: String): String =
        MessageDigest.getInstance("SHA-256").digest(tokenPlano.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
