package pt.aquavitae.android.feature.auth

/** Um campo inválido e a frase a mostrar. */
data class FieldProblem(val field: AuthField, val message: String)

// Limites que a API também aplica (RegisterRequest): username 3 a 30, password 8 a 72.
private const val USERNAME_MIN = 3
private const val USERNAME_MAX = 30
private const val PASSWORD_MIN = 8
private const val PASSWORD_MAX = 72

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/**
 * Valida o formulário de registo antes de o enviar (a API só devolve mensagens técnicas em inglês). Devolve o primeiro
 * problema, por ordem de campo, ou `null` se estiver tudo bem. Espera o username e o email já sem espaços nas pontas.
 *
 * O username não pode ter espaços nem `@`: no login o mesmo campo aceita username ou email, e um `@` tornaria ambíguo qual dos dois é.
 */
fun validateRegister(username: String, email: String, password: String): FieldProblem? = when {
    username.length !in USERNAME_MIN..USERNAME_MAX ->
        FieldProblem(AuthField.Username, "O username tem de ter entre $USERNAME_MIN e $USERNAME_MAX caracteres.")

    username.any { it.isWhitespace() || it == '@' } ->
        FieldProblem(AuthField.Username, "O username não pode ter espaços nem @.")

    !EMAIL_REGEX.matches(email) ->
        FieldProblem(AuthField.Email, "Escreve um email válido.")

    password.length < PASSWORD_MIN ->
        FieldProblem(AuthField.Password, "A password tem de ter pelo menos $PASSWORD_MIN caracteres.")

    password.length > PASSWORD_MAX ->
        FieldProblem(AuthField.Password, "A password pode ter no máximo $PASSWORD_MAX caracteres.")

    else -> null
}
