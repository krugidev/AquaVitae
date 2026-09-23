package pt.aquavitae.android.feature.cave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val EstiloSeccao = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
private val FormatoData = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("pt", "PT"))

/**
 * O popup "Adicionar à cave" (`android/design/caves/04-adicionar-a-cave-popup.png`), aberto a partir do "+" do
 * popup de detalhe da bebida. Pedido do utilizador: destacar nas pílulas de cave onde a bebida já está
 * (`CaveResponse.temBebida`, `?bebidaId=` em `GET /users/me/caves`) — distinto da pílula que se escolheu para este
 * "adicionar" (podes juntar mais garrafas a uma cave onde já há).
 */
@Composable
fun AdicionarACaveSheet(
    bebida: BebidaDetail,
    onDismiss: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: AdicionarACaveViewModel = hiltViewModel(key = "adicionar-cave-${bebida.id}"),
) {
    LaunchedEffect(bebida.id) { viewModel.carregar(bebida.id) }
    val state by viewModel.state.collectAsState()
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    LaunchedEffect(state) {
        if (state is AdicionarACaveUiState.Ready && (state as AdicionarACaveUiState.Ready).guardado) onGuardado()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            Modifier.fillMaxSize().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } },
        ) {
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
                    Text(text = "Adicionar à cave", style = AquaText.SectionSerif, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
                }
                when (val estado = state) {
                    AdicionarACaveUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { LoadingDots() }
                    is AdicionarACaveUiState.Error -> Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                    }
                    is AdicionarACaveUiState.Ready -> Conteudo(bebida, estado, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.Conteudo(bebida: BebidaDetail, state: AdicionarACaveUiState.Ready, viewModel: AdicionarACaveViewModel) {
    var mostrarDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardGray).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(Paper), contentAlignment = Alignment.Center) {
                val url = resolveImageUrl(bebida.imagePath)
                if (url != null) {
                    AsyncImage(model = url, contentDescription = bebida.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = bebida.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                val linha = listOfNotNull(bebida.produtorNome, bebida.produtorResumo?.regiao).joinToString(" • ")
                if (linha.isNotEmpty()) Text(text = linha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(text = "EM QUE CAVE", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.caves, key = { it.id }) { cave ->
                CavePillComDestaque(
                    texto = cave.nome.orEmpty(),
                    selecionada = cave.id == state.caveSelecionadaId,
                    jaTemBebida = cave.temBebida,
                    onClick = { viewModel.selecionarCave(cave.id) },
                )
            }
            item {
                PillNovaCave(onClick = viewModel::abrirNovaCave)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(text = "QUANTIDADE", style = EstiloSeccao)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PassoBotao(icone = Icons.Filled.Remove, descricao = "Menos", onClick = viewModel::onQuantidadeMenos)
                    Text(text = state.quantidade.toString(), style = AquaText.BebidaNomeSerif.copy(fontSize = 18.sp), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    PassoBotao(icone = Icons.Filled.Add, descricao = "Mais", onClick = viewModel::onQuantidadeMais)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = "PREÇO PAGO /UN", style = EstiloSeccao)
                Spacer(Modifier.height(8.dp))
                CampoTexto(valor = state.precoPago, onValueChange = viewModel::onPrecoPago, placeholder = "0,00", teclado = KeyboardType.Decimal)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(text = "DATA DE AQUISIÇÃO", style = EstiloSeccao)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(BurgundyTint.copy(alpha = 0.25f))
                .clickable { mostrarDatePicker = true }.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = state.dataAquisicao.format(FormatoData), style = AquaText.Field.copy(fontSize = 14.sp), modifier = Modifier.weight(1f))
            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Burgundy, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = "JANELA DE CONSUMO", style = EstiloSeccao, modifier = Modifier.weight(1f))
            Text(text = "OPCIONAL", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { CampoTexto(valor = state.janelaInicioAno, onValueChange = viewModel::onJanelaInicioAno, placeholder = "aaaa", teclado = KeyboardType.Number) }
            Text(text = "até", style = AquaText.Footer.copy(color = MutedInk), modifier = Modifier.padding(horizontal = 10.dp))
            Box(Modifier.weight(1f)) { CampoTexto(valor = state.janelaFimAno, onValueChange = viewModel::onJanelaFimAno, placeholder = "aaaa", teclado = KeyboardType.Number) }
        }
        Spacer(Modifier.height(18.dp))
        Text(text = "NOTAS", style = EstiloSeccao)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BurgundyTint.copy(alpha = 0.25f)).padding(horizontal = 14.dp, vertical = 10.dp)) {
            BasicTextField(
                value = state.notas,
                onValueChange = viewModel::onNotas,
                textStyle = AquaText.Field.copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                decorationBox = { inner ->
                    if (state.notas.isEmpty()) Text("Onde a compraste, para que ocasião a guardas...", style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk))
                    inner()
                },
            )
        }
        state.erro?.let {
            Spacer(Modifier.height(10.dp))
            Text(text = it, style = AquaText.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::guardar,
            enabled = state.caveSelecionadaId != null && !state.aGuardar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
        ) {
            Text(if (state.aGuardar) "A guardar…" else "Guardar na cave", style = AquaText.Button.copy(fontSize = 16.sp))
        }
        Spacer(Modifier.height(20.dp))
    }

    if (state.mostrarNovaCave) {
        NovaCaveSheet(aCriar = state.aCriarCave, erro = state.erroNovaCave, onCriar = viewModel::criarCave, onDismiss = viewModel::fecharNovaCave)
    }

    if (state.mostrarConfirmarDuplicado) {
        val nomeCave = state.caves.firstOrNull { it.id == state.caveSelecionadaId }?.nome.orEmpty()
        AlertDialog(
            onDismissRequest = viewModel::cancelarDuplicado,
            title = { Text("Já tens esta bebida aqui", style = AquaText.Label.copy(fontSize = 16.sp)) },
            text = {
                Text(
                    "A cave \"$nomeCave\" já tem garrafas desta bebida. Queres mesmo adicionar mais?",
                    style = AquaText.Field.copy(fontSize = 14.sp),
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmarDuplicado) { Text("Adicionar na mesma", color = Burgundy) } },
            dismissButton = { TextButton(onClick = viewModel::cancelarDuplicado) { Text("Cancelar", color = MutedInk) } },
        )
    }

    if (mostrarDatePicker) {
        val estadoPicker = rememberDatePickerState(
            initialSelectedDateMillis = state.dataAquisicao.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoPicker.selectedDateMillis?.let { millis ->
                        viewModel.onDataAquisicao(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estadoPicker)
        }
    }
}

/** A pílula de cave: cheia a grená se escolhida para este "adicionar"; um pequeno visto (canto) se a bebida já lá
 * está — as duas coisas são independentes (dá para escolher uma cave onde já há, para juntar mais garrafas). */
@Composable
private fun CavePillComDestaque(texto: String, selecionada: Boolean, jaTemBebida: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selecionada) Burgundy else Color.Transparent)
            .border(1.5.dp, Burgundy, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (jaTemBebida) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Já tens esta bebida aqui",
                    tint = if (selecionada) Color.White else Burgundy,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(text = texto, style = AquaText.Label.copy(fontSize = 13.sp, color = if (selecionada) Color.White else Burgundy))
        }
    }
}

@Composable
private fun PillNovaCave(onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier.height(36.dp).clip(shape).border(1.5.dp, MutedInk.copy(alpha = 0.4f), shape).clickable(onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "+ nova cave", style = AquaText.Label.copy(fontSize = 13.sp, color = MutedInk))
    }
}

@Composable
private fun PassoBotao(icone: androidx.compose.ui.graphics.vector.ImageVector, descricao: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(Burgundy).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = descricao, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CampoTexto(valor: String, onValueChange: (String) -> Unit, placeholder: String, teclado: KeyboardType) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(BurgundyTint.copy(alpha = 0.25f)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (valor.isEmpty()) Text(text = placeholder, style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk))
            BasicTextField(
                value = valor,
                onValueChange = onValueChange,
                textStyle = AquaText.Field.copy(fontSize = 14.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = teclado),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
