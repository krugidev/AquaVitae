package pt.aquavitae.android.feature.wishlist

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

/** Ecrã de wishlist — placeholder de UI, já ligado ao [WishlistViewModel]/API real. */
@Composable
fun WishlistScreen(viewModel: WishlistViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Wishlist", style = MaterialTheme.typography.headlineMedium)

        when (val state = uiState) {
            is WishlistUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is WishlistUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }

            is WishlistUiState.Success -> {
                if (state.bebidas.isEmpty()) {
                    Text("A tua wishlist está vazia.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        items(state.bebidas, key = { it.id }) { bebida -> WishlistItem(bebida) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistItem(bebida: BebidaSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = bebida.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = bebida.categoriaNome, style = MaterialTheme.typography.bodySmall)
        }
    }
}
