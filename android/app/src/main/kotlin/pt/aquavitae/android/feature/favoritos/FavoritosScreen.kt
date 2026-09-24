package pt.aquavitae.android.feature.favoritos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pt.aquavitae.android.R
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.linhaAtributos
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.BottomNavContentPadding
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder

/**
 * "Os meus favoritos" (`android/design/favoritos-wishlist/02-favoritos.png`): filtro por categoria (só as presentes
 * nos favoritos), nota própria vs. média da comunidade, e "Ver a tua review"/"Avaliar" conforme já tenhas publicado
 * uma review — o próprio popup de detalhe já bloqueia "Avaliar" se a bebida ainda não estiver provada (fatia 3c).
 * "PROVADA EM <mês>" e "N NA CAVE" do mockup ficam por fazer — pedem campos novos no `BebidaSummaryDto`, adiado a
 * pedido do utilizador (ver `PLANO.md`, "Por fazer depois").
 */
@Composable
fun FavoritosScreen(onVerPerfil: () -> Unit = {}, viewModel: FavoritosViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsState()
    // A barra de navegação preserva a ViewModel ao trocar de aba (saveState/restoreState) — sem isto, marcar um
    // favorito noutro ecrã e voltar aqui não se refletia (ver CLAUDE.md).
    LaunchedEffect(Unit) { viewModel.carregar() }

    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val state = uiState) {
            is FavoritosUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is FavoritosUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = state.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is FavoritosUiState.Ready -> FavoritosContent(
                state = state,
                viewModel = viewModel,
                onBebidaClick = { bebidaSelecionadaId = it },
                onVerPerfil = onVerPerfil,
            )
        }
    }

    bebidaSelecionadaId?.let { id ->
        BebidaDetalheSheet(
            bebidaId = id,
            onDismiss = { bebidaSelecionadaId = null },
            onAdicionarACave = { bebidaParaAdicionarACave = it },
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
private fun FavoritosContent(
    state: FavoritosUiState.Ready,
    viewModel: FavoritosViewModel,
    onBebidaClick: (Long) -> Unit,
    onVerPerfil: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = BottomNavContentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.ic_wordmark_home), contentDescription = "AquaVitae", modifier = Modifier.height(20.dp))
                Spacer(Modifier.weight(1f))
                AvatarBadge(avatar = state.avatar, iniciais = state.iniciais, onClick = onVerPerfil)
            }
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("Os meus ")
                    withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("favoritos") }
                },
                style = AquaText.SectionSerif,
            )
        }
        item {
            Text(
                text = "${state.favoritos.size} favoritos • ${state.totalProvadas} provadas • ${state.comNotaTua} com nota tua",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { PillChip(text = "Todas", selected = state.categoriaFiltro == null, onClick = { viewModel.selecionarCategoria(null) }) }
                items(state.categorias) { categoria ->
                    PillChip(text = categoria, selected = state.categoriaFiltro == categoria, onClick = { viewModel.selecionarCategoria(categoria) })
                }
            }
        }
        if (state.filtrados.isEmpty()) {
            item { Text(text = "Ainda não tens favoritos.", style = AquaText.SmallLink.copy(color = MutedInk)) }
        } else {
            items(state.visiveis, key = { it.bebida.id }) { relacao ->
                Column {
                    FavoritoCard(
                        relacao = relacao,
                        onClick = { onBebidaClick(relacao.bebida.id) },
                        onRemover = { viewModel.removerFavorito(relacao.bebida.id) },
                    )
                    HorizontalDivider(color = RoseBorder)
                }
            }
            if (state.temMais) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        LinkText(text = "Ver as ${state.filtrados.size} garrafas favoritas", onClick = viewModel::expandir, style = AquaText.SmallLink)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritoCard(relacao: BebidaRelacao, onClick: () -> Unit, onRemover: () -> Unit) {
    val bebida = relacao.bebida
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)) {
        Box(Modifier.size(width = 72.dp, height = 92.dp).clip(RoundedCornerShape(10.dp)).background(CardGray)) {
            val url = resolveImageUrl(bebida.imagePath)
            if (url != null) AsyncImage(model = url, contentDescription = bebida.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(text = bebida.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val linha = listOfNotNull(bebida.produtorNome, bebida.produtorRegiao).joinToString(" • ")
                    Text(text = linha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = bebida.linhaAtributos(), style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Remover dos favoritos",
                    tint = Burgundy,
                    modifier = Modifier.size(20.dp).clickable(onClick = onRemover),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(
                        text = bebida.notaPropria?.let { String.format(LocalePt, "%.1f", it) } ?: "—",
                        style = AquaText.BebidaNomeSerif.copy(fontSize = 17.sp, color = if (bebida.notaPropria != null) Burgundy else MutedInk),
                    )
                    Text(text = if (bebida.notaPropria != null) "A TUA NOTA" else "SEM NOTA TUA", style = AquaText.Footer.copy(color = MutedInk, fontSize = 8.sp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(text = String.format(LocalePt, "%.1f", bebida.ratingMedio), style = AquaText.Label.copy(fontSize = 14.sp))
                    Text(text = "MÉDIA • ${bebida.totalReviews} REVIEWS", style = AquaText.Footer.copy(color = MutedInk, fontSize = 8.sp))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Burgundy),
                border = BorderStroke(1.dp, Burgundy),
                modifier = Modifier.height(32.dp),
            ) {
                Text(if (bebida.notaPropria != null) "Ver a tua review" else "Avaliar", style = AquaText.Footer.copy(fontSize = 11.sp))
            }
        }
    }
}
