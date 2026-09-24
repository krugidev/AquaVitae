package pt.aquavitae.android.feature.wishlist

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
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
import pt.aquavitae.android.data.model.formatarPrecoPt
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
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle as JavaTextStyle

/**
 * "A minha wishlist" (`android/design/favoritos-wishlist/01-wishlist.png`): ordenar por recentes/preço/rating,
 * "Comprar em X" e "Para a cave". "MENOR DE N RETALHISTAS"/"ATUALIZADA HOJE" e a deteção de descidas de preço do
 * mockup ficam por fazer — pedem campos novos no `BebidaSummaryDto` (o link de compra completo, a data de
 * verificação, quantas ofertas ativas), adiado a pedido do utilizador (ver `PLANO.md`, "Por fazer depois"); por
 * agora "Comprar em X" abre o popup de detalhe da bebida em vez do link do retalhista.
 */
@Composable
fun WishlistScreen(onVerPerfil: () -> Unit = {}, viewModel: WishlistViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsState()
    // Ver o comentário equivalente em FavoritosScreen: sem isto, o ecrã ficava preso ao resultado da 1.ª visita.
    LaunchedEffect(Unit) { viewModel.carregar() }

    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val state = uiState) {
            is WishlistUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is WishlistUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = state.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is WishlistUiState.Ready -> WishlistContent(
                state = state,
                viewModel = viewModel,
                onBebidaClick = { bebidaSelecionadaId = it },
                onParaCave = { id -> viewModel.prepararParaCave(id) { detalhe -> bebidaParaAdicionarACave = detalhe } },
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
private fun WishlistContent(
    state: WishlistUiState.Ready,
    viewModel: WishlistViewModel,
    onBebidaClick: (Long) -> Unit,
    onParaCave: (Long) -> Unit,
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
                    append("A minha ")
                    withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("wishlist") }
                },
                style = AquaText.SectionSerif,
            )
        }
        item {
            Text(text = "${state.itens.size} bebidas", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OrdemWishlist.entries.toList()) { ordem ->
                    PillChip(text = ordem.label, selected = state.ordem == ordem, onClick = { viewModel.selecionarOrdem(ordem) })
                }
            }
        }
        if (state.itens.isEmpty()) {
            item { Text(text = "A tua wishlist está vazia.", style = AquaText.SmallLink.copy(color = MutedInk)) }
        } else {
            items(state.ordenados, key = { it.bebida.id }) { relacao ->
                Column {
                    WishlistCard(
                        relacao = relacao,
                        aAbrirCave = state.aAbrirCaveId == relacao.bebida.id,
                        onClick = { onBebidaClick(relacao.bebida.id) },
                        onRemover = { viewModel.remover(relacao.bebida.id) },
                        onComprar = { onBebidaClick(relacao.bebida.id) },
                        onParaCave = { onParaCave(relacao.bebida.id) },
                    )
                    HorizontalDivider(color = RoseBorder)
                }
            }
        }
    }
}

@Composable
private fun WishlistCard(
    relacao: BebidaRelacao,
    aAbrirCave: Boolean,
    onClick: () -> Unit,
    onRemover: () -> Unit,
    onComprar: () -> Unit,
    onParaCave: () -> Unit,
) {
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
                    Icons.Filled.Bookmark,
                    contentDescription = "Remover da wishlist",
                    tint = Burgundy,
                    modifier = Modifier.size(20.dp).clickable(onClick = onRemover),
                )
            }
            Spacer(Modifier.height(6.dp))
            bebida.precoDesde?.let { preco ->
                Text(text = formatarPrecoPt(preco), style = AquaText.BebidaNomeSerif.copy(fontSize = 17.sp, color = Burgundy))
            }
            Text(text = "ADICIONADA ${formatarAdicionada(relacao.data)}".uppercase(LocalePt), style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bebida.retalhistaNome != null) {
                    Button(
                        onClick = onComprar,
                        colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text("Comprar em ${bebida.retalhistaNome}", style = AquaText.Footer.copy(fontSize = 10.sp, color = Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                OutlinedButton(
                    onClick = onParaCave,
                    enabled = !aAbrirCave,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Burgundy),
                    border = BorderStroke(1.dp, Burgundy),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text(if (aAbrirCave) "A abrir…" else "Para a cave", style = AquaText.Footer.copy(fontSize = 11.sp))
                }
            }
        }
    }
}

/** "há 3 dias" (recente) ou "em junho" (mais antiga, sem ano — igual ao critério de `ProvadasScreen`). */
private fun formatarAdicionada(dataIso: String?): String {
    if (dataIso == null) return ""
    return runCatching {
        val data = Instant.parse(dataIso)
        val dias = ChronoUnit.DAYS.between(data, Instant.now())
        when {
            dias <= 0 -> "hoje"
            dias == 1L -> "há 1 dia"
            dias < 30 -> "há $dias dias"
            else -> {
                val mes = data.atZone(ZoneOffset.UTC).month.getDisplayName(JavaTextStyle.FULL, LocalePt)
                "em $mes"
            }
        }
    }.getOrDefault("")
}
