package pt.aquavitae.android.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.repository.PreferenciaRepository
import javax.inject.Inject

/** Estado do ecrã de onboarding (preferências iniciais). */
sealed interface OnboardingUiState {
    data object Idle : OnboardingUiState
    data object Loading : OnboardingUiState
    data object Success : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState
}

/**
 * ViewModel do ecrã de onboarding (corpo/acidez/doçura, categorias e castas
 * preferidas). Ecrã placeholder na UI, mas já ligado ao [PreferenciaRepository]
 * real — só falta desenhar o formulário completo.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenciaRepository: PreferenciaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // TODO: expor seletores reais de intervalo (acidez/doçura 1-5) e listas de
    // categorias/castas obtidas de um endpoint de lookups quando existir.
    fun savePreferencias(
        acidezMin: Int? = null,
        acidezMax: Int? = null,
        docuraMin: Int? = null,
        docuraMax: Int? = null,
        categoriaIds: List<Long> = emptyList(),
        castaIds: List<Long> = emptyList(),
    ) {
        _uiState.value = OnboardingUiState.Loading
        viewModelScope.launch {
            preferenciaRepository.updatePreferencias(
                acidezMin = acidezMin,
                acidezMax = acidezMax,
                docuraMin = docuraMin,
                docuraMax = docuraMax,
                categoriaIds = categoriaIds,
                castaIds = castaIds,
            )
                .onSuccess { _uiState.value = OnboardingUiState.Success }
                .onFailure {
                    _uiState.value = OnboardingUiState.Error(it.message ?: "Não foi possível guardar as preferências.")
                }
        }
    }
}
