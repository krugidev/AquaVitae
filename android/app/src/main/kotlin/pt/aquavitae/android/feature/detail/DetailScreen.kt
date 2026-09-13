package pt.aquavitae.android.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.VinhoDetalhe

/**
 * Ecrã de detalhe de uma bebida — placeholder de UI (o layout final com
 * imagem, ligações a retalhistas, etc. fica para depois), já ligado ao
 * [DetailViewModel]/API real, incluindo a secção condicional de detalhe de
 * vinho quando `categoriaNome == "Vinho"`.
 */
@Composable
fun DetailScreen(
    onNavigateToReviews: (Long) -> Unit,
    onNavigateToCave: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        is DetailUiState.Success -> {
            DetailContent(
                bebida = state.bebida,
                onNavigateToReviews = onNavigateToReviews,
                onNavigateToCave = onNavigateToCave,
                onNavigateToWishlist = onNavigateToWishlist,
                onNavigateToFavoritos = onNavigateToFavoritos,
            )
        }
    }
}

@Composable
private fun DetailContent(
    bebida: BebidaDetail,
    onNavigateToReviews: (Long) -> Unit,
    onNavigateToCave: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(text = bebida.nome, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = listOfNotNull(bebida.categoriaNome, bebida.produtorNome).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "★ ${bebida.ratingMedio} (${bebida.totalReviews} reviews)",
            style = MaterialTheme.typography.bodyMedium,
        )

        bebida.paisOrigemNome?.let { Text("País de origem: $it") }
        bebida.anoProducao?.let { Text("Ano de produção: $it") }
        bebida.teorAlcoolico?.let { Text("Teor alcoólico: $it%") }
        bebida.volumeMl?.let { Text("Volume: ${it}ml") }

        // Secção condicional: só existe quando categoriaNome == "Vinho".
        bebida.vinhoDetalhe?.let { vinho ->
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            VinhoDetalheSection(vinho)
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Button(onClick = { onNavigateToReviews(bebida.id) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver reviews")
        }
        Button(onClick = onNavigateToCave, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Adicionar à Cave")
        }
        Button(onClick = onNavigateToWishlist, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Adicionar à Wishlist")
        }
        Button(onClick = onNavigateToFavoritos, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Ver Favoritos")
        }
    }
}

@Composable
private fun VinhoDetalheSection(vinho: VinhoDetalhe) {
    Text(text = "Detalhe do vinho", style = MaterialTheme.typography.titleMedium)
    vinho.tipo?.let { Text("Tipo: $it") }
    vinho.corpo?.let { Text("Corpo: $it") }
    vinho.nivelAcidez?.let { Text("Acidez: $it/5") }
    vinho.nivelDocura?.let { Text("Doçura: $it/5") }
    vinho.tanino?.let { Text("Taninos: $it") }
    if (vinho.castas.isNotEmpty()) {
        Text(
            text = "Castas: " + vinho.castas.joinToString { "${it.casta} (${it.percentagem}%)" },
        )
    }
}
