package pt.aquavitae.android.feature.cave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.CaveDetailResponse
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.CaveRepository
import pt.aquavitae.android.data.repository.ProvadaRepository
import javax.inject.Inject

enum class OrdemEmGuarda(val label: String, val apiValue: String) {
    DATA_CONSUMO("Janela de consumo", "dataConsumo"),
    PRECO("Preço", "preco"),
}

/** Estado do ecrã "As minhas Caves" (`android/design/caves/01-caves-lista.png`). */
sealed interface CaveUiState {
    data object Loading : CaveUiState
    data class Error(val message: String) : CaveUiState
    data class Ready(
        val caves: List<CaveResponse> = emptyList(),
        val caveSelecionadaId: Long? = null,
        val detalhe: CaveDetailResponse? = null,
        val carregandoDetalhe: Boolean = false,
        val ordem: OrdemEmGuarda = OrdemEmGuarda.DATA_CONSUMO,
        val mostrarNovaCave: Boolean = false,
        val aCriarCave: Boolean = false,
        val erroNovaCave: String? = null,
        val aConsumirId: Long? = null,
        // Pedido do utilizador (2026-09-23): um resumo de 5 "já provadas" por baixo de "Em guarda", com link para o
        // ecrã inteiro — carregado uma vez só (não muda com a cave escolhida, é do utilizador, não da cave).
        val provadasRecentes: List<BebidaRelacao> = emptyList(),
    ) : CaveUiState {
        val caveSelecionada: CaveResponse? get() = caves.firstOrNull { it.id == caveSelecionadaId }
    }
}

/**
 * O ecrã "As minhas Caves": pílulas das caves do utilizador + "+" (abre o popup "Nova cave"), estatísticas da cave
 * escolhida, garrafas "Prontas a abrir" (com "Consumir") e "Em guarda" (com ordenação). Ligado a `GET /users/me/caves`
 * e `GET /caves/{id}` (o mesmo padrão já usado na homepage, aqui como ecrã dedicado e com o popup "Nova cave").
 */
@HiltViewModel
class CaveViewModel @Inject constructor(
    private val caveRepository: CaveRepository,
    private val provadaRepository: ProvadaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<CaveUiState>(CaveUiState.Loading)
    val state: StateFlow<CaveUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = CaveUiState.Loading
        viewModelScope.launch {
            caveRepository.getCaves()
                .onSuccess { caves ->
                    val selecionada = caves.firstOrNull()?.id
                    _state.value = CaveUiState.Ready(caves = caves, caveSelecionadaId = selecionada)
                    if (selecionada != null) carregarDetalhe(selecionada)
                    provadaRepository.getProvadas().onSuccess { provadas ->
                        atualizarPronto { copy(provadasRecentes = provadas.take(5)) }
                    }
                }
                .onFailure { _state.value = CaveUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarCave(id: Long) {
        atualizarPronto { copy(caveSelecionadaId = id, detalhe = null) }
        carregarDetalhe(id)
    }

    fun selecionarOrdem(ordem: OrdemEmGuarda) {
        atualizarPronto { copy(ordem = ordem) }
        caveSelecionadaIdAtual()?.let { carregarDetalhe(it) }
    }

    private fun carregarDetalhe(caveId: Long) {
        atualizarPronto { copy(carregandoDetalhe = true) }
        viewModelScope.launch {
            val ordem = (_state.value as? CaveUiState.Ready)?.ordem ?: OrdemEmGuarda.DATA_CONSUMO
            caveRepository.getCaveDetail(caveId, ordem.apiValue)
                .onSuccess { atualizarPronto { copy(detalhe = it, carregandoDetalhe = false) } }
                .onFailure { atualizarPronto { copy(carregandoDetalhe = false) } }
        }
    }

    fun abrirNovaCave() = atualizarPronto { copy(mostrarNovaCave = true, erroNovaCave = null) }

    fun fecharNovaCave() = atualizarPronto { copy(mostrarNovaCave = false) }

    fun criarCave(nome: String, descricao: String?) {
        if (nome.isBlank()) return
        atualizarPronto { copy(aCriarCave = true, erroNovaCave = null) }
        viewModelScope.launch {
            caveRepository.createCave(nome.trim(), descricao?.trim()?.ifBlank { null })
                .onSuccess { nova ->
                    // Não é "carregar() + selecionarCave(nova.id)": carregar() é assíncrono e a sua resposta (a
                    // repor caveSelecionadaId = caves.first()) podia chegar depois de selecionarCave() e
                    // sobrepor-se-lhe — corrida apanhada a testar ao vivo (a cave nova ficava criada mas por
                    // selecionar). Aqui é tudo na mesma corrotina, pela ordem certa.
                    caveRepository.getCaves()
                        .onSuccess { caves ->
                            atualizarPronto { copy(caves = caves, caveSelecionadaId = nova.id, aCriarCave = false, mostrarNovaCave = false) }
                            carregarDetalhe(nova.id)
                        }
                        .onFailure { atualizarPronto { copy(aCriarCave = false, mostrarNovaCave = false) } }
                }
                .onFailure { atualizarPronto { copy(aCriarCave = false, erroNovaCave = it.toUserMessage()) } }
        }
    }

    /** Consumir marca sempre a bebida como provada (pedido do utilizador, 2026-09-23: "Consumir" é um dos dois
     * caminhos para entrar na lista de já provadas — o outro é adicioná-la lá diretamente). Idempotente no backend,
     * por isso não faz mal chamar sempre, mesmo que já estivesse provada. */
    fun consumir(caveBebidaId: Long) {
        val caveId = caveSelecionadaIdAtual() ?: return
        atualizarPronto { copy(aConsumirId = caveBebidaId) }
        viewModelScope.launch {
            caveRepository.consumir(caveId, caveBebidaId)
                .onSuccess { consumo ->
                    consumo.consumida.bebidaId?.let { provadaRepository.addProvada(it) }
                    atualizarPronto { copy(aConsumirId = null) }
                    carregar()
                }
                .onFailure { atualizarPronto { copy(aConsumirId = null) } }
        }
    }

    /** Depois de guardar uma garrafa pelo popup "Adicionar à cave" aberto de dentro da própria Cave (sobre o popup
     * de detalhe de uma bebida): atualiza os totais de todas as pílulas (a garrafa pode ter ido para qualquer
     * cave, não só a que está aberta) e o detalhe da cave atual — sem `carregar()`, que reporia `caveSelecionadaId`
     * para a 1.ª cave e mostrava o ecrã de carregamento inteiro. Apanhado a testar ao vivo (2026-09-23): sem isto,
     * "INVESTIDOS"/"GARRAFAS" das pílulas ficavam com o valor de antes de adicionar, só a lista de garrafas
     * (`detalhe`, sempre pedida de novo ao escolher uma cave) é que já estava certa. */
    fun atualizarAposGuardar() {
        viewModelScope.launch {
            caveRepository.getCaves().onSuccess { caves -> atualizarPronto { copy(caves = caves) } }
        }
        caveSelecionadaIdAtual()?.let { carregarDetalhe(it) }
    }

    private fun caveSelecionadaIdAtual() = (_state.value as? CaveUiState.Ready)?.caveSelecionadaId

    private fun atualizarPronto(transform: CaveUiState.Ready.() -> CaveUiState.Ready) {
        _state.update { (it as? CaveUiState.Ready)?.transform() ?: it }
    }
}
