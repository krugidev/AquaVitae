package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.PreferenciaResponse
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.AuthRepository
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.PreferenciaRepository
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

/** Estado do ecrã "Detalhes do perfil" (`android/design/perfil/01-perfil.png`). */
sealed interface PerfilUiState {
    data object Loading : PerfilUiState
    data class Error(val message: String) : PerfilUiState
    data class Ready(
        val utilizador: UtilizadorMe,
        val preferencias: PreferenciaResponse,
        val categorias: List<LookupItem> = emptyList(),
        val castas: List<Casta> = emptyList(),
        val mostrarEditarPreferencias: Boolean = false,
    ) : PerfilUiState {
        /** Nomes das categorias preferidas, pela mesma ordem em que a API as devolveu. */
        val categoriaNomes: List<String>
            get() = preferencias.categoriaIds.mapNotNull { id -> categorias.firstOrNull { it.id == id }?.nome }

        val castaNomes: List<String>
            get() = preferencias.castaIds.mapNotNull { id -> castas.firstOrNull { it.id == id }?.nome }
    }
}

/**
 * O perfil do utilizador: dados + estatísticas + resumo das preferências (fatia 5). `GET /users/me` já traz as 6
 * estatísticas (provadas/reviews/favoritos/wishlist/caves/garrafas); as preferências e os nomes de categoria/casta
 * (só a API devolve os ids) vêm de pedidos à parte.
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferenciaRepository: PreferenciaRepository,
    private val lookupRepository: LookupRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = PerfilUiState.Loading
        viewModelScope.launch {
            userRepository.getMe()
                .onSuccess { utilizador ->
                    val preferencias = preferenciaRepository.getPreferencias().getOrDefault(PreferenciaResponse())
                    val categorias = lookupRepository.categoriasBebida().getOrDefault(emptyList())
                    val castas = lookupRepository.castas().getOrDefault(emptyList())
                    _state.value = PerfilUiState.Ready(
                        utilizador = utilizador,
                        preferencias = preferencias,
                        categorias = categorias,
                        castas = castas,
                    )
                }
                .onFailure { _state.value = PerfilUiState.Error(it.toUserMessage()) }
        }
    }

    fun abrirEditarPreferencias() = atualizarPronto { copy(mostrarEditarPreferencias = true) }

    fun fecharEditarPreferencias() = atualizarPronto { copy(mostrarEditarPreferencias = false) }

    /** Chamado pelo popup de preferências depois de gravar — evita um pedido novo a `GET /users/me`. */
    fun preferenciasAtualizadas(novas: PreferenciaResponse) =
        atualizarPronto { copy(preferencias = novas, mostrarEditarPreferencias = false) }

    fun terminarSessao(onTerminada: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onTerminada()
        }
    }

    private fun atualizarPronto(transform: PerfilUiState.Ready.() -> PerfilUiState.Ready) {
        _state.update { (it as? PerfilUiState.Ready)?.transform() ?: it }
    }
}
