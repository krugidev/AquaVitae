package pt.aquavitae.android.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Como a camada de rede avisa a UI de que a sessão morreu (o [TokenAuthenticator] não conseguiu renovar o token: expirou
 * ou foi revogado). Dois fios: [sessaoExpirada], um evento único para o `AppNavHost` levar ao login, e [avisoNoLogin], o
 * estado que faz o login mostrar "A tua sessão expirou" até haver uma entrada bem-sucedida.
 */
@Singleton
class SessionEvents @Inject constructor() {

    private val _sessaoExpirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessaoExpirada: SharedFlow<Unit> = _sessaoExpirada.asSharedFlow()

    private val _avisoNoLogin = MutableStateFlow(false)
    val avisoNoLogin: StateFlow<Boolean> = _avisoNoLogin.asStateFlow()

    fun notificarSessaoExpirada() {
        _avisoNoLogin.value = true
        _sessaoExpirada.tryEmit(Unit)
    }

    private val _contaApagada = MutableStateFlow(false)

    /** A conta acabou de ser apagada (Conta e segurança): o login mostra "A tua conta foi apagada." até haver uma entrada. */
    val contaApagada: StateFlow<Boolean> = _contaApagada.asStateFlow()

    fun notificarContaApagada() {
        _contaApagada.value = true
    }

    /** O utilizador voltou a entrar: os avisos já não fazem sentido. */
    fun limparAviso() {
        _avisoNoLogin.value = false
        _contaApagada.value = false
    }
}
