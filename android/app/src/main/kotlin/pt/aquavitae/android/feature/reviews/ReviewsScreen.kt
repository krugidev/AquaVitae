package pt.aquavitae.android.feature.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.ReviewResponse

/**
 * Ecrã de reviews de uma bebida — placeholder de UI (lista + formulário
 * simples), já ligado ao [ReviewsViewModel]/API real.
 */
@Composable
fun ReviewsScreen(viewModel: ReviewsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var ratingText by remember { mutableStateOf("5") }
    var comment by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Reviews", style = MaterialTheme.typography.headlineMedium)

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is ReviewsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ReviewsUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }

                is ReviewsUiState.Success -> {
                    if (state.reviews.isEmpty()) {
                        Text("Ainda sem reviews. Sê o primeiro a avaliar!")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.reviews, key = { it.id }) { review ->
                                ReviewItem(review)
                            }
                        }
                    }
                }
            }
        }

        // TODO: substituir por um seletor de estrelas em vez de um campo de texto.
        OutlinedTextField(
            value = ratingText,
            onValueChange = { ratingText = it },
            label = { Text("Rating (1-5)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comentário (opcional)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Button(
            onClick = {
                val rating = ratingText.toDoubleOrNull() ?: 5.0
                viewModel.submitReview(rating, comment.ifBlank { null })
                comment = ""
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(if (isSubmitting) "A enviar..." else "Submeter review")
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${review.utilizadorUsername} · ★ ${review.rating}", style = MaterialTheme.typography.titleSmall)
            review.comment?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            Text(text = review.createdAt.orEmpty(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
