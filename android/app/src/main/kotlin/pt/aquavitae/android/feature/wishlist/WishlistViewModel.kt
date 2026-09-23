package pt.aquavitae.android.feature.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.repository.WishlistRepository
import javax.inject.Inject

/** Estado do ecrã de wishlist. */
sealed interface WishlistUiState {
    data object Loading : WishlistUiState
    data class Success(val bebidas: List<BebidaRelacao>) : WishlistUiState
    data class Error(val message: String) : WishlistUiState
}

/**
 * ViewModel da wishlist (distinta da Cave e dos Favoritos). Placeholder de UI,
 * já ligado ao [WishlistRepository] real.
 */
@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        _uiState.value = WishlistUiState.Loading
        viewModelScope.launch {
            wishlistRepository.getWishlist()
                .onSuccess { _uiState.value = WishlistUiState.Success(it) }
                .onFailure { _uiState.value = WishlistUiState.Error(it.message ?: "Não foi possível carregar a wishlist.") }
        }
    }

    fun removeFromWishlist(bebidaId: Long) {
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(bebidaId)
                .onSuccess { loadWishlist() }
                .onFailure { _uiState.value = WishlistUiState.Error(it.message ?: "Não foi possível remover.") }
        }
    }
}
