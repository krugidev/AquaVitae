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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaRelacao

/** Ecrã de favoritos — placeholder de UI, já ligado ao [FavoritosViewModel]/API real. */
@Composable
fun FavoritosScreen(viewModel: FavoritosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // A barra de navegação usa saveState/restoreState (ver AppNavHost): trocar de aba e voltar não recria a
    // ViewModel, por isso o `init {}` (que só corre uma vez) não chega — sem isto, marcar um favorito noutro
    // ecrã e voltar aqui mostrava sempre o resultado da 1.ª visita (às vezes "vazio"), sem erro nenhum. Apanhado
    // pelo utilizador ao testar ao vivo (2026-09-24) — mesma classe de bug do `CaveViewModel.atualizarAposGuardar`,
    // ver CLAUDE.md.
    LaunchedEffect(Unit) { viewModel.loadFavoritos() }

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
                        items(state.bebidas, key = { it.bebida.id }) { relacao -> FavoritoItem(relacao) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritoItem(relacao: BebidaRelacao) {
    val bebida = relacao.bebida
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = bebida.nome.orEmpty(), style = MaterialTheme.typography.titleMedium)
            Text(text = bebida.categoriaNome.orEmpty(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
