package pt.aquavitae.android.feature.cave

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.descricaoJanela
import pt.aquavitae.android.data.model.formatarPrecoPt
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.ui.components.BottomNavContentPadding
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder

/**
 * Ecrã "As minhas Caves" (`android/design/caves/01-caves-lista.png`): pílulas das caves + "+" (popup "Nova cave"),
 * estatísticas da cave escolhida, "Prontas a abrir" (com "Consumir") e "Em guarda" (com ordenação).
 */
@Composable
fun CaveScreen(onVerProvadas: () -> Unit = {}, onVerProdutor: (Long) -> Unit = {}, viewModel: CaveViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    // O detalhe de uma bebida é sempre um popup (também "nas caves", ver BebidaCard) — falhava aqui, apanhado a
    // testar o bloqueio da review (2026-09-23): as linhas de garrafa nunca tinham ficado ligadas.
    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            CaveUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is CaveUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is CaveUiState.Ready -> {
                CaveContent(estado, viewModel, onVerProvadas, onBebidaClick = { id -> bebidaSelecionadaId = id })
                if (estado.mostrarNovaCave) {
                    NovaCaveSheet(
                        aCriar = estado.aCriarCave,
                        erro = estado.erroNovaCave,
                        onCriar = viewModel::criarCave,
                        onDismiss = viewModel::fecharNovaCave,
                    )
                }
            }
        }
    }

    bebidaSelecionadaId?.let { id ->
        var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }
        BebidaDetalheSheet(
            bebidaId = id,
            onDismiss = { bebidaSelecionadaId = null },
            onAdicionarACave = { bebidaParaAdicionarACave = it },
            onVerProdutor = onVerProdutor,
        )
        bebidaParaAdicionarACave?.let { bebida ->
            AdicionarACaveSheet(
                bebida = bebida,
                onDismiss = { bebidaParaAdicionarACave = null },
                onGuardado = {
                    bebidaParaAdicionarACave = null
                    viewModel.atualizarAposGuardar()
                },
            )
        }
    }
}

