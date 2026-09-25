package pt.aquavitae.android.feature.provadas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.ProvadaRepository
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

/** As 3 primeiras pílulas do popup "Já provadas" — a lista de categorias vem por baixo (a mesma linha, escolha única). */
enum class FiltroNota(val label: String) { TODAS("Todas"), COM_NOTA("Com nota"), SEM_NOTA("Sem nota") }

private const val MESES_POR_PAGINA = 2
private const val ESPERA_BUSCA_MS = 350L

/** O ecrã "Já provadas" (`android/design/caves/... "Bebidas já provadas"`, a imagem do popup pedido pelo utilizador). */
sealed interface ProvadasUiState {
    data object Loading : ProvadasUiState
    data class Error(val message: String) : ProvadasUiState
    data class Ready(
        val todas: List<BebidaRelacao> = emptyList(),
        val categorias: List<LookupItem> = emptyList(),
        val filtroNota: FiltroNota = FiltroNota.TODAS,
        val categoriaFiltroId: Long? = null,
        val mesesVisiveis: Int = MESES_POR_PAGINA,
        // Pedido do utilizador: procurar e adicionar uma bebida que não esteve em nenhuma cave (o mockup não tinha
        // esta barra — foi pedida à parte).
        val busca: String = "",
        val aBuscar: Boolean = false,
        val resultadosBusca: List<BebidaSummary> = emptyList(),
        val aAdicionarId: Long? = null,
    ) : ProvadasUiState {
        private val filtradas: List<BebidaRelacao>
            get() = todas.filter { relacao ->
                val passaNota = when (filtroNota) {
                    FiltroNota.TODAS -> true
                    FiltroNota.COM_NOTA -> relacao.bebida.notaPropria != null
                    FiltroNota.SEM_NOTA -> relacao.bebida.notaPropria == null
                }
                val passaCategoria = categoriaFiltroId == null ||
                    categorias.firstOrNull { it.id == categoriaFiltroId }?.nome == relacao.bebida.categoriaNome
                passaNota && passaCategoria
            }

        private val agrupadasPorMes: List<Pair<ChaveMes, List<BebidaRelacao>>>
            get() = filtradas.groupBy { chaveDoMes(it.data) }.entries.sortedByDescending { it.key }.map { it.key to it.value }

        /** Agrupa por mês (mais recente primeiro) e só mostra `mesesVisiveis` grupos. */
        val gruposVisiveis: List<Pair<String, List<BebidaRelacao>>>
            get() = agrupadasPorMes.take(mesesVisiveis).map { it.first.label to it.second }

        val temMaisMeses: Boolean get() = agrupadasPorMes.size > mesesVisiveis
    }
}

@HiltViewModel
class ProvadasViewModel @Inject constructor(
    private val provadaRepository: ProvadaRepository,
    private val bebidaRepository: BebidaRepository,
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ProvadasUiState>(ProvadasUiState.Loading)
    val state: StateFlow<ProvadasUiState> = _state.asStateFlow()

    private var buscaJob: Job? = null

    init {
        carregar()
    }

    fun carregar() {
        _state.value = ProvadasUiState.Loading
        viewModelScope.launch {
            provadaRepository.getProvadas()
                .onSuccess { todas ->
                    _state.value = ProvadasUiState.Ready(todas = todas)
                    lookupRepository.categoriasBebida().onSuccess { categorias -> atualizarPronto { copy(categorias = categorias) } }
                }
                .onFailure { _state.value = ProvadasUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarFiltroNota(filtro: FiltroNota) = atualizarPronto { copy(filtroNota = filtro, categoriaFiltroId = null) }

    fun selecionarCategoria(id: Long?) = atualizarPronto { copy(categoriaFiltroId = id, filtroNota = FiltroNota.TODAS) }

    fun carregarMaisMeses() = atualizarPronto { copy(mesesVisiveis = mesesVisiveis + MESES_POR_PAGINA) }

    fun onBusca(texto: String) {
        atualizarPronto { copy(busca = texto) }
        buscaJob?.cancel()
        if (texto.isBlank()) {
            atualizarPronto { copy(resultadosBusca = emptyList(), aBuscar = false) }
            return
        }
        buscaJob = viewModelScope.launch {
            delay(ESPERA_BUSCA_MS)
            atualizarPronto { copy(aBuscar = true) }
            bebidaRepository.searchBebidas(search = texto, size = 10)
                .onSuccess { pagina -> atualizarPronto { copy(resultadosBusca = pagina.content, aBuscar = false) } }
                .onFailure { atualizarPronto { copy(aBuscar = false) } }
        }
    }

    /** Adiciona diretamente à lista de já provadas (sem passar por nenhuma cave) — o 2.º caminho pedido pelo utilizador. */
    fun adicionarDaBusca(bebida: BebidaSummary) {
        atualizarPronto { copy(aAdicionarId = bebida.id) }
        viewModelScope.launch {
            provadaRepository.addProvada(bebida.id)
                .onSuccess {
                    atualizarPronto { copy(aAdicionarId = null, busca = "", resultadosBusca = emptyList()) }
                    carregar()
                }
                .onFailure { atualizarPronto { copy(aAdicionarId = null) } }
        }
    }

    private fun atualizarPronto(transform: ProvadasUiState.Ready.() -> ProvadasUiState.Ready) {
        _state.update { (it as? ProvadasUiState.Ready)?.transform() ?: it }
    }
}

private data class ChaveMes(val ano: Int, val mes: Int, val label: String) : Comparable<ChaveMes> {
    override fun compareTo(other: ChaveMes) = compareValuesBy(this, other, { it.ano }, { it.mes })
}

/** "SETEMBRO 2026" a partir do instant ISO de `BebidaRelacao.data` — chave de agrupamento e rótulo, já ordenável. */
private fun chaveDoMes(dataIso: String?): ChaveMes {
    val data = dataIso?.let { runCatching { Instant.parse(it).atZone(ZoneOffset.UTC) }.getOrNull() }
    if (data == null) return ChaveMes(0, 0, "SEM DATA")
    val mesNome = data.month.getDisplayName(java.time.format.TextStyle.FULL, pt.aquavitae.android.data.model.LocalePt)
    return ChaveMes(data.year, data.monthValue, "$mesNome ${data.year}".uppercase(pt.aquavitae.android.data.model.LocalePt))
}
