package pt.aquavitae.android.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.navigation.AppDestinations
import javax.inject.Inject

/** Estado do ecrã de detalhe de uma bebida. */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val bebida: BebidaDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

/**
 * ViewModel do ecrã de detalhe. Placeholder de UI, mas já ligado ao
 * [BebidaRepository] real: recebe o id da bebida via argumento de navegação
 * (SavedStateHandle) e carrega o [BebidaDetail] correspondente, incluindo a
 * secção condicional [pt.aquavitae.android.data.model.VinhoDetalhe] quando a
 * categoria for "Vinho".
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bebidaRepository: BebidaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bebidaId: Long = checkNotNull(savedStateHandle[AppDestinations.ARG_BEBIDA_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            bebidaRepository.getBebidaDetail(bebidaId)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { _uiState.value = DetailUiState.Error(it.message ?: "Não foi possível carregar a bebida.") }
        }
    }
}
