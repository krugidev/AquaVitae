package pt.aquavitae.android.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.ui.components.DialogFillScreen
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.components.LookupContent
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.RangePillRow
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

private val FormaFolha = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val EstiloSeccao = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

/**
 * O popup "Filtros" do catálogo (`android/design/catalogo/01-filtros-popup.png`): categoria, origem (país + região),
 * preço, rating mínimo e, só com "Vinho" escolhido, os atributos do vinho (acidez, doçura, tipo, corpo, castas).
 * Edita um rascunho à parte de [CatalogViewModel] — "Ver N bebidas" aplica-o à lista de baixo; fechar de outra forma
 * (fora, ou o gesto de voltar) descarta. Sobre `Dialog`, o mesmo padrão do `TermsSheet` (`DialogScrim`/`DialogFillScreen`)
 * mas sem o arrasto para fechar (não é preciso aqui: o conteúdo já rola por dentro).
 */
@Composable
fun FiltrosSheet(
    state: CatalogUiState.Ready,
    viewModel: CatalogViewModel,
    onDismiss: () -> Unit,
) {
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        DialogScrim()
        DialogFillScreen()
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = alturaBarraNavegacao)
                .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(FormaFolha)
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Filtros", style = AquaText.SectionSerif, modifier = Modifier.weight(1f))
                    if (state.rascunho.totalAtivos > 0) {
                        Text(
                            text = "LIMPAR ${state.rascunho.totalAtivos}",
                            style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
                            modifier = Modifier.clickable(onClick = viewModel::limparRascunho),
                        )
                    }
                }

                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                    SeccaoCategoria(state, viewModel)
                    Spacer(Modifier.height(22.dp))
                    SeccaoOrigem(state, viewModel)
                    Spacer(Modifier.height(22.dp))
                    SeccaoPreco(state, viewModel)
                    Spacer(Modifier.height(22.dp))
                    SeccaoRating(state, viewModel)
                    if (state.rascunhoEhVinho) {
                        Spacer(Modifier.height(18.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(MutedInk.copy(alpha = 0.15f)))
                        Spacer(Modifier.height(18.dp))
                        SeccaoAtributosVinho(state, viewModel)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)) {
                    val texto = when {
                        state.aContarRascunho || state.contagemRascunho == null -> "Ver bebidas"
                        else -> "Ver ${state.contagemRascunho} bebidas"
                    }
                    Button(
                        onClick = { viewModel.aplicarFiltros(); onDismiss() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
                    ) {
                        Text(texto, style = AquaText.Button.copy(fontSize = 16.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccaoCategoria(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    Column {
        Text(text = "CATEGORIA", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        FlowPills(state.categorias.map { it.id to it.nome.orEmpty() }, selecionados = setOfNotNull(state.rascunho.categoriaId)) {
            viewModel.selecionarCategoriaRascunho(it)
        }
    }
}

@Composable
private fun SeccaoOrigem(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    Column {
        Text(text = "ORIGEM", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        LookupContent(state.paises, onRetry = {}) { paises ->
            var aberto by remember { mutableStateOf(false) }
            val nomePais = paises.firstOrNull { it.id == state.rascunho.paisId }?.nome ?: "Escolher país"
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BurgundyTint.copy(alpha = 0.4f))
                        .clickable { aberto = true }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = nomePais, style = AquaText.Field, modifier = Modifier.weight(1f))
                    Text(text = "▾", style = AquaText.Label)
                }
                DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                    paises.forEach { pais ->
                        DropdownMenuItem(text = { Text(pais.nome.orEmpty()) }, onClick = { viewModel.selecionarPais(pais.id); aberto = false })
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LookupContent(state.regioes, onRetry = {}) { regioes ->
            if (regioes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(regioes, key = { it.id }) { regiao ->
                        PillChip(text = regiao.nome.orEmpty(), selected = regiao.id in state.rascunho.regiaoIds, onClick = { viewModel.toggleRegiao(regiao.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccaoPreco(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    val min = state.rascunho.precoMin?.toFloat() ?: PRECO_MIN
    val max = state.rascunho.precoMax?.toFloat() ?: PRECO_MAX
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(text = "PREÇO", style = EstiloSeccao, modifier = Modifier.weight(1f))
            Text(text = "${min.toInt()}€ – ${if (max >= PRECO_MAX) "${PRECO_MAX.toInt()}€+" else "${max.toInt()}€"}", style = AquaText.Footer.copy(color = Burgundy, fontSize = 11.sp))
        }
        Spacer(Modifier.height(4.dp))
        RangeSlider(
            value = min..max,
            onValueChange = { viewModel.setPreco(it.start.toDouble(), it.endInclusive.toDouble()) },
            valueRange = PRECO_MIN..PRECO_MAX,
            colors = SliderDefaults.colors(thumbColor = Burgundy, activeTrackColor = Burgundy, inactiveTrackColor = BurgundyTint),
        )
    }
}

private const val PRECO_MIN = 0f
private const val PRECO_MAX = 150f

@Composable
private fun SeccaoRating(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    Column {
        Text(text = "RATING MÍNIMO", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos" to null, "3+" to 3.0, "4+" to 4.0, "4,5+" to 4.5).forEach { (label, valor) ->
                PillChip(text = label, selected = state.rascunho.ratingMin == valor, onClick = { viewModel.selecionarRatingMin(valor) })
            }
        }
    }
}

@Composable
private fun SeccaoAtributosVinho(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    Column {
        Text(text = "ATRIBUTOS DE VINHO", style = EstiloSeccao)
        Spacer(Modifier.height(14.dp))
        Text(text = "Acidez", style = AquaText.Option.copy(fontSize = 14.sp))
        Spacer(Modifier.height(8.dp))
        RangePillRow(min = state.rascunho.acidezMin, max = state.rascunho.acidezMax, onRangeChange = viewModel::setAcidez)
        Spacer(Modifier.height(16.dp))
        Text(text = "Doçura", style = AquaText.Option.copy(fontSize = 14.sp))
        Spacer(Modifier.height(8.dp))
        RangePillRow(min = state.rascunho.docuraMin, max = state.rascunho.docuraMax, onRangeChange = viewModel::setDocura)
        Spacer(Modifier.height(16.dp))
        Text(text = "Tipo", style = AquaText.Option.copy(fontSize = 14.sp))
        Spacer(Modifier.height(8.dp))
        LookupContent(state.tipos, onRetry = {}) { tipos ->
            FlowPills(tipos.map { it.id to it.nome.orEmpty() }, selecionados = setOfNotNull(state.rascunho.tipoId)) { viewModel.selecionarTipo(it) }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "Corpo", style = AquaText.Option.copy(fontSize = 14.sp))
        Spacer(Modifier.height(8.dp))
        LookupContent(state.corpos, onRetry = {}) { corpos ->
            FlowPills(corpos.map { it.id to it.nome.orEmpty() }, selecionados = setOfNotNull(state.rascunho.corpoId)) { viewModel.selecionarCorpo(it) }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(text = "Castas", style = AquaText.Option.copy(fontSize = 14.sp), modifier = Modifier.weight(1f))
            LookupContent(state.castas, onRetry = {}) { Text(text = "PROCURAR NAS ${it.size}", style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp)) }
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
        val escolhidas = state.castasEscolhidas
        if (escolhidas.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(escolhidas, key = { it.id }) { casta -> CastaChipRemovivel(casta, onClick = { viewModel.toggleCasta(casta.id) }) }
            }
            Spacer(Modifier.height(8.dp))
        }
        val sugeridas = state.castasSugeridas
        if (sugeridas.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sugeridas, key = { it.id }) { casta ->
                    PillChip(text = casta.nome.orEmpty(), selected = false, onClick = { viewModel.toggleCasta(casta.id) })
                }
            }
        }
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

/**
 * Pílulas de escolha única (categoria, tipo, corpo) numa linha com scroll horizontal — como as pílulas de categoria da
 * homepage, em vez de quebrarem linha (simplificação: evita depender do `FlowRow` do Compose para esta 1.ª versão).
 */
@Composable
private fun FlowPills(itens: List<Pair<Long, String>>, selecionados: Set<Long>, onClick: (Long) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(itens, key = { it.first }) { (id, nome) -> PillChip(text = nome, selected = id in selecionados, onClick = { onClick(id) }) }
    }
}
