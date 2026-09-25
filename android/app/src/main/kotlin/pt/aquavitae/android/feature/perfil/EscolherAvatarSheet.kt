package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.ui.components.AvatarTileGrid
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * "Escolher avatar" (`android/design/perfil/03-escolher-avatar.png`), aberto ao tocar no avatar do ecrã "Editar
 * perfil". Igual ao passo do onboarding, mais uma pílula "Todos" (sem equivalente lá). Só confirma a escolha
 * localmente (`onConfirmar`) — quem grava é o "Guardar" do ecrã "Editar perfil".
 */
@Composable
fun EscolherAvatarSheet(
    avatarIdAtual: Long?,
    onDismiss: () -> Unit,
    onConfirmar: (Avatar) -> Unit,
    viewModel: EscolherAvatarViewModel = hiltViewModel(key = "escolher-avatar"),
) {
    val state by viewModel.state.collectAsState()
    var selecionadoId by remember { mutableStateOf(avatarIdAtual) }
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buildAnnotatedString {
                            append("Escolhe um ")
                            withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("avatar") }
                        },
                        style = AquaText.SectionSerif,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
                }
                when (val estado = state) {
                    EscolherAvatarUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { LoadingDots() }
                    is EscolherAvatarUiState.Error -> Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                    }
                    is EscolherAvatarUiState.Ready -> Conteudo(estado, viewModel, selecionadoId, onSelecionar = { selecionadoId = it }, onDismiss, onConfirmar)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.Conteudo(
    state: EscolherAvatarUiState.Ready,
    viewModel: EscolherAvatarViewModel,
    selecionadoId: Long?,
    onSelecionar: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirmar: (Avatar) -> Unit,
) {
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { PillChip(text = "Todos", selected = state.categoriaId == null, onClick = { viewModel.selecionarCategoria(null) }) }
            items(state.categorias, key = { it.id }) { categoria ->
                PillChip(text = categoria.nome.orEmpty(), selected = categoria.id == state.categoriaId, onClick = { viewModel.selecionarCategoria(categoria.id) })
            }
        }
        Spacer(Modifier.height(18.dp))
        AvatarTileGrid(avatares = state.visiveis, selectedId = selecionadoId, onSelect = onSelecionar)
        Spacer(Modifier.height(20.dp))
        val escolhido = state.avatares.firstOrNull { it.id == selecionadoId }
        if (escolhido != null) {
            Text(text = "SELECIONADO", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
            Text(text = escolhido.nome.orEmpty(), style = AquaText.Label.copy(fontSize = 15.sp))
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            text = "Usar este avatar",
            onClick = { escolhido?.let { onConfirmar(it); onDismiss() } },
            enabled = escolhido != null,
        )
        Spacer(Modifier.height(20.dp))
    }
}
