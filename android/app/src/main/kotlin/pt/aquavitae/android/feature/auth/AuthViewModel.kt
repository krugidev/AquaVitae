package pt.aquavitae.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.AuthRepository
import pt.aquavitae.android.data.repository.UserRepository
import retrofit2.HttpException
import javax.inject.Inject

private data class PendingRegister(val username: String, val email: String, val password: String)

/**
 * ViewModel de autenticação (login e registo). Estado exposto por [StateFlow] de uma sealed interface ([AuthUiState]);
 * chamadas ao repositório dentro de [viewModelScope], sem lógica de rede na UI.
 *
 * Termos e condições: no registo, o popup aparece ANTES de enviar (a API só cria a conta com `aceitouTermos = true`);
 * no login, aparece DEPOIS, se `GET /api/users/me` disser `precisaAceitarTermos` (contas anteriores aos termos, ou que
 * aceitaram uma versão anterior).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var pendingRegister: PendingRegister? = null

    fun login(identificador: String, password: String) {
        val id = identificador.trim()
        if (id.isEmpty()) {
            _uiState.value = AuthUiState.Error("Escreve o teu username ou email.", AuthField.Identificador)
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Escreve a tua password.", AuthField.Password)
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(id, password).fold(
                onSuccess = { afterLogin() },
                onFailure = { _uiState.value = AuthUiState.Error(it.toUserMessage()) },
            )
        }
    }

    // Se o /me falhar não se bloqueia o utilizador: entra, e os termos voltam a ser pedidos na próxima abertura da app.
    private suspend fun afterLogin() {
        val me = userRepository.getMe().getOrNull()
        _uiState.value = if (me?.precisaAceitarTermos == true) {
            AuthUiState.NeedsTerms(TermsContext.Login)
        } else {
            AuthUiState.Done(AuthDestination.Home)
        }
    }

    /** Valida o formulário e, se estiver bem, mostra o popup dos termos (o registo só segue quando se aceita). */
    fun register(username: String, email: String, password: String) {
        val user = username.trim()
        val mail = email.trim()
        validateRegister(user, mail, password)?.let {
            _uiState.value = AuthUiState.Error(it.message, it.field)
            return
        }
        pendingRegister = PendingRegister(user, mail, password)
        _uiState.value = AuthUiState.NeedsTerms(TermsContext.Register)
    }

    fun acceptTerms() {
        val current = _uiState.value as? AuthUiState.NeedsTerms ?: return
        if (current.loading) return
        _uiState.value = current.copy(loading = true)
        viewModelScope.launch {
            when (current.context) {
                TermsContext.Register -> {
                    val pending = pendingRegister ?: run { _uiState.value = AuthUiState.Idle; return@launch }
                    authRepository.register(pending.username, pending.email, pending.password, aceitouTermos = true).fold(
                        onSuccess = {
                            pendingRegister = null
                            _uiState.value = AuthUiState.Done(AuthDestination.Onboarding)
                        },
                        onFailure = { _uiState.value = registerError(it) },
                    )
                }

                TermsContext.Login -> userRepository.aceitarTermos().fold(
                    onSuccess = { _uiState.value = AuthUiState.Done(AuthDestination.Home) },
                    onFailure = { _uiState.value = AuthUiState.Error(it.toUserMessage()) },
                )
            }
        }
    }

    /** "Agora não" no popup. No registo volta ao formulário; no login a sessão fecha (não se entra sem aceitar). */
    fun declineTerms() {
        val current = _uiState.value as? AuthUiState.NeedsTerms ?: return
        if (current.loading) return
        if (current.context == TermsContext.Login) {
            viewModelScope.launch { authRepository.logout() }
        }
        pendingRegister = null
        _uiState.value = AuthUiState.Idle
    }

    /** O utilizador voltou a escrever: apaga a mensagem de erro. */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    /** Repõe o estado, ex.: depois de navegar para longe do ecrã. */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // 409: a mensagem da API diz se o conflito é no email ou no username, e pinta-se o campo certo.
    private fun registerError(error: Throwable): AuthUiState.Error {
        val message = error.toUserMessage()
        val field = if (error is HttpException && error.code() == 409) {
            when {
                message.contains("email", ignoreCase = true) -> AuthField.Email
                message.contains("username", ignoreCase = true) -> AuthField.Username
                else -> null
            }
        } else {
            null
        }
        return AuthUiState.Error(message, field)
    }
}
