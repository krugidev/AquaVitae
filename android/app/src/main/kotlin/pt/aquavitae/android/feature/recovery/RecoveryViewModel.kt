package pt.aquavitae.android.feature.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.AuthRepository
import javax.inject.Inject

/** Os 3 passos da recuperação: identificar a conta, escrever o código recebido e definir a password nova. */
enum class RecoveryStep { Identify, Code, NewPassword }

data class RecoveryUiState(
    val step: RecoveryStep = RecoveryStep.Identify,
    val identificador: String = "",
    val codigo: String = "",
    val novaPassword: String = "",
    val repetirPassword: String = "",
    val loading: Boolean = false,
    val error: RecoveryProblem? = null,
    /** A password mudou: o ecrã navega para o login (que mostra o "Password alterada!"). */
    val done: Boolean = false,
)

/**
 * A recuperação de password ponta a ponta contra a API (`/recuperar-password`, `/verificar-codigo`, `/redefinir-password`).
 * Um só ViewModel para os 3 passos: o `identificador` e o código escritos ficam guardados para os passos seguintes.
 */
@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecoveryUiState())
    val state: StateFlow<RecoveryUiState> = _state.asStateFlow()

    fun onIdentificadorChange(value: String) = _state.update { it.copy(identificador = value, error = null) }

    fun onCodigoChange(value: String) = _state.update { it.copy(codigo = value, error = null) }

    fun onNovaPasswordChange(value: String) = _state.update { it.copy(novaPassword = value, error = null) }

    fun onRepetirPasswordChange(value: String) = _state.update { it.copy(repetirPassword = value, error = null) }

    /** Volta ao passo anterior. Devolve `false` se já estava no 1.º (o ecrã sai então para o login). */
    fun back(): Boolean {
        val atual = _state.value
        if (atual.loading) return true
        when (atual.step) {
            RecoveryStep.Identify -> return false
            // Voltar ao 1.º passo é pedir outro código: o que se tinha escrito já não serve.
            RecoveryStep.Code -> _state.update { it.copy(step = RecoveryStep.Identify, codigo = "", error = null) }
            RecoveryStep.NewPassword -> _state.update { it.copy(step = RecoveryStep.Code, error = null) }
        }
        return true
    }

    /** O botão de seguir: faz o que o passo atual pede (pedir o código, conferi-lo ou definir a password). */
    fun submit() {
        val atual = _state.value
        if (atual.loading) return
        val identificador = atual.identificador.trim()
        when (atual.step) {
            RecoveryStep.Identify -> {
                if (identificador.isEmpty()) {
                    return fail(RecoveryProblem("Escreve o teu username ou email.", RecoveryField.Identificador))
                }
                // A API responde 202 exista ou não a conta: seguir para o código não confirma que a conta existe.
                chamar({ authRepository.recuperarPassword(identificador) }, RecoveryField.Identificador) {
                    it.copy(step = RecoveryStep.Code, codigo = "")
                }
            }

            RecoveryStep.Code -> {
                if (atual.codigo.length != CODE_LENGTH) {
                    return fail(RecoveryProblem("O código tem $CODE_LENGTH dígitos.", RecoveryField.Codigo))
                }
                chamar({ authRepository.verificarCodigo(identificador, atual.codigo) }, RecoveryField.Codigo) {
                    it.copy(step = RecoveryStep.NewPassword)
                }
            }

            RecoveryStep.NewPassword -> {
                validateNewPassword(atual.novaPassword, atual.repetirPassword)?.let { return fail(it) }
                chamar({ authRepository.redefinirPassword(identificador, atual.codigo, atual.novaPassword) }, null) {
                    it.copy(done = true)
                }
            }
        }
    }

    private fun fail(problem: RecoveryProblem) = _state.update { it.copy(error = problem) }

    // Faz a chamada com o botão em "a carregar". Se a API recusar, o erro (em português) fica ligado ao campo `field`.
    private fun chamar(
        call: suspend () -> Result<Unit>,
        field: RecoveryField?,
        onSuccess: (RecoveryUiState) -> RecoveryUiState,
    ) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            call().fold(
                onSuccess = { _state.update { s -> onSuccess(s).copy(loading = false) } },
                onFailure = { e -> _state.update { s -> s.copy(loading = false, error = RecoveryProblem(e.toUserMessage(), field)) } },
            )
        }
    }
}
