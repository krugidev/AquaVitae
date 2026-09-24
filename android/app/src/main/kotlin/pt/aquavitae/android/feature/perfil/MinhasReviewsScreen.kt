package pt.aquavitae.android.feature.perfil

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.MinhaReview
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.EstrelasRating
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.iniciaisDe
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder

private val EstiloSeccao = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp, fontWeight = FontWeight.Bold)

/**
 * "As minhas reviews" (`android/design/perfil/05-reviews-e-conta-seguranca.png`, imagem 9a): as reviews do utilizador agrupadas por
 * mês, com pílulas de mês que filtram, o excerto (~90 caracteres), as estrelas (com meias) e a data. Tocar num cartão abre o popup
 * de detalhe da bebida (o mesmo de todos os ecrãs).
 */
@Composable
fun MinhasReviewsScreen(
    onVoltar: () -> Unit,
    onVerProdutor: ((Long) -> Unit)? = null,
    viewModel: MinhasReviewsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }
    // Voltar a este ecrã (ou fechar o popup depois de mudar uma review) pede a lista de novo: uma review nova ou apagada mexe nela.
    LaunchedEffect(Unit) { viewModel.carregar() }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            MinhasReviewsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is MinhasReviewsUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
                LinkText(text = "VOLTAR", onClick = onVoltar)
            }

            is MinhasReviewsUiState.Ready -> Conteudo(estado, viewModel, onVoltar, onBebidaClick = { bebidaSelecionadaId = it })
        }
    }

    bebidaSelecionadaId?.let { id ->
        BebidaDetalheSheet(
            bebidaId = id,
            onDismiss = {
                bebidaSelecionadaId = null
                viewModel.carregar()
            },
            onAdicionarACave = { bebidaParaAdicionarACave = it },
            onVerProdutor = onVerProdutor,
        )
    }
    bebidaParaAdicionarACave?.let { bebida ->
        AdicionarACaveSheet(
            bebida = bebida,
            onDismiss = { bebidaParaAdicionarACave = null },
            onGuardado = { bebidaParaAdicionarACave = null },
        )
    }
}

@Composable
private fun Conteudo(
    state: MinhasReviewsUiState.Ready,
    viewModel: MinhasReviewsViewModel,
    onVoltar: () -> Unit,
    onBebidaClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Burgundy) }
                Text(text = "PERFIL", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
            }
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("As minhas ")
                    withStyle(AquaText.SectionSerifAccent.copy(fontStyle = FontStyle.Italic).toSpanStyle()) { append("reviews") }
                },
                style = AquaText.SectionSerif.copy(fontSize = 28.sp),
            )
        }
        item {
            val total = state.todas.size
            Text(
                text = "$total ${if (total == 1) "REVIEW PUBLICADA" else "REVIEWS PUBLICADAS"}",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
            )
        }
        if (state.todas.isEmpty()) {
            item {
                Text(
                    text = "Ainda não escreveste nenhuma review. Abre uma bebida que já provaste e conta-nos o que achaste.",
                    style = AquaText.SmallLink.copy(color = MutedInk),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            return@LazyColumn
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { PillChip(text = "Todos", selected = state.mes == null, onClick = { viewModel.selecionarMes(null) }) }
                // O ano só se escreve nas pílulas de meses que não são do ano da review mais recente.
                val anoDeReferencia = state.meses.firstOrNull()?.ano ?: 0
                items(state.meses, key = { it.ano * 100 + it.mes }) { mes ->
                    PillChip(text = mes.pilula(anoDeReferencia), selected = state.mes == mes, onClick = { viewModel.selecionarMes(mes) })
                }
            }
        }
        state.grupos.forEach { grupo ->
            item(key = "grupo-${grupo.cabecalho}") {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = grupo.cabecalho, style = EstiloSeccao, modifier = Modifier.weight(1f))
                    val n = grupo.reviews.size
                    Text(text = "$n ${if (n == 1) "REVIEW" else "REVIEWS"}", style = EstiloSeccao)
                }
            }
            items(grupo.reviews, key = { it.id }) { review ->
                ReviewCard(review, state.utilizador, onClick = { onBebidaClick(review.bebida.id) })
            }
        }
    }
}

@Composable
private fun ReviewCard(review: MinhaReview, utilizador: UtilizadorMe?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, RoseBorder, shape).clickable(onClick = onClick).padding(14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = review.bebida.nome.orEmpty(),
                style = AquaText.Label.copy(fontSize = 14.sp, color = Ink),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            val nota = review.rating
            if (nota != null) {
                EstrelasRating(nota = nota, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = String.format(LocalePt, "%.1f", nota), style = AquaText.SectionSerif.copy(color = Burgundy, fontSize = 17.sp))
            }
        }
        excertoDaReview(review.comment)?.let { excerto ->
            Spacer(Modifier.height(6.dp))
            Text(text = excerto, style = AquaText.Field.copy(fontSize = 13.sp), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(thickness = 1.dp, color = RoseBorder)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarBadge(
                avatar = utilizador?.avatar,
                iniciais = iniciaisDe(utilizador?.firstName, utilizador?.lastName, utilizador?.username),
                size = 30.dp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = utilizador?.nomeParaMostrar.orEmpty(),
                style = AquaText.Label.copy(fontSize = 12.sp, color = Ink),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = dataCurtaDaReview(review.createdAt), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
    }
}
