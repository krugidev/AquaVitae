package pt.aquavitae.android.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.MinhaReview
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.data.network.toUserMessage
import pt.aquavitae.android.data.repository.ReviewRepository
import pt.aquavitae.android.data.repository.UserRepository
import javax.inject.Inject

/** Estado do ecrã "As minhas reviews" (`android/design/perfil/05-reviews-e-conta-seguranca.png`, imagem 9a). */
sealed interface MinhasReviewsUiState {
    data object Loading : MinhasReviewsUiState
    data class Error(val message: String) : MinhasReviewsUiState
    data class Ready(
        val todas: List<MinhaReview>,
        /** Para o rodapé de cada cartão (avatar e nome de quem escreveu — o próprio); `null` se não se conseguiu ler o perfil. */
        val utilizador: UtilizadorMe?,
        /** A pílula de mês escolhida; `null` = "Todos". */
        val mes: MesDeReviews? = null,
    ) : MinhasReviewsUiState {
        val meses: List<MesDeReviews> get() = mesesComReviews(todas)
        val visiveis: List<MinhaReview> get() = if (mes == null) todas else todas.filter { mesDaReview(it.createdAt) == mes }
        val grupos: List<GrupoDeReviews> get() = agruparPorMes(visiveis)
    }
}

/**
 * "As minhas reviews": `GET /api/users/me/reviews` (já existia, sem uso na app) agrupado por mês e filtrável pelas pílulas de mês.
 * O filtro é local (a lista vem toda, sem paginação, como Favoritos/Wishlist/Provadas).
 */
@HiltViewModel
class MinhasReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<MinhasReviewsUiState>(MinhasReviewsUiState.Loading)
    val state: StateFlow<MinhasReviewsUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        // Só mostra o ecrã de carregamento a seguir a um erro (voltar ao ecrã pede os dados de novo sem piscar).
        if (_state.value is MinhasReviewsUiState.Error) _state.value = MinhasReviewsUiState.Loading
        viewModelScope.launch {
            reviewRepository.getMinhasReviews().fold(
                onSuccess = { reviews ->
                    val utilizador = userRepository.getMe().getOrNull()
                    _state.update { atual ->
                        // Mantém a pílula escolhida se o mês ainda tem reviews (uma review apagada pode ter esvaziado o mês).
                        val mes = (atual as? MinhasReviewsUiState.Ready)?.mes?.takeIf { it in mesesComReviews(reviews) }
                        MinhasReviewsUiState.Ready(reviews, utilizador, mes)
                    }
                },
                onFailure = { _state.value = MinhasReviewsUiState.Error(it.toUserMessage()) },
            )
        }
    }

    fun selecionarMes(mes: MesDeReviews?) {
        _state.update { if (it is MinhasReviewsUiState.Ready) it.copy(mes = mes) else it }
    }
}
