package pt.aquavitae.android.feature.cave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.repository.CaveRepository
import javax.inject.Inject

/** Estado do ecrã da Cave Virtual (lista de caves do utilizador). */
sealed interface CaveUiState {
    data object Loading : CaveUiState
    data class Success(val caves: List<CaveResponse>) : CaveUiState
    data class Error(val message: String) : CaveUiState
}

/**
 * ViewModel da Cave Virtual. Placeholder de UI, já ligado ao [CaveRepository]
 * real: lista as caves do utilizador e permite criar uma nova.
 */
@HiltViewModel
class CaveViewModel @Inject constructor(
    private val caveRepository: CaveRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaveUiState>(CaveUiState.Loading)
    val uiState: StateFlow<CaveUiState> = _uiState.asStateFlow()

    init {
        loadCaves()
    }

    fun loadCaves() {
        _uiState.value = CaveUiState.Loading
        viewModelScope.launch {
            caveRepository.getCaves()
                .onSuccess { _uiState.value = CaveUiState.Success(it) }
                .onFailure { _uiState.value = CaveUiState.Error(it.message ?: "Não foi possível carregar as caves.") }
        }
    }

    fun createCave(nome: String, descricao: String?) {
        viewModelScope.launch {
            caveRepository.createCave(nome, descricao)
                .onSuccess { loadCaves() }
                .onFailure { _uiState.value = CaveUiState.Error(it.message ?: "Não foi possível criar a cave.") }
        }
    }
}
