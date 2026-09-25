package pt.aquavitae.android.feature.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.local.TokenDataStore
import pt.aquavitae.android.data.network.isSessionInvalid
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

sealed interface SessionState {
    /** A verificar se há sessão (a mostrar o ecrã de loading). */
    data object Checking : SessionState

    /** Sem sessão (ou expirada, ou os termos mudaram): vai para o login. */
    data object NeedsLogin : SessionState

    /** Sessão válida: vai para a app. */
    data object LoggedIn : SessionState

    /** Havia sessão mas não foi possível falar com o servidor: mostra "Tentar de novo". */
    data class Offline(val message: String) : SessionState
}

// A marca fica no ecrã pelo menos este tempo, mesmo que a resposta seja instantânea (evita um "flash").
private const val MIN_LOADING_MS = 1200L

/**
 * Decide para onde a app arranca. Sem token: login. Com token: `GET /api/users/me`; se o servidor recusar o token (sessão
 * expirada) apaga-se a sessão e vai-se para o login; se o servidor não responder, fica no loading com "Tentar de novo".
 * Se `precisaAceitarTermos` (os termos mudaram desde a última vez), volta-se ao login, onde o popup dos termos aparece.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        check()
    }

    fun check() {
        _state.value = SessionState.Checking
        viewModelScope.launch {
            val started = System.currentTimeMillis()
            val token = tokenDataStore.tokenFlow.first()
            val result: SessionState = if (token.isNullOrBlank()) {
                SessionState.NeedsLogin
            } else {
                userRepository.getMe().fold(
                    onSuccess = { me ->
                        if (me.precisaAceitarTermos) {
                            tokenDataStore.clearSession()
                            SessionState.NeedsLogin
                        } else {
                            SessionState.LoggedIn
                        }
                    },
                    onFailure = { error ->
                        if (error.isSessionInvalid()) {
                            tokenDataStore.clearSession()
                            SessionState.NeedsLogin
                        } else {
                            SessionState.Offline(error.toUserMessage())
                        }
                    },
                )
            }
            if (result !is SessionState.Offline) {
                val remaining = MIN_LOADING_MS - (System.currentTimeMillis() - started)
                if (remaining > 0) delay(remaining)
            }
            _state.value = result
        }
    }
}
