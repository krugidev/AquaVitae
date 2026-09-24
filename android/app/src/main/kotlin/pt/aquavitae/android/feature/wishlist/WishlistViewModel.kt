package pt.aquavitae.android.feature.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.model.maisBarataDisponivel
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.CompraRepository
import pt.aquavitae.android.data.repository.UserRepository
import pt.aquavitae.android.data.repository.WishlistRepository
import pt.aquavitae.android.ui.components.iniciaisDe
import javax.inject.Inject

/** As 3 pílulas de ordenação do mockup — ordenadas no telemóvel (a lista já vem inteira, sem paginação). */
enum class OrdemWishlist(val label: String) { RECENTES("Recentes"), PRECO("Preço"), RATING("Rating") }

/** Estado do ecrã "A minha wishlist" (`android/design/favoritos-wishlist/01-wishlist.png`). */
sealed interface WishlistUiState {
    data object Loading : WishlistUiState
    data class Error(val message: String) : WishlistUiState
    data class Ready(
        // Por ordem de wishlist_data_criacao desc (a API já devolve assim com sort=recente).
        val itens: List<BebidaRelacao> = emptyList(),
        val ordem: OrdemWishlist = OrdemWishlist.RECENTES,
        val avatar: Avatar? = null,
        val iniciais: String = "?",
        val aRemoverId: Long? = null,
        // "Para a cave": busca o BebidaDetail antes de abrir o AdicionarACaveSheet (a lista só tem BebidaSummary).
        val aAbrirCaveId: Long? = null,
    ) : WishlistUiState {
        val ordenados: List<BebidaRelacao>
            get() = when (ordem) {
                OrdemWishlist.RECENTES -> itens
                OrdemWishlist.PRECO -> itens.sortedBy { it.bebida.precoDesde ?: Double.MAX_VALUE }
                OrdemWishlist.RATING -> itens.sortedByDescending { it.bebida.ratingMedio }
            }
    }
}

/**
 * Wishlist do utilizador: ordenar por recentes/preço/rating (no telemóvel — a lista já vem inteira, ver
 * `CLAUDE.md`), "Comprar em X" (por agora abre o popup de detalhe — falta o link+clique, ver `PLANO.md`, "Por fazer
 * depois") e "Para a cave" (busca o detalhe e abre o `AdicionarACaveSheet` diretamente, sem passar pelo popup).
 */
@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val userRepository: UserRepository,
    private val bebidaRepository: BebidaRepository,
    private val compraRepository: CompraRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val state: StateFlow<WishlistUiState> = _state.asStateFlow()

    fun carregar() {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            wishlistRepository.getWishlist()
                .onSuccess { itens ->
                    val utilizador = userRepository.getMe().getOrNull()
                    _state.value = WishlistUiState.Ready(
                        itens = itens,
                        avatar = utilizador?.avatar,
                        iniciais = iniciaisDe(utilizador?.firstName, utilizador?.lastName, utilizador?.username),
                    )
                }
                .onFailure { _state.value = WishlistUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarOrdem(ordem: OrdemWishlist) = atualizarPronto { copy(ordem = ordem) }

    /** Tira o item da lista de imediato (otimista); se o pedido falhar, volta a carregar tudo para repor. */
    fun remover(bebidaId: Long) {
        atualizarPronto { copy(aRemoverId = bebidaId, itens = itens.filterNot { it.bebida.id == bebidaId }) }
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(bebidaId)
                .onSuccess { atualizarPronto { copy(aRemoverId = null) } }
                .onFailure { carregar() }
        }
    }

    /** A lista só tem `BebidaSummary`; o popup "Adicionar à cave" precisa do `BebidaDetail` completo. */
    fun prepararParaCave(bebidaId: Long, onPronto: (BebidaDetail) -> Unit) {
        atualizarPronto { copy(aAbrirCaveId = bebidaId) }
        viewModelScope.launch {
            bebidaRepository.getBebidaDetail(bebidaId)
                .onSuccess { detalhe ->
                    atualizarPronto { copy(aAbrirCaveId = null) }
                    onPronto(detalhe)
                }
                .onFailure { atualizarPronto { copy(aAbrirCaveId = null) } }
        }
    }

    // Os "Comprar" em curso (um duplo toque não abre a loja duas vezes nem regista dois cliques).
    private val aComprar = mutableSetOf<Long>()

    /**
     * "Comprar em X": a lista só tem `BebidaSummary` (sem o link), por isso vai buscar as ofertas da bebida, abre a mais barata
     * das disponíveis (`aoAbrir` devolve `false` se não houver browser) e regista o clique. Sem nenhuma oferta disponível
     * (o preço da lista pode ter ficado para trás) chama `semOferta`, que abre o popup de detalhe.
     */
    fun comprar(bebidaId: Long, aoAbrir: (String) -> Boolean, semOferta: () -> Unit) {
        if (!aComprar.add(bebidaId)) return
        viewModelScope.launch {
            val oferta = compraRepository.getOfertas(bebidaId).getOrNull()?.maisBarataDisponivel()
            aComprar.remove(bebidaId)
            val url = oferta?.url
            if (oferta != null && url != null && aoAbrir(url)) {
                compraRepository.registarClique(bebidaId, oferta.id)
            } else {
                semOferta()
            }
        }
    }

    private fun atualizarPronto(transform: WishlistUiState.Ready.() -> WishlistUiState.Ready) {
        _state.update { (it as? WishlistUiState.Ready)?.transform() ?: it }
    }
}
