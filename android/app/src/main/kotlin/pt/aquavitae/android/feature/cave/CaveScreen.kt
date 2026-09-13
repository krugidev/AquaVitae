package pt.aquavitae.android.feature.cave

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
import pt.aquavitae.android.data.model.CaveResponse

/**
 * Ecrã da Cave Virtual — placeholder de UI (lista simples de caves; o detalhe
 * de cada cave com as garrafas fica para um ecrã seguinte), já ligado ao
 * [CaveViewModel]/API real.
 */
@Composable
fun CaveScreen(viewModel: CaveViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "A minha Cave", style = MaterialTheme.typography.headlineMedium)

        when (val state = uiState) {
            is CaveUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is CaveUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }

            is CaveUiState.Success -> {
                if (state.caves.isEmpty()) {
                    Text("Ainda não tens nenhuma cave. TODO: botão \"Criar cave\".")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        items(state.caves, key = { it.id }) { cave -> CaveItem(cave) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaveItem(cave: CaveResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = cave.nome, style = MaterialTheme.typography.titleMedium)
            cave.descricao?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
