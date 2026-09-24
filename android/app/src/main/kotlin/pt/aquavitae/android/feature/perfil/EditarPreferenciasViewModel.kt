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
import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.PreferenciaResponse
import pt.aquavitae.android.data.model.contemSemAcentos
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.PreferenciaRepository
import javax.inject.Inject

private const val MAX_CASTAS_SUGERIDAS = 12

/** Estado do popup "Editar preferências" (`android/design/perfil/04-editar-preferencias.png`). */
sealed interface EditarPreferenciasUiState {
    data object Loading : EditarPreferenciasUiState
    data class Error(val message: String) : EditarPreferenciasUiState
    data class Ready(
        val categorias: List<LookupItem> = emptyList(),
        val castas: List<Casta> = emptyList(),
        val categoriaIds: Set<Long> = emptySet(),
        val acidezMin: Int? = null,
        val acidezMax: Int? = null,
        val docuraMin: Int? = null,
        val docuraMax: Int? = null,
        val castaIds: Set<Long> = emptySet(),
        val castaBusca: String = "",
        val aGuardar: Boolean = false,
        val erro: String? = null,
    ) : EditarPreferenciasUiState {
        val castasEscolhidas: List<Casta> get() = castaIds.mapNotNull { id -> castas.firstOrNull { it.id == id } }

        val castasSugeridas: List<Casta>
            get() = if (castaBusca.isBlank()) {
                emptyList()
            } else {
                castas.filter { it.id !in castaIds && it.nome?.contemSemAcentos(castaBusca) == true }
                    .take(MAX_CASTAS_SUGERIDAS)
            }
    }
}

/**
 * Editar as preferências (tipos de bebida, acidez/doçura, castas) — as mesmas do onboarding, aqui num popup só com
 * pílulas horizontais (em vez das listas paginadas do onboarding). Alimentam "Escolhido para ti" na homepage.
 */
@HiltViewModel
class EditarPreferenciasViewModel @Inject constructor(
    private val preferenciaRepository: PreferenciaRepository,
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<EditarPreferenciasUiState>(EditarPreferenciasUiState.Loading)
    val state: StateFlow<EditarPreferenciasUiState> = _state.asStateFlow()

    private var inicializado = false

    /** Chamado uma vez, com os valores já carregados pelo `PerfilViewModel` (evita um `GET` duplicado). */
    fun inicializar(atuais: PreferenciaResponse) {
        if (inicializado) return
        inicializado = true
        _state.value = EditarPreferenciasUiState.Loading
        viewModelScope.launch {
            val categorias = lookupRepository.categoriasBebida().getOrDefault(emptyList())
            val castas = lookupRepository.castas().getOrDefault(emptyList())
            _state.value = EditarPreferenciasUiState.Ready(
                categorias = categorias,
                castas = castas,
                categoriaIds = atuais.categoriaIds.toSet(),
                acidezMin = atuais.acidezMin,
                acidezMax = atuais.acidezMax,
                docuraMin = atuais.docuraMin,
                docuraMax = atuais.docuraMax,
                castaIds = atuais.castaIds.toSet(),
            )
        }
    }

    fun toggleCategoria(id: Long) = atualizarPronto {
        copy(categoriaIds = if (id in categoriaIds) categoriaIds - id else categoriaIds + id)
    }

    fun onAcidez(min: Int, max: Int) = atualizarPronto { copy(acidezMin = min, acidezMax = max) }

    fun onDocura(min: Int, max: Int) = atualizarPronto { copy(docuraMin = min, docuraMax = max) }

    fun onCastaBusca(texto: String) = atualizarPronto { copy(castaBusca = texto) }

    fun toggleCasta(id: Long) = atualizarPronto {
        copy(castaIds = if (id in castaIds) castaIds - id else castaIds + id, castaBusca = "")
    }

    fun guardar(onGuardado: (PreferenciaResponse) -> Unit) {
        val atual = _state.value as? EditarPreferenciasUiState.Ready ?: return
        atualizarPronto { copy(aGuardar = true, erro = null) }
        viewModelScope.launch {
            val request = PreferenciaRequest(
                acidezMin = atual.acidezMin,
                acidezMax = atual.acidezMax,
                docuraMin = atual.docuraMin,
                docuraMax = atual.docuraMax,
                categoriaIds = atual.categoriaIds.toList(),
                castaIds = atual.castaIds.toList(),
            )
            preferenciaRepository.updatePreferencias(request)
                .onSuccess {
                    atualizarPronto { copy(aGuardar = false) }
                    onGuardado(
                        PreferenciaResponse(
                            acidezMin = atual.acidezMin,
                            acidezMax = atual.acidezMax,
                            docuraMin = atual.docuraMin,
                            docuraMax = atual.docuraMax,
                            categoriaIds = atual.categoriaIds.toList(),
                            castaIds = atual.castaIds.toList(),
                        ),
                    )
                }
                .onFailure { atualizarPronto { copy(aGuardar = false, erro = it.toUserMessage()) } }
        }
    }

    private fun atualizarPronto(transform: EditarPreferenciasUiState.Ready.() -> EditarPreferenciasUiState.Ready) {
        _state.update { (it as? EditarPreferenciasUiState.Ready)?.transform() ?: it }
    }
}
