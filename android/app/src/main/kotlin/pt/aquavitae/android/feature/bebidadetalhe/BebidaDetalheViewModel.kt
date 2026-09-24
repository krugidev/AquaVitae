package pt.aquavitae.android.feature.bebidadetalhe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.model.OfertaCompra
import pt.aquavitae.android.data.model.ReviewsResponse
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.BebidaRepository
import pt.aquavitae.android.data.repository.CompraRepository
import pt.aquavitae.android.data.repository.FavoritoRepository
import pt.aquavitae.android.data.repository.ReviewRepository
import pt.aquavitae.android.data.repository.WishlistRepository
import javax.inject.Inject

enum class DetalheTab { DETALHES, REVIEWS }

/** O popup de detalhe de uma bebida (`android/design/caves/03-bebida-detalhe-popup.png` e `05-...-reviews-popup.png`). */
sealed interface BebidaDetalheUiState {
    data object Loading : BebidaDetalheUiState
    data class Error(val message: String) : BebidaDetalheUiState
    data class Ready(
        val bebida: BebidaDetail,
        val tab: DetalheTab = DetalheTab.DETALHES,
        val reviews: LookupState<ReviewsResponse> = LookupState.Loading,
        // "Onde comprar": todas as ofertas dos retalhistas (a mais barata também é o botão principal do cabeçalho).
        val ofertas: LookupState<List<OfertaCompra>> = LookupState.Loading,
        // Espelham bebida.isFavorito/isWishlist mas atualizam-se logo ao tocar (otimista), sem esperar pela resposta.
        val isFavorito: Boolean = false,
        val isWishlist: Boolean = false,
        val aAlternarFavorito: Boolean = false,
        val aAlternarWishlist: Boolean = false,
        // A escrever/editar a própria review.
        val minhaEstrelas: Double = 0.0,
        val meuComentario: String = "",
        val aSubmeterReview: Boolean = false,
        val erroReview: String? = null,
    ) : BebidaDetalheUiState
}

/**
 * Sem argumento de navegação (o popup abre por cima de qualquer ecrã, não é uma rota): o ecrã que o mostra pede
 * `hiltViewModel(key = "bebida-detalhe-$bebidaId")` — uma chave por bebida dá uma instância nova (e o estado
 * limpo) sempre que se abre uma bebida diferente, sem precisar de injeção assistida do Hilt.
 */
