package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.PreferenciaResponse
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.components.RangePillRow
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * "Editar preferências" (`android/design/perfil/04-editar-preferencias.png`), aberto do "EDITAR" no cartão de
 * preferências do ecrã de perfil. Mesmos dados do onboarding (tipos de bebida, acidez/doçura, castas), aqui em
 * pílulas horizontais compactas — o `RangePillRow` já existia para o popup de filtros do catálogo, reutilizado aqui
 * tal e qual. Guardar aplica-se de imediato em "Escolhido para ti" na homepage (nada de novo a fazer para isso).
 */
@Composable
fun EditarPreferenciasSheet(
    preferenciasAtuais: PreferenciaResponse,
    onDismiss: () -> Unit,
    onGuardado: (PreferenciaResponse) -> Unit,
    viewModel: EditarPreferenciasViewModel = hiltViewModel(key = "editar-preferencias"),
) {
    LaunchedEffect(Unit) { viewModel.inicializar(preferenciasAtuais) }
    val state by viewModel.state.collectAsState()
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "PERFIL", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
                }
                when (val estado = state) {
                    EditarPreferenciasUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { LoadingDots() }
                    is EditarPreferenciasUiState.Error -> Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                    }
                    is EditarPreferenciasUiState.Ready -> Conteudo(estado, viewModel, onGuardado)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.Conteudo(state: EditarPreferenciasUiState.Ready, viewModel: EditarPreferenciasViewModel, onGuardado: (PreferenciaResponse) -> Unit) {
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Text(
            text = buildAnnotatedString {
                append("As minhas ")
                withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("preferências") }
            },
            style = AquaText.SectionSerif,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "São as mesmas do início, e mandam no que aparece em \"Escolhido para ti\".",
            style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp),
        )
        Spacer(Modifier.height(18.dp))
        Text(text = "QUE TIPOS DE BEBIDA PREFERES?", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.categorias, key = { it.id }) { categoria ->
                PillChip(text = categoria.nome.orEmpty(), selected = categoria.id in state.categoriaIds, onClick = { viewModel.toggleCategoria(categoria.id) })
            }
        }
        Spacer(Modifier.height(18.dp))
        NivelSecao(titulo = "DOÇURA", min = state.docuraMin, max = state.docuraMax, rotuloMin = "MUITO SECA", rotuloMax = "MUITO DOCE", onChange = viewModel::onDocura)
        Spacer(Modifier.height(16.dp))
        NivelSecao(titulo = "ACIDEZ", min = state.acidezMin, max = state.acidezMax, rotuloMin = "MUITO MACIA", rotuloMax = "MUITO FRESCA", onChange = viewModel::onAcidez)
        Text(
            text = "Só se aplica a vinho — é a única categoria com nível de acidez no catálogo.",
            style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = "CASTAS QUE APRECIAS", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), modifier = Modifier.weight(1f))
            Text(text = "${state.castaIds.size} DE ${state.castas.size}", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(BurgundyTint.copy(alpha = 0.4f)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = state.castaBusca,
                onValueChange = viewModel::onCastaBusca,
                textStyle = AquaText.Field.copy(fontSize = 14.sp),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { inner -> if (state.castaBusca.isEmpty()) Text("Procurar casta...", style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk)); inner() },
            )
        }
        Spacer(Modifier.height(10.dp))
        if (state.castasEscolhidas.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.castasEscolhidas, key = { it.id }) { casta -> CastaChipRemovivel(casta, onClick = { viewModel.toggleCasta(casta.id) }) }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (state.castasSugeridas.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.castasSugeridas, key = { it.id }) { casta ->
                    PillChip(text = casta.nome.orEmpty(), selected = false, onClick = { viewModel.toggleCasta(casta.id) })
                }
            }
        }
        state.erro?.let {
            Spacer(Modifier.height(10.dp))
            Text(text = it, style = AquaText.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = "Guardar preferências", onClick = { viewModel.guardar(onGuardado) }, loading = state.aGuardar)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun NivelSecao(titulo: String, min: Int?, max: Int?, rotuloMin: String, rotuloMax: String, onChange: (Int, Int) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        Text(text = titulo, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), modifier = Modifier.weight(1f))
        Text(text = if (min != null && max != null) "$min a $max" else "—", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
    }
    Spacer(Modifier.height(8.dp))
    RangePillRow(min = min, max = max, onRangeChange = onChange)
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(text = rotuloMin, style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp), modifier = Modifier.weight(1f))
        Text(text = rotuloMax, style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
    }
}

@Composable
private fun CastaChipRemovivel(casta: Casta, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier.height(36.dp).clip(shape).background(Burgundy).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = casta.nome.orEmpty(), style = AquaText.Label.copy(fontSize = 13.sp, color = Color.White))
        Spacer(Modifier.width(6.dp))
        Text(text = "×", style = AquaText.Label.copy(fontSize = 15.sp, color = Color.White))
    }
}
