package pt.aquavitae.android.feature.compra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.CliquePendente
import pt.aquavitae.android.data.model.RespostaCliqueRequest
import pt.aquavitae.android.data.repository.AuthRepository
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.CompraRepository
import javax.inject.Inject

/**
 * O inquérito "Compraste?" (briefing, secção 4): depois de clicar em "Comprar" o utilizador sai para a loja; quando a app
 * volta ao primeiro plano, se há um clique com mais de 2 minutos ainda por perguntar (e menos de 72 horas), pergunta se
 * chegou a comprar. "Sim" regista a resposta e abre o "Adicionar à cave" com o preço do link; "Não comprei" regista e
 * cala; fechar sem responder ("Mais tarde") não regista nada e não volta a perguntar **neste arranque da app** (o
 * clique continua por perguntar e reaparece no próximo).
 */
data class CompraPromptUiState(
    val pergunta: CliquePendente? = null,
    val aResponder: Boolean = false,
    /** Depois de "Sim": a bebida para o popup "Adicionar à cave" (com `precoSugerido`, o preço do link em que clicou). */
    val bebidaParaCave: BebidaDetail? = null,
    val precoSugerido: Double? = null,
)

@HiltViewModel
class CompraPromptViewModel @Inject constructor(
    private val compraRepository: CompraRepository,
    private val bebidaRepository: BebidaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CompraPromptUiState())
    val state: StateFlow<CompraPromptUiState> = _state.asStateFlow()

    // Os cliques já mostrados neste arranque (o "Mais tarde" não pode fazer a pergunta voltar a cada regresso à app).
    private val jaMostrados = mutableSetOf<Long>()
    private var aVerificar = false

    /** Chamado sempre que a app volta ao primeiro plano (`ON_RESUME`). Sem sessão, ou com um popup já aberto, não faz nada. */
    fun verificar() {
        val atual = _state.value
        if (aVerificar || atual.pergunta != null || atual.bebidaParaCave != null) return
        aVerificar = true
        viewModelScope.launch {
            try {
                if (!authRepository.isLoggedIn.first()) return@launch
                compraRepository.getCliquesPendentes().onSuccess { lista ->
                    val proximo = lista.firstOrNull { it.id !in jaMostrados } ?: return@onSuccess
                    jaMostrados += proximo.id
                    _state.update { it.copy(pergunta = proximo) }
                }
            } finally {
                aVerificar = false
            }
        }
    }

    fun maisTarde() = _state.update { it.copy(pergunta = null) }

    fun naoComprei() {
        val clique = _state.value.pergunta ?: return
        _state.update { it.copy(pergunta = null) }
        viewModelScope.launch { compraRepository.responder(clique.id, RespostaCliqueRequest.NAO_COMPREI) }
    }

    fun comprei() {
        val clique = _state.value.pergunta ?: return
        _state.update { it.copy(aResponder = true) }
        viewModelScope.launch {
            compraRepository.responder(clique.id, RespostaCliqueRequest.COMPREI)
            bebidaRepository.getBebidaDetail(clique.bebidaId)
                .onSuccess { bebida -> _state.update { it.copy(pergunta = null, aResponder = false, bebidaParaCave = bebida, precoSugerido = clique.preco) } }
                // Sem o detalhe não há como abrir o popup da cave: a resposta já ficou registada, só se fecha.
                .onFailure { _state.update { it.copy(pergunta = null, aResponder = false) } }
        }
    }

    fun fecharCave() = _state.update { it.copy(bebidaParaCave = null, precoSugerido = null) }
}
