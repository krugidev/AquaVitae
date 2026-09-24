package pt.aquavitae.android.feature.produtor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.navigation.AppDestinations
import javax.inject.Inject

/** Quantas garrafas a página mostra de início, e o máximo depois de "Carregar mais" (o resto é no catálogo do produtor). */
const val GARRAFAS_INICIAIS = 3
const val GARRAFAS_MAXIMO = 6

/** Estado da página de um produtor (`android/design/produtor/01-produtor.png`). */
sealed interface ProdutorUiState {
    data object Loading : ProdutorUiState
    data class Error(val message: String) : ProdutorUiState
    data class Ready(
        val produtor: ProdutorDetail,
        /** As primeiras [GARRAFAS_MAXIMO] do produtor (rating desc, como o catálogo). */
        val bebidas: List<BebidaSummary>,
        val expandido: Boolean = false,
        val historiaAberta: Boolean = false,
    ) : ProdutorUiState {
        /** As garrafas em ecrã: 3 de início, até 6 depois de "Carregar mais". */
        val visiveis: List<BebidaSummary> get() = bebidas.take(if (expandido) GARRAFAS_MAXIMO else GARRAFAS_INICIAIS)

        /** O "N" de "CARREGAR MAIS N": as que ainda faltam mostrar até ao máximo (0 = sem botão). */
        val maisParaCarregar: Int get() = if (expandido) 0 else (bebidas.size - GARRAFAS_INICIAIS).coerceIn(0, GARRAFAS_MAXIMO - GARRAFAS_INICIAIS)

        /** Depois de mostrar as 6, o resto só se vê no catálogo do produtor. */
        val temMaisNoCatalogo: Boolean get() = produtor.totalProdutos > GARRAFAS_MAXIMO
    }
}

/**
 * A página de um produtor (fatia 6): os detalhes (`GET /produtores/{id}`, já com o rating geral e as categorias) e as
 * suas primeiras 6 garrafas (`GET /produtores/{id}/bebidas?size=6`). O id vem do argumento de navegação.
 */
@HiltViewModel
class ProdutorViewModel @Inject constructor(
    private val bebidaRepository: BebidaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val produtorId: Long = checkNotNull(savedStateHandle[AppDestinations.ARG_PRODUTOR_ID])

    private val _state = MutableStateFlow<ProdutorUiState>(ProdutorUiState.Loading)
    val state: StateFlow<ProdutorUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = ProdutorUiState.Loading
        viewModelScope.launch {
            pedir()
                .onSuccess { (produtor, bebidas) -> _state.value = ProdutorUiState.Ready(produtor, bebidas) }
                .onFailure { _state.value = ProdutorUiState.Error(it.toUserMessage()) }
        }
    }

    /**
     * Volta a pedir os dados sem mostrar o carregamento nem perder o que o utilizador abriu (as 6 garrafas, o popup).
     * Chamado ao fechar o popup de detalhe de uma bebida: uma review nova muda o rating dela e o rating geral.
     */
    fun atualizar() {
        viewModelScope.launch {
            pedir().onSuccess { (produtor, bebidas) ->
                _state.update { (it as? ProdutorUiState.Ready)?.copy(produtor = produtor, bebidas = bebidas) ?: it }
            }
        }
    }

    fun expandir() = atualizarPronto { copy(expandido = true) }

    fun abrirHistoria() = atualizarPronto { copy(historiaAberta = true) }

    fun fecharHistoria() = atualizarPronto { copy(historiaAberta = false) }

    private suspend fun pedir(): Result<Pair<ProdutorDetail, List<BebidaSummary>>> = runCatching {
        coroutineScope {
            val detalhe = async { bebidaRepository.getProdutorDetail(produtorId).getOrThrow() }
            val bebidas = async { bebidaRepository.getBebidasDoProdutor(produtorId, page = 0, size = GARRAFAS_MAXIMO).getOrThrow().content }
            detalhe.await() to bebidas.await()
        }
    }

    private fun atualizarPronto(transform: ProdutorUiState.Ready.() -> ProdutorUiState.Ready) {
        _state.update { (it as? ProdutorUiState.Ready)?.transform() ?: it }
    }
}