@HiltViewModel
class BebidaDetalheViewModel @Inject constructor(
    private val bebidaRepository: BebidaRepository,
    private val reviewRepository: ReviewRepository,
    private val favoritoRepository: FavoritoRepository,
    private val wishlistRepository: WishlistRepository,
    private val compraRepository: CompraRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<BebidaDetalheUiState>(BebidaDetalheUiState.Loading)
    val state: StateFlow<BebidaDetalheUiState> = _state.asStateFlow()

    private var bebidaId: Long = 0

    /** Chamado uma vez, logo que o popup abre (`LaunchedEffect(bebidaId)` no ecrã) — ver o comentário da classe. */
    fun carregar(bebidaId: Long) {
        this.bebidaId = bebidaId
        _state.value = BebidaDetalheUiState.Loading
        viewModelScope.launch {
            bebidaRepository.getBebidaDetail(bebidaId)
                .onSuccess { bebida ->
                    _state.value = BebidaDetalheUiState.Ready(
                        bebida = bebida,
                        isFavorito = bebida.isFavorito == true,
                        isWishlist = bebida.isWishlist == true,
                        minhaEstrelas = bebida.notaPropria ?: 0.0,
                    )
                    carregarReviews()
                    carregarOfertas()
                }
                .onFailure { _state.value = BebidaDetalheUiState.Error(it.toUserMessage()) }
        }
    }

    fun selecionarTab(tab: DetalheTab) = atualizarPronto { copy(tab = tab) }

    private fun carregarReviews() {
        atualizarPronto { copy(reviews = LookupState.Loading) }
        viewModelScope.launch {
            reviewRepository.getReviews(bebidaId)
                .onSuccess { atualizarPronto { copy(reviews = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(reviews = LookupState.Error(it.toUserMessage())) } }
        }
    }

    private fun carregarOfertas() {
        atualizarPronto { copy(ofertas = LookupState.Loading) }
        viewModelScope.launch {
            compraRepository.getOfertas(bebidaId)
                .onSuccess { atualizarPronto { copy(ofertas = LookupState.Ready(it)) } }
                .onFailure { atualizarPronto { copy(ofertas = LookupState.Error(it.toUserMessage())) } }
        }
    }

    /**
     * O utilizador tocou num botão "Comprar": regista o clique (para a comissão e para o inquérito "Compraste?" ao voltar à
     * app). O ecrã abre a loja **antes** de chamar isto e não espera pela resposta — falhar (sem rede, 409 se o link ficou
     * indisponível) nunca pode impedir de comprar.
     */
    fun registarClique(linkId: Long) {
        viewModelScope.launch { compraRepository.registarClique(bebidaId, linkId) }
    }

    fun alternarFavorito() {
        val atual = _state.value as? BebidaDetalheUiState.Ready ?: return
        val novoValor = !atual.isFavorito
        atualizarPronto { copy(isFavorito = novoValor, aAlternarFavorito = true) }
        viewModelScope.launch {
            val resultado = if (novoValor) favoritoRepository.addFavorito(bebidaId) else favoritoRepository.removeFavorito(bebidaId)
            resultado.onFailure { atualizarPronto { copy(isFavorito = !novoValor) } } // reverte se falhar
            atualizarPronto { copy(aAlternarFavorito = false) }
        }
    }

    fun alternarWishlist() {
        val atual = _state.value as? BebidaDetalheUiState.Ready ?: return
        val novoValor = !atual.isWishlist
        atualizarPronto { copy(isWishlist = novoValor, aAlternarWishlist = true) }
        viewModelScope.launch {
            val resultado = if (novoValor) wishlistRepository.addToWishlist(bebidaId) else wishlistRepository.removeFromWishlist(bebidaId)
            resultado.onFailure { atualizarPronto { copy(isWishlist = !novoValor) } }
            atualizarPronto { copy(aAlternarWishlist = false) }
        }
    }

    fun onMinhasEstrelas(valor: Double) = atualizarPronto { copy(minhaEstrelas = valor) }

    fun onMeuComentario(texto: String) = atualizarPronto { copy(meuComentario = texto.take(COMENTARIO_MAX)) }

    /**
     * Só publica se a bebida já estiver marcada como consumida (`bebida.isProvada`) — pedido do utilizador
     * (2026-09-23): uma review só existe quando o utilizador já tem a bebida na lista de "já provadas", e essa
     * marcação só acontece por dois caminhos deliberados ("Consumir" numa cave, ou adicionar à lista diretamente),
     * nunca escondida atrás de "publicar review" (era o que este método fazia antes). A API também recusa (409) sem
     * isto, mas não se chega a pedir: o formulário (`MinhaReviewForm` em `BebidaDetalheSheet.kt`) já fica escondido,
     * substituído por uma explicação, sem `bebida.isProvada`.
     */
    fun publicarReview() {
        val atual = _state.value as? BebidaDetalheUiState.Ready ?: return
        if (atual.minhaEstrelas <= 0.0 || atual.bebida.isProvada != true) return
        atualizarPronto { copy(aSubmeterReview = true, erroReview = null) }
        viewModelScope.launch {
            reviewRepository.submitReview(bebidaId, atual.minhaEstrelas, atual.meuComentario.trim().ifBlank { null })
                .onSuccess {
                    atualizarPronto { copy(aSubmeterReview = false, meuComentario = "") }
                    carregarReviews()
                    atualizarRatingDaBebida()
                }
                .onFailure { atualizarPronto { copy(aSubmeterReview = false, erroReview = it.toUserMessage()) } }
        }
    }

    /** Depois de publicar, `bebida.ratingMedio`/`totalReviews` (o cabeçalho do popup) ficam desatualizados — o
     * backend recalcula-os ao gravar a review, mas só `carregarReviews()` foi pedido de novo. Volta a pedir só a
     * bebida (não a `carregar()` inteira, que reporia a tab e as marcações otimistas de favorito/wishlist). */
    private fun atualizarRatingDaBebida() {
        viewModelScope.launch {
            bebidaRepository.getBebidaDetail(bebidaId).onSuccess { bebidaAtualizada ->
                atualizarPronto { copy(bebida = bebidaAtualizada) }
            }
        }
    }

    private fun atualizarPronto(transform: BebidaDetalheUiState.Ready.() -> BebidaDetalheUiState.Ready) {
        _state.update { (it as? BebidaDetalheUiState.Ready)?.transform() ?: it }
    }

    companion object {
        const val COMENTARIO_MAX = 4000
    }
}
