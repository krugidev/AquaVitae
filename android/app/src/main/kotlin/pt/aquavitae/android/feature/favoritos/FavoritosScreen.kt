package pt.aquavitae.android.feature.favoritos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaSummary

/** Ecrã de favoritos — placeholder de UI, já ligado ao [FavoritosViewModel]/API real. */
@Composable
fun FavoritosScreen(viewModel: FavoritosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Favoritos", style = MaterialTheme.typography.headlineMedium)

        when (val state = uiState) {
            is FavoritosUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is FavoritosUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }

            is FavoritosUiState.Success -> {
                if (state.bebidas.isEmpty()) {
                    Text("Ainda não tens favoritos.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        items(state.bebidas, key = { it.id }) { bebida -> FavoritoItem(bebida) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritoItem(bebida: BebidaSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = bebida.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = bebida.categoriaNome, style = MaterialTheme.typography.bodySmall)
        }
    }
}
