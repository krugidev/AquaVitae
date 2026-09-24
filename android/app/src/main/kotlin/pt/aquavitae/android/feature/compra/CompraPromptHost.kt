package pt.aquavitae.android.feature.compra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.CliquePendente
import pt.aquavitae.android.data.model.textoHa
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * Fica por cima de tudo (na `MainActivity`, fora do grafo de navegação) e, sempre que a app volta ao primeiro plano, deixa
 * a [CompraPromptViewModel] ver se há um "Compraste?" por fazer. Sem sessão ou sem cliques por perguntar não desenha nada.
 */
@Composable
fun CompraPromptHost(viewModel: CompraPromptViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_RESUME) viewModel.verificar() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    state.pergunta?.let { clique ->
        PerguntaCompraDialog(
            clique = clique,
            aResponder = state.aResponder,
            onComprei = viewModel::comprei,
            onNaoComprei = viewModel::naoComprei,
            onMaisTarde = viewModel::maisTarde,
        )
    }
    state.bebidaParaCave?.let { bebida ->
        AdicionarACaveSheet(
            bebida = bebida,
            precoSugerido = state.precoSugerido,
            onDismiss = viewModel::fecharCave,
            onGuardado = viewModel::fecharCave,
        )
    }
}

/** "Compraste X?" — a bebida, onde clicou e há quanto tempo; "Sim" leva ao "Adicionar à cave". */
@Composable
private fun PerguntaCompraDialog(
    clique: CliquePendente,
    aResponder: Boolean,
    onComprei: () -> Unit,
    onNaoComprei: () -> Unit,
    onMaisTarde: () -> Unit,
) {
    Dialog(onDismissRequest = onMaisTarde) {
        DialogScrim()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Paper)
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(width = 84.dp, height = 104.dp).clip(RoundedCornerShape(12.dp)).background(CardGray), contentAlignment = Alignment.Center) {
                val url = resolveImageUrl(clique.bebidaImagePath)
                if (url != null) {
                    AsyncImage(model = url, contentDescription = clique.bebidaNome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(104.dp))
                } else {
                    Text(text = "Imagem\nda bebida", style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp), textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Compraste esta bebida?", style = AquaText.SectionSerif.copy(fontSize = 22.sp), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(text = clique.bebidaNome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 16.sp, color = Burgundy), textAlign = TextAlign.Center)
            // Sem artigo ("na Continente" / "no Continente" dependia do género da loja): o nome vai entre parêntesis.
            val onde = clique.retalhistaNome?.let { "($it) " }.orEmpty()
            val quando = textoHa(clique.dataClique).orEmpty()
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tocaste em \"Comprar\" ${onde}${quando}.".replace("  ", " ").replace(" .", "."),
                style = AquaText.Field.copy(fontSize = 13.sp, color = MutedInk),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = "Sim, adicionar à cave", onClick = onComprei, loading = aResponder)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(
                    text = "NÃO COMPREI",
                    style = AquaText.Footer.copy(color = Burgundy, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable(enabled = !aResponder, onClick = onNaoComprei).padding(horizontal = 10.dp, vertical = 12.dp),
                )
                Text(
                    text = "MAIS TARDE",
                    style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable(enabled = !aResponder, onClick = onMaisTarde).padding(horizontal = 10.dp, vertical = 12.dp),
                )
            }
        }
    }
}
