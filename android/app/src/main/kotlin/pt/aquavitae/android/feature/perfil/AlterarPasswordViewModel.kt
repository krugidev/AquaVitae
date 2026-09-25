package pt.aquavitae.android.feature.perfil

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
import pt.aquavitae.android.feature.recovery.CODE_LENGTH
import pt.aquavitae.android.feature.recovery.RecoveryField
import pt.aquavitae.android.feature.recovery.RecoveryProblem
import pt.aquavitae.android.feature.recovery.validateNewPassword
import pt.aquavitae.android.ui.components.CodigoContagem
import pt.aquavitae.android.ui.components.agoraParaContagem
import javax.inject.Inject

/** Os passos do "Alterar password": confirmar o envio, escrever o código, definir a password nova e o fim. */
enum class AlterarPasswordPasso { Enviar, Codigo, NovaPassword, Concluido }

data class AlterarPasswordUiState(
    val passo: AlterarPasswordPasso = AlterarPasswordPasso.Enviar,
    val email: String = "",
    val codigo: String = "",
    val novaPassword: String = "",
    val repetirPassword: String = "",
    val aCarregar: Boolean = false,
    /** Um pedido de código novo em curso (o link "PEDIR NOVO CÓDIGO" fica a "A ENVIAR…"). */
    val aPedirNovo: Boolean = false,
    val erro: RecoveryProblem? = null,
    /** Os contadores do código (validade e "pedir novo"), arrancados quando o servidor respondeu ao pedido. */
    val contagem: CodigoContagem? = null,
    /** No fim: este telemóvel continua com a sessão (renovada com a password nova). `false` = tem de voltar a entrar. */
    val sessaoMantida: Boolean = true,
)

/**
 * "Alterar password" da folha "Conta e segurança": o **mesmo sistema do código** da recuperação de password (pedido do utilizador,
 * em vez de pedir a password atual). O email da conta vem de fora e o servidor manda o código para lá; usa os mesmos 3 endpoints
 * (`/recuperar-password`, `/verificar-codigo`, `/redefinir-password`), com o email como identificador, e o mesmo temporizador de
 * 15 minutos com "pedir novo". Como redefinir a password revoga **todas** as sessões, no fim entra-se outra vez com a password
 * nova para este telemóvel não ficar sem sessão (os outros dispositivos ficam de facto sem ela).
 */
@HiltViewModel
class AlterarPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AlterarPasswordUiState())
    val state: StateFlow<AlterarPasswordUiState> = _state.asStateFlow()

    /** Ao abrir a folha: recomeça do 1.º passo (o ViewModel sobrevive entre aberturas). */
    fun iniciar(email: String) {
        _state.value = AlterarPasswordUiState(email = email)
    }

    fun onCodigoChange(valor: String) = _state.update { it.copy(codigo = valor, erro = null) }

    fun onNovaPasswordChange(valor: String) = _state.update { it.copy(novaPassword = valor, erro = null) }

    fun onRepetirPasswordChange(valor: String) = _state.update { it.copy(repetirPassword = valor, erro = null) }

    /** "ENVIAR CÓDIGO": pede o código (vai para o email da conta) e passa ao passo de o escrever. */
    fun enviarCodigo() = pedirCodigo(aPedirNovo = false)

    /** "PEDIR NOVO CÓDIGO": o servidor invalida o anterior e manda outro (passado o intervalo mínimo entre pedidos). */
    fun pedirNovoCodigo() {
        val atual = _state.value
        if (atual.aCarregar || atual.aPedirNovo || atual.passo != AlterarPasswordPasso.Codigo) return
        pedirCodigo(aPedirNovo = true)
    }

    private fun pedirCodigo(aPedirNovo: Boolean) {
        val atual = _state.value
        if (atual.aCarregar || atual.email.isBlank()) return
        _state.update { it.copy(aCarregar = !aPedirNovo, aPedirNovo = aPedirNovo, erro = null) }
        viewModelScope.launch {
            authRepository.recuperarPassword(atual.email).fold(
                onSuccess = { info ->
                    _state.update {
                        it.copy(
                            passo = AlterarPasswordPasso.Codigo,
                            codigo = "",
                            contagem = CodigoContagem.aPartirDe(info, agoraParaContagem()),
                            aCarregar = false,
                            aPedirNovo = false,
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(aCarregar = false, aPedirNovo = false, erro = RecoveryProblem(e.toUserMessage())) } },
            )
        }
    }

    /** "CONTINUAR" no passo do código: confere-o no servidor (401 "Código inválido"/"Código expirado") antes de pedir a password. */
    fun verificarCodigo() {
        val atual = _state.value
        if (atual.aCarregar) return
        if (atual.codigo.length != CODE_LENGTH) {
            _state.update { it.copy(erro = RecoveryProblem("O código tem $CODE_LENGTH dígitos.", RecoveryField.Codigo)) }
            return
        }
        _state.update { it.copy(aCarregar = true, erro = null) }
        viewModelScope.launch {
            authRepository.verificarCodigo(atual.email, atual.codigo).fold(
                onSuccess = { _state.update { it.copy(passo = AlterarPasswordPasso.NovaPassword, aCarregar = false) } },
                onFailure = { e ->
                    _state.update { it.copy(aCarregar = false, erro = RecoveryProblem(e.toUserMessage(), RecoveryField.Codigo)) }
                },
            )
        }
    }

    /** "GUARDAR PASSWORD": define a password nova e volta a entrar com ela (este telemóvel mantém a sessão). */
    fun guardar() {
        val atual = _state.value
        if (atual.aCarregar) return
        validateNewPassword(atual.novaPassword, atual.repetirPassword)?.let { problema ->
            _state.update { it.copy(erro = problema) }
            return
        }
        _state.update { it.copy(aCarregar = true, erro = null) }
        viewModelScope.launch {
            authRepository.redefinirPassword(atual.email, atual.codigo, atual.novaPassword).fold(
                onSuccess = {
                    // As sessões todas foram revogadas: entra-se de novo já com a password nova (nova sessão só para este telemóvel).
                    val sessaoMantida = authRepository.login(atual.email, atual.novaPassword).isSuccess
                    if (!sessaoMantida) authRepository.logout()
                    _state.update { it.copy(passo = AlterarPasswordPasso.Concluido, aCarregar = false, sessaoMantida = sessaoMantida, novaPassword = "", repetirPassword = "") }
                },
                onFailure = { e ->
                    _state.update { it.copy(aCarregar = false, erro = RecoveryProblem(e.toUserMessage(), RecoveryField.NovaPassword)) }
                },
            )
        }
    }

    /** A seta para trás: cada passo volta ao anterior (do 1.º devolve `false`: a folha fecha-se). */
    fun voltar(): Boolean {
        val atual = _state.value
        if (atual.aCarregar) return true
        when (atual.passo) {
            AlterarPasswordPasso.Enviar, AlterarPasswordPasso.Concluido -> return false
            AlterarPasswordPasso.Codigo -> _state.update { it.copy(passo = AlterarPasswordPasso.Enviar, codigo = "", erro = null) }
            AlterarPasswordPasso.NovaPassword -> _state.update { it.copy(passo = AlterarPasswordPasso.Codigo, erro = null) }
        }
        return true
    }
}
