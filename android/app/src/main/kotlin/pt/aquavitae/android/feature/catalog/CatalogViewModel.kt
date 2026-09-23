package pt.aquavitae.android.feature.catalog

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
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.CatalogFiltro
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.LookupRepository
import javax.inject.Inject

private const val CATEGORIA_VINHO = "Vinho"
private const val TAMANHO_PAGINA = 20
private const val ESPERA_CONTAGEM_MS = 350L

enum class CatalogSort(val label: String, val apiValue: String?) {
    MELHOR_AVALIADAS("Melhor avaliadas", "ratingMedio,desc"),
    NOME_AZ("Nome (A-Z)", "nome,asc"),
}

/** Estado do ecrã "Catálogo": os resultados (aplicados) + o popup de filtros (em edição, só aplica ao tocar "Ver X bebidas"). */
sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Error(val message: String) : CatalogUiState
    data class Ready(
        val categorias: List<LookupItem> = emptyList(),
        val resultados: List<BebidaSummary> = emptyList(),
        val totalElements: Long = 0,
        val temMais: Boolean = false,
        val carregandoMais: Boolean = false,
        val sort: CatalogSort = CatalogSort.MELHOR_AVALIADAS,
        /** O que está a filtrar a lista de baixo agora mesmo. */
        val filtro: CatalogFiltro = CatalogFiltro(),
        val filtrosAbertos: Boolean = false,
        /** Só interessa com `filtrosAbertos`: o que se está a editar no popup, ainda não aplicado. */
        val rascunho: CatalogFiltro = CatalogFiltro(),
        val paises: LookupState<List<LookupItem>> = LookupState.Loading,
        val regioes: LookupState<List<LookupItem>> = LookupState.Loading,
        val corpos: LookupState<List<LookupItem>> = LookupState.Loading,
        val tipos: LookupState<List<LookupItem>> = LookupState.Loading,
        val castas: LookupState<List<Casta>> = LookupState.Loading,
        val castaBusca: String = "",
        val contagemRascunho: Long? = null,
        val aContarRascunho: Boolean = false,
    ) : CatalogUiState {
        val categoriaVinhoId: Long? get() = categorias.firstOrNull { it.nome.equals(CATEGORIA_VINHO, ignoreCase = true) }?.id
        val rascunhoEhVinho: Boolean get() = rascunho.categoriaId != null && rascunho.categoriaId == categoriaVinhoId
        val castasEscolhidas: List<Casta>
            get() = (castas as? LookupState.Ready)?.data?.filter { it.id in rascunho.castaIds }.orEmpty()

        /** As castas visíveis na pesquisa do popup: as escolhidas primeiro, depois as que casam com o texto (até 30). */
        val castasSugeridas: List<Casta>
            get() {
                val todas = (castas as? LookupState.Ready)?.data.orEmpty()
                val escolhidasIds = rascunho.castaIds
                val porNome = if (castaBusca.isBlank()) todas else todas.filter { it.nome?.contains(castaBusca, ignoreCase = true) == true }
                return porNome.filter { it.id !in escolhidasIds }.take(30)
            }
    }
}

