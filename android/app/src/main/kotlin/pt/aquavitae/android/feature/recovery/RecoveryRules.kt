package pt.aquavitae.android.feature.recovery

/** O código de recuperação tem 6 dígitos (é o que o backend gera; o desenho tinha 5 espaços e usa-se 6). */
const val CODE_LENGTH = 6

// Limites que a API também aplica à password nova (RedefinirPasswordRequest): 8 a 72, como no registo.
private const val PASSWORD_MIN = 8
private const val PASSWORD_MAX = 72

/** Que campo tem o problema (o ecrã pinta a linha desse campo de vermelho). */
enum class RecoveryField { Identificador, Codigo, NovaPassword, RepetirPassword }

/** A frase a mostrar e o campo com problema (`null` = um erro que não é de um campo, ex.: sem ligação). */
data class RecoveryProblem(val message: String, val field: RecoveryField? = null)

/**
 * O que se escreveu no 1.º passo é um email? Um username não pode ter `@` (ver `validateRegister`), por isso o `@` chega
 * para os distinguir.
 */
fun looksLikeEmail(identificador: String): Boolean = identificador.contains('@')

/**
 * O email com a parte do nome quase toda tapada (`demouser@gmail.com` → `******user@gmail.com`), como no desenho do ecrã do
 * código. Só se mostra a quem escreveu o email (e portanto já o conhece): mostrar o email de uma conta a quem escreveu
 * um username revelaria dados de uma conta alheia.
 */
fun maskEmail(email: String): String {
    val at = email.lastIndexOf('@')
    if (at < 0) return email
    val local = email.substring(0, at)
    val visible = if (local.length <= 1) 0 else (local.length / 2).coerceIn(1, 4)
    return "******" + local.takeLast(visible) + email.substring(at)
}

/** Valida a password nova e a repetição antes de enviar (a API só devolveria mensagens técnicas em inglês). */
fun validateNewPassword(nova: String, repetida: String): RecoveryProblem? = when {
    nova.length < PASSWORD_MIN ->
        RecoveryProblem("A password tem de ter pelo menos $PASSWORD_MIN caracteres.", RecoveryField.NovaPassword)

    nova.length > PASSWORD_MAX ->
        RecoveryProblem("A password pode ter no máximo $PASSWORD_MAX caracteres.", RecoveryField.NovaPassword)

    nova != repetida ->
        RecoveryProblem("As passwords não coincidem.", RecoveryField.RepetirPassword)

    else -> null
}
