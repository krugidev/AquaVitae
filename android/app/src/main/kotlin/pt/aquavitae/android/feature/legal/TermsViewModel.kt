package pt.aquavitae.android.feature.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.model.TermosTexto
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.LegalRepository
import javax.inject.Inject

/** Vai buscar o texto dos termos quando o popup abre pela 1.ª vez e guarda o estado (a carregar, erro ou pronto). */
@HiltViewModel
class TermsViewModel @Inject constructor(
    private val legalRepository: LegalRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<LookupState<TermosTexto>>(LookupState.Loading)
    val state: StateFlow<LookupState<TermosTexto>> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = LookupState.Loading
        viewModelScope.launch {
            _state.value = legalRepository.termos().fold(
                onSuccess = { LookupState.Ready(it) },
                onFailure = { LookupState.Error(it.toUserMessage()) },
            )
        }
    }
}