@Composable
private fun CaveContent(state: CaveUiState.Ready, viewModel: CaveViewModel, onVerProvadas: () -> Unit, onBebidaClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = BottomNavContentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text(
                text = buildAnnotatedString {
                    append("As minhas ")
                    withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("Caves") }
                },
                style = AquaText.SectionSerif,
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.caves, key = { it.id }) { cave ->
                        PillChip(text = cave.nome.orEmpty(), selected = cave.id == state.caveSelecionadaId, onClick = { viewModel.selecionarCave(cave.id) })
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Burgundy).clickable(onClick = viewModel::abrirNovaCave),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nova cave", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        if (state.caves.isEmpty()) {
            item { Text(text = "Ainda não tens nenhuma cave. Toca no \"+\" para criar a primeira.", style = AquaText.SmallLink.copy(color = MutedInk)) }
            return@LazyColumn
        }
        val cave = state.caveSelecionada
        if (cave != null) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CaveStatBox("GARRAFAS", cave.totalGarrafas.toString(), Modifier.weight(1f))
                    CaveStatBox("INVESTIDOS", formatarPrecoPt(cave.valorTotal), Modifier.weight(1f))
                    CaveStatBox("A ABRIR JÁ", cave.totalProntasAAbrir.toString(), Modifier.weight(1f))
                }
            }
        }
        if (state.carregandoDetalhe) {
            item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { LoadingDots(dotSize = 8.dp) } }
        }
        state.detalhe?.let { detalhe ->
            if (detalhe.prontasAAbrir.isNotEmpty()) {
                item { Text(text = "Prontas a abrir", style = AquaText.SectionSerif.copy(fontSize = 17.sp)) }
                items(detalhe.prontasAAbrir, key = { it.id }) { garrafa ->
                    GarrafaRow(
                        garrafa,
                        aConsumir = state.aConsumirId == garrafa.id,
                        onConsumir = { viewModel.consumir(garrafa.id) },
                        onClick = { garrafa.bebidaId?.let(onBebidaClick) },
                    )
                }
            }
            if (detalhe.emGuarda.isNotEmpty()) {
                item { OrdenarEmGuardaHeader(state.ordem, viewModel::selecionarOrdem) }
                items(detalhe.emGuarda, key = { it.id }) { garrafa ->
                    GarrafaRow(garrafa, aConsumir = false, onConsumir = {}, onClick = { garrafa.bebidaId?.let(onBebidaClick) })
                }
            }
            if (detalhe.prontasAAbrir.isEmpty() && detalhe.emGuarda.isEmpty()) {
                item { Text(text = "Esta cave ainda não tem garrafas.", style = AquaText.SmallLink.copy(color = MutedInk)) }
            } else {
                item {
                    Text(
                        text = "Ver as ${detalhe.totalGarrafas} garrafas",
                        style = AquaText.Footer.copy(color = Burgundy, fontSize = 11.sp),
                    )
                }
            }
        }
        if (state.provadasRecentes.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Já provadas", style = AquaText.SectionSerif.copy(fontSize = 17.sp), modifier = Modifier.weight(1f))
                    LinkText(text = "ABRIR MAIS", onClick = onVerProvadas, style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
                }
            }
            items(state.provadasRecentes, key = { it.bebida.id }) { relacao ->
                ProvadaResumoRow(relacao, onClick = { onBebidaClick(relacao.bebida.id) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProvadaResumoRow(relacao: BebidaRelacao, onClick: () -> Unit) {
    val bebida = relacao.bebida
    Row(
        Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = bebida.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = bebida.produtorNome.orEmpty(), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
        Text(
            text = bebida.notaPropria?.let { String.format(LocalePt, "%.1f", it) } ?: "—",
            style = AquaText.Label.copy(fontSize = 14.sp, color = if (bebida.notaPropria != null) Burgundy else MutedInk),
        )
    }
}

@Composable
private fun CaveStatBox(label: String, valor: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(modifier = modifier.clip(shape).border(1.dp, RoseBorder, shape).padding(horizontal = 12.dp, vertical = 12.dp)) {
        Text(text = valor, style = AquaText.BebidaNomeSerif.copy(fontSize = 22.sp, color = Burgundy))
        Text(text = label, style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
    }
}

@Composable
private fun OrdenarEmGuardaHeader(ordemAtual: OrdemEmGuarda, onOrdemChange: (OrdemEmGuarda) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Em guarda", style = AquaText.SectionSerif.copy(fontSize = 17.sp), modifier = Modifier.weight(1f))
        Box {
            Text(
                text = "ORDENAR ▾",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
                modifier = Modifier.clickable { aberto = true },
            )
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                OrdemEmGuarda.entries.forEach { opcao ->
                    val texto = if (opcao == ordemAtual) "${opcao.label} ✓" else opcao.label
                    DropdownMenuItem(text = { Text(texto) }, onClick = { onOrdemChange(opcao); aberto = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GarrafaRow(garrafa: CaveBebidaResponse, aConsumir: Boolean, onConsumir: () -> Unit, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onClick).padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = garrafa.quantidade.toString(),
                style = AquaText.BebidaNomeSerif.copy(fontSize = 26.sp, color = Burgundy),
                modifier = Modifier.width(34.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(text = garrafa.bebidaNome.orEmpty(), style = AquaText.BebidaNomeSerif)
                Text(text = garrafa.descricaoJanela(), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
                garrafa.notas?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                garrafa.precoPago?.let { Text(text = formatarPrecoPt(it), style = AquaText.Label.copy(fontSize = 14.sp)) }
                Text(text = "/UN", style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
            }
        }
        if (onConsumirEhUtil(garrafa)) {
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = onConsumir,
                enabled = !aConsumir,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Burgundy),
                border = androidx.compose.foundation.BorderStroke(1.dp, Burgundy),
                modifier = Modifier.align(Alignment.End).height(32.dp),
            ) {
                Text(if (aConsumir) "A consumir…" else "Consumir", style = AquaText.Footer.copy(fontSize = 11.sp))
            }
        }
    }
}

/** Só mostra "Consumir" nas garrafas prontas — o `onConsumir` que se recebe já vem vazio para as em guarda. */
private fun onConsumirEhUtil(garrafa: CaveBebidaResponse) = garrafa.estado.name == "PRONTA" || garrafa.estado.name == "EM_ATRASO"
