package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.network.SessionEvents
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.AuthRepository
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

data class ApagarContaUiState(
    val password: String = "",
    val aApagar: Boolean = false,
    val erro: String? = null,
)

/**
 * O popup de confirmação de "Apagar conta": pede a password da conta e chama `POST /api/users/me/apagar` (apaga caves, garrafas,
 * wishlist, favoritos, provadas, reviews e preferências; não se desfaz). Se correu bem, termina a sessão neste telemóvel e avisa
 * o login ("A tua conta foi apagada."). Password errada → 403 "Password incorreta." (a mensagem da API passa tal e qual).
 */
@HiltViewModel
class ApagarContaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val sessionEvents: SessionEvents,
) : ViewModel() {

    private val _state = MutableStateFlow(ApagarContaUiState())
    val state: StateFlow<ApagarContaUiState> = _state.asStateFlow()

    /** Ao abrir o popup: recomeça sem a password de uma abertura anterior. */
    fun iniciar() {
        _state.value = ApagarContaUiState()
    }

    fun onPasswordChange(valor: String) = _state.update { it.copy(password = valor, erro = null) }

    fun apagar(onApagada: () -> Unit) {
        val atual = _state.value
        if (atual.aApagar) return
        if (atual.password.isEmpty()) {
            _state.update { it.copy(erro = "Escreve a tua password para confirmar.") }
            return
        }
        _state.update { it.copy(aApagar = true, erro = null) }
        viewModelScope.launch {
            userRepository.apagarConta(atual.password).fold(
                onSuccess = {
                    // A conta já não existe: limpa a sessão do telemóvel (o `logout` também tenta revogar o token no servidor,
                    // sem problema se já não existe) e diz ao login porque se voltou lá.
                    authRepository.logout()
                    sessionEvents.notificarContaApagada()
                    _state.update { it.copy(aApagar = false, password = "") }
                    onApagada()
                },
                onFailure = { e -> _state.update { it.copy(aApagar = false, erro = e.toUserMessage()) } },
            )
        }
    }
}
