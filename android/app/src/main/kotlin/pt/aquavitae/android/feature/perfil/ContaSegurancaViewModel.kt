package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

/**
 * Os dados que a folha "Conta e segurança" mostra e de que os seus dois pedidos precisam: o email (só leitura; para onde vai o código
 * do "Alterar password") e o que se perde ao apagar a conta (os totais do perfil). Pede-os à API ao abrir, para a folha funcionar da
 * mesma maneira a partir do perfil e do "Editar perfil".
 */
@HiltViewModel
class ContaSegurancaViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _utilizador = MutableStateFlow<UtilizadorMe?>(null)
    val utilizador: StateFlow<UtilizadorMe?> = _utilizador.asStateFlow()

    fun carregar() {
        viewModelScope.launch { userRepository.getMe().onSuccess { _utilizador.value = it } }
    }
}
