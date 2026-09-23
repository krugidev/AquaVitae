package pt.aquavitae.android.feature.home

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
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.CaveRepository
import pt.aquavitae.android.data.repository.LookupRepository
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

// A "Escolhido para ti" pede um tamanho pequeno (2 cartões, como no desenho); as pílulas de categoria filtram-na.
// "Ver mais sugestões" (por implementar) levaria ao catálogo já filtrado pela mesma categoria.
private const val TAMANHO_SUGESTOES = 2
private const val CATEGORIA_OMISSAO = "Vinho"

/**
 * A homepage: saudação/avatar/estatísticas (`GET /users/me`), "Escolhido para ti" (catálogo filtrado por categoria,
 * ordenado por rating), "As minhas Caves" (a 1.ª cave do utilizador, com as garrafas prontas a abrir + em guarda) e o
 * produtor em destaque da semana. As 3 últimas secções carregam em paralelo e falham de forma independente — ver
 * [HomeUiState].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bebidaRepository: BebidaRepository,
    private val caveRepository: CaveRepository,
    private val lookupRepository: LookupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            val utilizador = userRepository.getMe().getOrElse {
                _state.value = HomeUiState.Error(it.toUserMessage())
                return@launch
            }

            val categorias = lookupRepository.categoriasBebida().getOrDefault(emptyList())
            val categoriaId = categorias.firstOrNull { it.nome.equals(CATEGORIA_OMISSAO, ignoreCase = true) }?.id
                ?: categorias.firstOrNull()?.id

            val caves = caveRepository.getCaves().getOrDefault(emptyList())
            val caveId = caves.firstOrNull()?.id

            coroutineScope {
                val sugestoesDeferred = async { buscarSugestoes(categoriaId) }
                val garrafasDeferred = async { garrafasDaCave(caveId) }
                val produtorDeferred = async { bebidaRepository.getProdutorDestaque().getOrNull() }

                _state.value = HomeUiState.Ready(
                    utilizador = utilizador,
                    categorias = categorias,
                    categoriaSelecionadaId = categoriaId,
                    sugestoes = sugestoesDeferred.await(),
                    caves = caves,
                    caveSelecionadaId = caveId,
                    garrafasCaveSelecionada = garrafasDeferred.await(),
                    produtorDestaque = produtorDeferred.await(),
                )
            }
        }
    }

    /** Tocar numa pílula de categoria: refaz só "Escolhido para ti" (o resto do ecrã fica como está). */
    fun selecionarCategoria(categoriaId: Long) {
        val atual = _state.value as? HomeUiState.Ready ?: return
        if (atual.categoriaSelecionadaId == categoriaId) return
        _state.update { (it as? HomeUiState.Ready)?.copy(categoriaSelecionadaId = categoriaId, sugestoesLoading = true) ?: it }
        viewModelScope.launch {
            val sugestoes = buscarSugestoes(categoriaId)
            _state.update {
                (it as? HomeUiState.Ready)
                    ?.takeIf { estado -> estado.categoriaSelecionadaId == categoriaId }
                    ?.copy(sugestoes = sugestoes, sugestoesLoading = false)
                    ?: it
            }
        }
    }

    /** Tocar numa pílula de cave: troca as garrafas mostradas para a cave escolhida. */
    fun selecionarCave(caveId: Long) {
        val atual = _state.value as? HomeUiState.Ready ?: return
        if (atual.caveSelecionadaId == caveId) return
        viewModelScope.launch {
            val garrafas = garrafasDaCave(caveId)
            _state.update {
                (it as? HomeUiState.Ready)?.copy(caveSelecionadaId = caveId, garrafasCaveSelecionada = garrafas) ?: it
            }
        }
    }

    /** Depois de guardar uma garrafa pelo popup "Adicionar à cave" (aberto de dentro da própria homepage, sobre o
     * popup de detalhe de uma bebida): atualiza a lista de caves e as garrafas da cave escolhida, sem recarregar o
     * ecrã inteiro. Mesma razão do `CaveViewModel.atualizarAposGuardar` — ver `CLAUDE.md`, "Android — a UI". */
    fun atualizarAposGuardar() {
        val atual = _state.value as? HomeUiState.Ready ?: return
        viewModelScope.launch {
            val caves = caveRepository.getCaves().getOrDefault(atual.caves)
            val garrafas = garrafasDaCave(atual.caveSelecionadaId)
            _state.update { (it as? HomeUiState.Ready)?.copy(caves = caves, garrafasCaveSelecionada = garrafas) ?: it }
        }
    }

    private suspend fun buscarSugestoes(categoriaId: Long?) =
        bebidaRepository.searchBebidas(
            categoriaIds = categoriaId?.let { listOf(it) },
            size = TAMANHO_SUGESTOES,
            sort = "ratingMedio,desc",
        ).getOrNull()?.content.orEmpty()

    private suspend fun garrafasDaCave(caveId: Long?): List<CaveBebidaResponse> {
        if (caveId == null) return emptyList()
        val detalhe = caveRepository.getCaveDetail(caveId).getOrNull() ?: return emptyList()
        return (detalhe.prontasAAbrir + detalhe.emGuarda)
    }
}
