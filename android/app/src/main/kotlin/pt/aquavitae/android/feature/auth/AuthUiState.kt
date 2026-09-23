package pt.aquavitae.android.feature.auth

/** Que campo tem o erro (o ecrã pinta a linha desse campo de vermelho). */
enum class AuthField { Identificador, Username, Email, Password }

/** Porque é que o popup dos termos aparece. */
enum class TermsContext {
    /** Antes de criar a conta (o registo só segue depois de aceitar). */
    Register,

    /** Depois do login, de quem nunca aceitou ou aceitou uma versão anterior. */
    Login,
}

/** Para onde ir quando a autenticação acaba. */
enum class AuthDestination { Home, Onboarding }

/** Estado da UI de autenticação (login e registo partilham o mesmo ViewModel/estado). */
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String, val field: AuthField? = null) : AuthUiState

    /** O popup dos termos está por mostrar; `loading` enquanto se envia a aceitação. */
    data class NeedsTerms(val context: TermsContext, val loading: Boolean = false) : AuthUiState

    /** Autenticação concluída: o ecrã navega e repõe o estado. */
    data class Done(val destination: AuthDestination) : AuthUiState
}
