package pt.aquavitae.android.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.repository.BebidaRepository
import javax.inject.Inject

/** Estado do ecrã de catálogo (lista/pesquisa de bebidas). */
sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val bebidas: List<BebidaSummary>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

/**
 * ViewModel do catálogo. Segundo ecrã do fluxo MVP — implementado com lógica
 * real: chama [BebidaRepository.searchBebidas] e expõe loading/lista/erro.
 */
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val bebidaRepository: BebidaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        search()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun search(categoriaId: Long? = null) {
        _uiState.value = CatalogUiState.Loading
        viewModelScope.launch {
            bebidaRepository.searchBebidas(search = _query.value.ifBlank { null }, categoriaId = categoriaId)
                .onSuccess { page -> _uiState.value = CatalogUiState.Success(page.content) }
                .onFailure { _uiState.value = CatalogUiState.Error(it.message ?: "Não foi possível carregar o catálogo.") }
        }
    }
}
