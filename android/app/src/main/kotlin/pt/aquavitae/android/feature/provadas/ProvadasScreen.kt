package pt.aquavitae.android.feature.provadas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.TextStyle as JavaTextStyle

private val EstiloSeccao = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)

/**
 * O ecrã "Já provadas": todas as bebidas que o utilizador já marcou como consumidas — por "Consumir" numa cave, ou
 * adicionadas diretamente aqui (a barra de pesquisa, pedida pelo utilizador — não está no mockup, só as caves lá
 * chegam automaticamente). Agrupadas por mês; filtros por nota e por categoria.
 */
@Composable
fun ProvadasScreen(onVoltar: () -> Unit, viewModel: ProvadasViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            ProvadasUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is ProvadasUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is ProvadasUiState.Ready -> ProvadasContent(estado, viewModel, onVoltar)
        }
    }
}

@Composable
private fun ProvadasContent(state: ProvadasUiState.Ready, viewModel: ProvadasViewModel, onVoltar: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Burgundy) }
            }
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("Já ")
                    withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("provadas") }
                },
                style = AquaText.SectionSerif,
            )
        }
        item {
            val comNota = state.todas.count { it.bebida.notaPropria != null }
            Text(
                text = "${state.todas.size} bebidas • $comNota com nota tua",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
            )
        }
        item { BarraBusca(state, viewModel) }
        if (state.busca.isNotBlank()) {
            item { ResultadosBusca(state, viewModel) }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FiltroNota.entries.toList()) { filtro ->
                    PillChip(text = filtro.label, selected = state.categoriaFiltroId == null && state.filtroNota == filtro, onClick = { viewModel.selecionarFiltroNota(filtro) })
                }
                items(state.categorias, key = { it.id }) { categoria ->
                    PillChip(text = categoria.nome.orEmpty(), selected = state.categoriaFiltroId == categoria.id, onClick = { viewModel.selecionarCategoria(categoria.id) })
                }
            }
        }
        if (state.gruposVisiveis.isEmpty()) {
            item { Text(text = "Sem bebidas com estes filtros.", style = AquaText.SmallLink.copy(color = MutedInk)) }
        }
        state.gruposVisiveis.forEach { (mes, itens) ->
            item { Text(text = mes, style = EstiloSeccao.copy(color = Burgundy)) }
            items(itens, key = { it.bebida.id }) { relacao -> ProvadaRow(relacao) }
        }
        if (state.temMaisMeses) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    LinkText(text = "Carregar mais antigas", onClick = viewModel::carregarMaisMeses, style = AquaText.SmallLink)
                }
            }
        }
    }
}

@Composable
private fun BarraBusca(state: ProvadasUiState.Ready, viewModel: ProvadasViewModel) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier.fillMaxWidth().height(46.dp).clip(shape).background(BurgundyTint.copy(alpha = 0.25f)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = MutedInk, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (state.busca.isEmpty()) {
                Text("Adicionar uma bebida que não guardaste em nenhuma cave...", style = AquaText.Field.copy(fontSize = 13.sp, color = MutedInk))
            }
            BasicTextField(value = state.busca, onValueChange = viewModel::onBusca, textStyle = AquaText.Field.copy(fontSize = 13.sp), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ResultadosBusca(state: ProvadasUiState.Ready, viewModel: ProvadasViewModel) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardGray)) {
        if (state.aBuscar) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { LoadingDots(dotSize = 6.dp) }
        } else if (state.resultadosBusca.isEmpty()) {
            Text(text = "Sem resultados.", style = AquaText.SmallLink.copy(color = MutedInk), modifier = Modifier.padding(16.dp))
        } else {
            state.resultadosBusca.forEach { bebida -> ResultadoBuscaRow(bebida, aAdicionar = state.aAdicionarId == bebida.id, onAdicionar = { viewModel.adicionarDaBusca(bebida) }) }
        }
    }
}

@Composable
private fun ResultadoBuscaRow(bebida: BebidaSummary, aAdicionar: Boolean, onAdicionar: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(text = bebida.nome.orEmpty(), style = AquaText.Label.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            val linha = listOfNotNull(bebida.categoriaNome, bebida.produtorNome).joinToString(" • ")
            Text(text = linha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        }
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(Burgundy).clickable(enabled = !aAdicionar, onClick = onAdicionar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Adicionar a já provadas", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ProvadaRow(relacao: BebidaRelacao) {
    val bebida = relacao.bebida
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(CardGray), contentAlignment = Alignment.Center) {
            val url = resolveImageUrl(bebida.imagePath)
            if (url != null) AsyncImage(model = url, contentDescription = bebida.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = bebida.nome.orEmpty(), style = AquaText.Label.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            val linha = listOfNotNull(bebida.produtorNome, bebida.produtorRegiao).joinToString(" • ")
            Text(text = linha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = bebida.notaPropria?.let { String.format(LocalePt, "%.1f", it) } ?: "—",
                style = AquaText.Label.copy(fontSize = 15.sp, color = if (bebida.notaPropria != null) Burgundy else MutedInk),
            )
            Text(text = formatarDataCurta(relacao.data), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
    }
}

/** "12 de setembro" — sem ano (a maioria das bebidas provadas é recente; o mês já vai no cabeçalho do grupo). */
private fun formatarDataCurta(dataIso: String?): String {
    if (dataIso == null) return ""
    return runCatching {
        val data = Instant.parse(dataIso).atZone(ZoneOffset.UTC)
        val mes = data.month.getDisplayName(JavaTextStyle.FULL, LocalePt)
        "${data.dayOfMonth} de $mes"
    }.getOrDefault("")
}
