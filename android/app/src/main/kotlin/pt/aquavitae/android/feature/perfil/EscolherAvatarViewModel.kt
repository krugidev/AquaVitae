package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.LookupRepository
import javax.inject.Inject

/** Estado do popup "Escolher avatar" (`android/design/perfil/03-escolher-avatar.png`). */
sealed interface EscolherAvatarUiState {
    data object Loading : EscolherAvatarUiState
    data class Error(val message: String) : EscolherAvatarUiState
    data class Ready(
        val categorias: List<LookupItem> = emptyList(),
        val avatares: List<Avatar> = emptyList(),
        // `null` = pílula "Todos" (sem equivalente no onboarding, só aqui).
        val categoriaId: Long? = null,
    ) : EscolherAvatarUiState {
        val visiveis: List<Avatar> get() = if (categoriaId == null) avatares else avatares.filter { it.categoriaId == categoriaId }
    }
}

/** Carrega as categorias e os avatares uma vez; a escolha em si fica em estado local do `EscolherAvatarSheet`. */
@HiltViewModel
class EscolherAvatarViewModel @Inject constructor(
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<EscolherAvatarUiState>(EscolherAvatarUiState.Loading)
    val state: StateFlow<EscolherAvatarUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = EscolherAvatarUiState.Loading
        viewModelScope.launch {
            val categorias = lookupRepository.avatarCategorias()
            val avatares = lookupRepository.avatares()
            if (categorias.isFailure || avatares.isFailure) {
                _state.value = EscolherAvatarUiState.Error((categorias.exceptionOrNull() ?: avatares.exceptionOrNull())!!.toUserMessage())
                return@launch
            }
            _state.value = EscolherAvatarUiState.Ready(categorias = categorias.getOrThrow(), avatares = avatares.getOrThrow())
        }
    }

    fun selecionarCategoria(id: Long?) = atualizarPronto { copy(categoriaId = id) }

    private fun atualizarPronto(transform: EscolherAvatarUiState.Ready.() -> EscolherAvatarUiState.Ready) {
        _state.update { (it as? EscolherAvatarUiState.Ready)?.transform() ?: it }
    }
}
