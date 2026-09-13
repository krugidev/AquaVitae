package pt.aquavitae.android.feature.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.repository.FavoritoRepository
import javax.inject.Inject

/** Estado do ecrã de favoritos. */
sealed interface FavoritosUiState {
    data object Loading : FavoritosUiState
    data class Success(val bebidas: List<BebidaSummary>) : FavoritosUiState
    data class Error(val message: String) : FavoritosUiState
}

/**
 * ViewModel dos favoritos (distintos da Cave e da Wishlist). Placeholder de
 * UI, já ligado ao [FavoritoRepository] real.
 */
@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val favoritoRepository: FavoritoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritosUiState>(FavoritosUiState.Loading)
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        loadFavoritos()
    }

    fun loadFavoritos() {
        _uiState.value = FavoritosUiState.Loading
        viewModelScope.launch {
            favoritoRepository.getFavoritos()
                .onSuccess { _uiState.value = FavoritosUiState.Success(it) }
                .onFailure { _uiState.value = FavoritosUiState.Error(it.message ?: "Não foi possível carregar os favoritos.") }
        }
    }

    fun removeFavorito(bebidaId: Long) {
        viewModelScope.launch {
            favoritoRepository.removeFavorito(bebidaId)
                .onSuccess { loadFavoritos() }
                .onFailure { _uiState.value = FavoritosUiState.Error(it.message ?: "Não foi possível remover.") }
        }
    }
}
