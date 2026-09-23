package pt.aquavitae.android.feature.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.ReviewResponse
import pt.aquavitae.android.data.repository.ReviewRepository
import pt.aquavitae.android.navigation.AppDestinations
import javax.inject.Inject

/** Estado do ecrã de reviews de uma bebida. */
sealed interface ReviewsUiState {
    data object Loading : ReviewsUiState
    data class Success(val reviews: List<ReviewResponse>) : ReviewsUiState
    data class Error(val message: String) : ReviewsUiState
}

/**
 * ViewModel de listagem/submissão de reviews. Placeholder de UI, já ligado ao
 * [ReviewRepository] real: carrega as reviews da bebida (id via argumento de
 * navegação) e permite submeter uma nova review.
 */
@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bebidaId: Long = checkNotNull(savedStateHandle[AppDestinations.ARG_BEBIDA_ID])

    private val _uiState = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Loading)
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        _uiState.value = ReviewsUiState.Loading
        viewModelScope.launch {
            reviewRepository.getReviews(bebidaId)
                .onSuccess { _uiState.value = ReviewsUiState.Success(it.reviews) }
                .onFailure { _uiState.value = ReviewsUiState.Error(it.message ?: "Não foi possível carregar as reviews.") }
        }
    }

    fun submitReview(rating: Double, comment: String?) {
        viewModelScope.launch {
            _isSubmitting.value = true
            reviewRepository.submitReview(bebidaId, rating, comment)
                .onSuccess { loadReviews() }
                .onFailure { _uiState.value = ReviewsUiState.Error(it.message ?: "Não foi possível submeter a review.") }
            _isSubmitting.value = false
        }
    }
}
