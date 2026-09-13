package pt.aquavitae.android.feature.catalog

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaSummary

/**
 * Ecrã de catálogo (lista + pesquisa de bebidas). Segundo ecrã do fluxo MVP —
 * implementado com lógica real: mostra loading/lista/erro conforme o
 * [CatalogUiState] exposto pelo [CatalogViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onBebidaClick: (Long) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Catálogo", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Pesquisar bebidas") },
            trailingIcon = {
                // Sem dependência de material-icons-core no skeleton: usa-se texto simples.
                IconButton(onClick = { viewModel.search() }) {
                    Text("Ir")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        )

        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is CatalogUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is CatalogUiState.Success -> {
                if (state.bebidas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sem resultados.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.bebidas, key = { it.id }) { bebida ->
                            BebidaListItem(bebida = bebida, onClick = { onBebidaClick(bebida.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BebidaListItem(bebida: BebidaSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = bebida.nome, style = MaterialTheme.typography.titleMedium)
            Text(
                text = listOfNotNull(bebida.categoriaNome, bebida.produtorNome).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "★ ${bebida.ratingMedio} (${bebida.totalReviews})",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