/**
 * O ecrã "Catálogo" (fatia 2b): a lista de resultados ligada a `GET /api/bebidas` com todos os filtros do popup
 * ("Filtros do catálogo", ver `android/design/catalogo/`). O popup edita um rascunho à parte (`rascunho`) — só
 * substitui o filtro aplicado (`filtro`, que é o que a lista de baixo usa) quando se toca em "Ver X bebidas"; fechar
 * de outra forma (fora, ou o gesto de voltar) descarta as alterações. A contagem ao vivo no botão consulta a mesma
 * pesquisa com `size=1` (só para ler `totalElements`), com um pequeno atraso (350ms) para não disparar 1 pedido por toque.
 */
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val bebidaRepository: BebidaRepository,
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private var contagemJob: Job? = null
    private var buscaJob: Job? = null

    init {
        carregar()
    }

    fun carregar() {
        _state.value = CatalogUiState.Loading
        viewModelScope.launch {
            lookupRepository.categoriasBebida()
                .onSuccess { categorias ->
                    val categoriaInicial = categorias.firstOrNull { it.nome.equals(CATEGORIA_VINHO, ignoreCase = true) }?.id
                        ?: categorias.firstOrNull()?.id
                    val filtroInicial = CatalogFiltro(categoriaId = categoriaInicial)
                    _state.value = CatalogUiState.Ready(categorias = categorias, filtro = filtroInicial, rascunho = filtroInicial)
                    buscar(reiniciar = true)
                    carregarLookupsDoPopup()
                }
                .onFailure { _state.value = CatalogUiState.Error(it.toUserMessage()) }
        }
    }

    // --- Resultados (o filtro já aplicado) ---

    fun selecionarCategoriaAtiva(id: Long) = atualizarPronto { copy(filtro = filtro.copy(categoriaId = id), rascunho = rascunho.copy(categoriaId = id)) }
        .also { buscar(reiniciar = true) }

    fun pesquisar(texto: String) = atualizarPronto { copy(filtro = filtro.copy(search = texto.ifBlank { null })) }
        .also { buscar(reiniciar = true) }

    fun selecionarSort(sort: CatalogSort) = atualizarPronto { copy(sort = sort) }.also { buscar(reiniciar = true) }

    /** Remove um único filtro pelas pílulas removíveis por cima da lista (não mexe na categoria). */
    fun removerFiltro(remover: (CatalogFiltro) -> CatalogFiltro) {
        atualizarPronto {
            val categoriaId = filtro.categoriaId
            val novo = remover(filtro).copy(categoriaId = categoriaId)
            copy(filtro = novo, rascunho = novo)
        }
        buscar(reiniciar = true)
    }

    fun carregarMais() {
        val atual = _state.value as? CatalogUiState.Ready ?: return
        if (!atual.temMais || atual.carregandoMais) return
        buscar(reiniciar = false)
    }

    private fun buscar(reiniciar: Boolean) {
        buscaJob?.cancel()
        buscaJob = viewModelScope.launch {
            val antes = _state.value as? CatalogUiState.Ready ?: return@launch
            val pagina = if (reiniciar) 0 else (antes.resultados.size / TAMANHO_PAGINA)
            _state.update {
                (it as? CatalogUiState.Ready)?.copy(carregandoMais = true, resultados = if (reiniciar) emptyList() else it.resultados) ?: it
            }
            bebidaRepository.searchBebidas(antes.filtro, page = pagina, size = TAMANHO_PAGINA, sort = antes.sort.apiValue)
                .onSuccess { pageResponse ->
                    _state.update { estadoAtual ->
                        val atual = estadoAtual as? CatalogUiState.Ready ?: return@update estadoAtual
                        atual.copy(
                            resultados = if (reiniciar) pageResponse.content else atual.resultados + pageResponse.content,
                            totalElements = pageResponse.totalElements,
                            temMais = !pageResponse.last,
                            carregandoMais = false,
                        )
                    }
                }
                .onFailure { erro ->
                    _state.update { (it as? CatalogUiState.Ready)?.copy(carregandoMais = false) ?: CatalogUiState.Error(erro.toUserMessage()) }
                }
        }
    }

    // --- Popup de filtros: edita `rascunho`, só aplica a `filtro` em aplicarFiltros() ---

    // "Portugal" por omissão (ver carregarLookupsDoPopup) só quando o filtro aplicado ainda não tem país nenhum — um
    // país já aplicado antes (ou já escolhido numa abertura anterior do popup nesta visita) tem sempre prioridade.
    fun abrirFiltros() = atualizarPronto {
        copy(filtrosAbertos = true, rascunho = filtro.copy(paisId = filtro.paisId ?: rascunho.paisId), castaBusca = "")
    }.also { recarregarRegioesDoRascunho() }

    // Repõe `regioes` para o país do filtro aplicado (o rascunho pode ter mudado de país sem aplicar) — assim os
    // nomes das pílulas removíveis por cima da lista (ver CatalogScreen) nunca ficam dessincronizados de `regioes`.
    fun fecharFiltrosSemAplicar() {
        atualizarPronto { copy(filtrosAbertos = false, rascunho = filtro) }
        recarregarRegioesDoRascunho()
    }

    fun aplicarFiltros() {
        atualizarPronto { copy(filtro = rascunho, filtrosAbertos = false) }
        buscar(reiniciar = true)
    }

    fun limparRascunho() = editarRascunho { it.limpo() }

    fun selecionarCategoriaRascunho(id: Long) {
        editarRascunho { it.copy(categoriaId = id) }
        recarregarRegioesDoRascunho()
    }

    fun selecionarPais(id: Long) {
        editarRascunho { it.copy(paisId = id, regiaoIds = emptySet()) }
        recarregarRegioesDoRascunho()
    }

    fun toggleRegiao(id: Long) = editarRascunho { it.copy(regiaoIds = if (id in it.regiaoIds) it.regiaoIds - id else it.regiaoIds + id) }

    fun setPreco(min: Double, max: Double) = editarRascunho { it.copy(precoMin = min, precoMax = max) }

    /** Toca outra vez na já escolhida para voltar a "Todos". */
    fun selecionarRatingMin(valor: Double?) = editarRascunho { it.copy(ratingMin = if (it.ratingMin == valor) null else valor) }

    fun setAcidez(min: Int, max: Int) = editarRascunho { it.copy(acidezMin = min, acidezMax = max) }

    fun setDocura(min: Int, max: Int) = editarRascunho { it.copy(docuraMin = min, docuraMax = max) }

    fun selecionarCorpo(id: Long) = editarRascunho { it.copy(corpoId = if (it.corpoId == id) null else id) }

    fun selecionarTipo(id: Long) = editarRascunho { it.copy(tipoId = if (it.tipoId == id) null else id) }

    fun onCastaBusca(texto: String) = atualizarPronto { copy(castaBusca = texto) }

    fun toggleCasta(id: Long) = editarRascunho { it.copy(castaIds = if (id in it.castaIds) it.castaIds - id else it.castaIds + id) }

    private fun editarRascunho(transform: (CatalogFiltro) -> CatalogFiltro) {
        atualizarPronto { copy(rascunho = transform(rascunho)) }
        contarRascunho()
    }

    private fun contarRascunho() {
        contagemJob?.cancel()
        contagemJob = viewModelScope.launch {
            delay(ESPERA_CONTAGEM_MS)
            val rascunho = (_state.value as? CatalogUiState.Ready)?.rascunho ?: return@launch
            atualizarPronto { copy(aContarRascunho = true) }
            bebidaRepository.searchBebidas(rascunho, page = 0, size = 1)
                .onSuccess { pageResponse -> atualizarPronto { copy(contagemRascunho = pageResponse.totalElements, aContarRascunho = false) } }
                .onFailure { atualizarPronto { copy(aContarRascunho = false) } }
        }
    }

    private fun recarregarRegioesDoRascunho() {
        val paisId = (_state.value as? CatalogUiState.Ready)?.rascunho?.paisId ?: return
        atualizarPronto { copy(regioes = LookupState.Loading) }
        viewModelScope.launch {
            lookupRepository.regioes(paisId)
                .onSuccess { atualizarPronto { copy(regioes = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(regioes = LookupState.Error(it.toUserMessage())) } }
        }
    }

    private fun carregarLookupsDoPopup() {
        viewModelScope.launch {
            lookupRepository.paisesBebida()
                .onSuccess { paises ->
                    atualizarPronto { copy(paises = LookupState.Ready(paises)) }
                    // Só o rascunho (o popup ainda fechado): "Portugal" por omissão, como no mockup — a lista de baixo só
                    // passa a filtrar por país quando o utilizador tocar em "Ver X bebidas".
                    val paisInicial = paises.firstOrNull { it.id == CatalogFiltro.PAIS_PORTUGAL_ID }?.id ?: paises.firstOrNull()?.id
                    if (paisInicial != null) {
                        editarRascunho { it.copy(paisId = paisInicial) }
                        recarregarRegioesDoRascunho()
                    }
                }
                .onFailure { atualizarPronto { copy(paises = LookupState.Error(it.toUserMessage())) } }
        }
        viewModelScope.launch {
            lookupRepository.vinhoCorpos()
                .onSuccess { atualizarPronto { copy(corpos = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(corpos = LookupState.Error(it.toUserMessage())) } }
        }
        viewModelScope.launch {
            lookupRepository.vinhoTipos()
                .onSuccess { atualizarPronto { copy(tipos = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(tipos = LookupState.Error(it.toUserMessage())) } }
        }
        viewModelScope.launch {
            lookupRepository.castas()
                .onSuccess { atualizarPronto { copy(castas = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(castas = LookupState.Error(it.toUserMessage())) } }
        }
    }

    private fun atualizarPronto(transform: CatalogUiState.Ready.() -> CatalogUiState.Ready) {
        _state.update { (it as? CatalogUiState.Ready)?.transform() ?: it }
    }
}
