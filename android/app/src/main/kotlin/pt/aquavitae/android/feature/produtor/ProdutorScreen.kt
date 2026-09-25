package pt.aquavitae.android.feature.produtor

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.model.coordenadasTexto
import pt.aquavitae.android.data.model.formatarPrecoPt
import pt.aquavitae.android.data.model.garrafasTexto
import pt.aquavitae.android.data.model.geoUri
import pt.aquavitae.android.data.model.linhaAtributos
import pt.aquavitae.android.data.model.mapaWebUrl
import pt.aquavitae.android.data.model.moradaParaMostrar
import pt.aquavitae.android.data.model.temLocalizacao
import pt.aquavitae.android.data.model.websiteParaMostrar
import pt.aquavitae.android.data.model.websiteUrl
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder
import pt.aquavitae.android.ui.util.abrirLink

private val AlturaImagem = 210.dp
private val SobreposicaoConteudo = 28.dp

/** Fundo do cabeçalho quando o produtor não tem imagem (o cor-de-rosa suave do mockup). */
private val FundoSemImagem = Brush.verticalGradient(listOf(Color(0xFFE3CDC9), Color(0xFFF3E8E6)))

/**
 * A página de um produtor (fatia 6, `android/design/produtor/01-produtor.png`): imagem, nome, localização, rating geral
 * das suas bebidas, pílulas (visitas, site, nº de garrafas), história (o popup abre em "Ler a história completa"),
 * a localização (morada em texto; "Abrir no mapa" abre a app de mapas) e as primeiras garrafas do catálogo — 3 de início,
 * "Carregar mais" até 6; a seta do título
 * leva ao catálogo só desse produtor. As garrafas abrem o popup de detalhe (toque curto ou premido, como em todo o lado).
 */
@Composable
fun ProdutorScreen(
    onVoltar: () -> Unit,
    onVerCatalogo: (Long) -> Unit,
    viewModel: ProdutorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }

    Box(Modifier.fillMaxSize().background(Paper)) {
        when (val estado = state) {
            ProdutorUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is ProdutorUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is ProdutorUiState.Ready -> {
                ProdutorConteudo(estado, viewModel, onVerCatalogo, onBebidaClick = { bebidaSelecionadaId = it })
                if (estado.historiaAberta) {
                    HistoriaProdutorSheet(nome = estado.produtor.nome.orEmpty(), historia = estado.produtor.historia.orEmpty(), onDismiss = viewModel::fecharHistoria)
                }
            }
        }

        // Por cima de tudo (e do scroll): o botão de voltar continua à mão mesmo a meio da página.
        BotaoVoltar(onVoltar, Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 16.dp, top = 8.dp))

        bebidaSelecionadaId?.let { id ->
            // Aqui o "VER PRODUTOR" do popup não faz sentido (já estamos na página dele): sem `onVerProdutor`.
            BebidaDetalheSheet(
                bebidaId = id,
                onDismiss = {
                    bebidaSelecionadaId = null
                    viewModel.atualizar()
                },
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
}

@Composable
private fun BotaoVoltar(onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(40.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onVoltar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Ink, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProdutorConteudo(
    state: ProdutorUiState.Ready,
    viewModel: ProdutorViewModel,
    onVerCatalogo: (Long) -> Unit,
    onBebidaClick: (Long) -> Unit,
) {
    val produtor = state.produtor
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            ImagemProdutor(produtor, Modifier.fillMaxWidth().height(AlturaImagem))
            Column(
                modifier = Modifier
                    .padding(top = AlturaImagem - SobreposicaoConteudo)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            ) {
                Cabecalho(produtor)
                Spacer(Modifier.height(16.dp))
                Pilulas(produtor)
                produtor.historia?.takeIf { it.isNotBlank() }?.let { historia ->
                    Spacer(Modifier.height(20.dp))
                    Historia(historia, onLerCompleta = viewModel::abrirHistoria)
                }
                if (produtor.temLocalizacao) {
                    Spacer(Modifier.height(20.dp))
                    LocalizacaoCard(produtor)
                }
                Spacer(Modifier.height(28.dp))
                GarrafasEmCatalogo(state, viewModel, onVerCatalogo, onBebidaClick)
            }
        }
    }
}

@Composable
private fun ImagemProdutor(produtor: ProdutorDetail, modifier: Modifier = Modifier) {
    val url = resolveImageUrl(produtor.imagePath)
    Box(modifier.background(FundoSemImagem), contentAlignment = Alignment.Center) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = produtor.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(text = "Imagem do produtor", style = AquaText.SectionSerif.copy(color = Color.White, fontSize = 16.sp))
        }
    }
}

@Composable
private fun Cabecalho(produtor: ProdutorDetail) {
    Text(text = "PRODUTOR", style = AquaText.Footer.copy(fontSize = 10.sp))
    Spacer(Modifier.height(6.dp))
    Text(text = produtor.nome.orEmpty(), style = AquaText.SectionSerif.copy(fontSize = 34.sp, lineHeight = 38.sp))
    val localizacao = listOfNotNull(
        produtor.regiao,
        produtor.paisNome,
    ).joinToString(", ").ifBlank { null }
    val linha = listOfNotNull(localizacao, produtor.anoFundacao?.let { "fundada em $it" }).joinToString(" · ")
    if (linha.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(text = linha, style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk))
    }
    Spacer(Modifier.height(10.dp))
    RatingGeral(produtor)
}

/** O rating geral do produtor (a sugestão do utilizador): a média de todas as reviews das suas bebidas. */
@Composable
private fun RatingGeral(produtor: ProdutorDetail) {
    val rating = produtor.ratingMedio
    if (rating == null || produtor.totalReviews == 0) {
        Text(text = "Sem reviews ainda", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        return
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = FontFamily.Serif, fontSize = 26.sp, color = Ink)) { append(String.format(LocalePt, "%.1f", rating)) }
                withStyle(SpanStyle(fontSize = 13.sp, color = MutedInk)) { append(" /5") }
            },
            style = AquaText.Field,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "${produtor.totalReviews} ${if (produtor.totalReviews == 1) "REVIEW" else "REVIEWS"} · RATING GERAL",
            style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Pilulas(produtor: ProdutorDetail) {
    val context = LocalContext.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (produtor.permiteVisitas) Pilula("RECEBE VISITAS")
        produtor.websiteParaMostrar()?.let { site ->
            Pilula(site.uppercase(LocalePt), onClick = produtor.websiteUrl()?.let { url -> { context.abrirLink(url) } })
        }
        Pilula(garrafasTexto(produtor.totalProdutos).uppercase(LocalePt))
    }
}

@Composable
private fun Pilula(texto: String, onClick: (() -> Unit)? = null) {
    val forma = RoundedCornerShape(6.dp)
    Text(
        text = texto,
        style = AquaText.Footer.copy(fontSize = 10.sp),
        modifier = Modifier
            .clip(forma)
            .border(1.dp, RoseBorder, forma)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
}

@Composable
private fun Historia(historia: String, onLerCompleta: () -> Unit) {
    var cortada by remember(historia) { mutableStateOf(false) }
    Text(
        text = historia,
        style = AquaText.Field.copy(fontSize = 15.sp, lineHeight = 22.sp),
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { cortada = it.hasVisualOverflow },
    )
    // Se a história cabe toda nas 4 linhas, não há mais nada para ler no popup.
    if (cortada) {
        LinkText(
            text = "LER A HISTÓRIA COMPLETA",
            onClick = onLerCompleta,
            style = AquaText.Footer.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Start,
        )
    }
}

/**
 * A localização: a morada em texto (ou, sem morada, as coordenadas) e "Abrir no mapa", que abre a app de mapas do
 * telemóvel no ponto do produtor. Sem mapa embebido (o Maps SDK pede um projeto Google Cloud com faturação e chave) —
 * decisão do utilizador em 2026-09-24. O cartão inteiro é tocável.
 */
@Composable
private fun LocalizacaoCard(produtor: ProdutorDetail) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .clickable { context.abrirMapa(produtor) },
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Burgundy, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = "LOCALIZAÇÃO", style = AquaText.Footer.copy(fontSize = 10.sp))
                Spacer(Modifier.height(4.dp))
                // Sem morada, as coordenadas fazem de texto (o cartão só existe se há uma das duas).
                Text(
                    text = produtor.moradaParaMostrar ?: produtor.coordenadasTexto().orEmpty(),
                    style = AquaText.Field.copy(fontSize = 15.sp, lineHeight = 22.sp),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoseBorder))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Abrir no mapa", style = AquaText.Label.copy(fontSize = 13.sp), modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Burgundy)
        }
    }
}

@Composable
private fun GarrafasEmCatalogo(
    state: ProdutorUiState.Ready,
    viewModel: ProdutorViewModel,
    onVerCatalogo: (Long) -> Unit,
    onBebidaClick: (Long) -> Unit,
) {
    val produtor = state.produtor
    // A linha toda é a seta do pedido do utilizador — um alvo de toque maior que só o ícone.
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onVerCatalogo(produtor.id) }.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Garrafas em catálogo", style = AquaText.SectionSerif, modifier = Modifier.weight(1f))
        Text(text = produtor.totalProdutos.toString(), style = AquaText.Footer.copy(color = Burgundy, fontSize = 12.sp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver o catálogo do produtor", tint = Burgundy)
    }

    if (state.visiveis.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoseBorder))
        Text(
            text = "Ainda sem garrafas no catálogo.",
            style = AquaText.SmallLink.copy(color = MutedInk),
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
    state.visiveis.forEach { bebida -> GarrafaRow(bebida, onClick = { onBebidaClick(bebida.id) }) }
    if (state.visiveis.isNotEmpty()) Box(Modifier.fillMaxWidth().height(1.dp).background(RoseBorder))

    if (state.maisParaCarregar > 0) {
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            LinkText(
                text = "CARREGAR MAIS ${state.maisParaCarregar}",
                onClick = viewModel::expandir,
                style = AquaText.Footer.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            )
        }
    } else if (state.expandido && state.temMaisNoCatalogo) {
        // Depois das 6 o resto só se vê no catálogo do produtor (a seta de cima): esta ligação só o torna óbvio.
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            LinkText(
                text = "VER AS ${produtor.totalProdutos} GARRAFAS",
                onClick = { onVerCatalogo(produtor.id) },
                style = AquaText.Footer.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

/** Uma garrafa da lista do produtor (linhas planas com separador, como no mockup — não o cartão cinzento do catálogo). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GarrafaRow(bebida: BebidaSummary, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoseBorder))
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            Box(Modifier.size(width = 52.dp, height = 68.dp).clip(RoundedCornerShape(6.dp)).background(CardGray)) {
                val url = resolveImageUrl(bebida.imagePath)
                if (url != null) AsyncImage(model = url, contentDescription = bebida.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(text = bebida.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 16.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(text = bebida.linhaAtributos(), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                RatingDaGarrafa(bebida)
            }
            bebida.precoDesde?.let {
                Spacer(Modifier.width(10.dp))
                Text(text = formatarPrecoPt(it), style = AquaText.Label.copy(fontSize = 14.sp, color = Ink))
            }
        }
    }
}

@Composable
private fun RatingDaGarrafa(bebida: BebidaSummary) {
    if (bebida.totalReviews == 0) {
        Text(text = "SEM REVIEWS", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        return
    }
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = FontFamily.Serif, fontSize = 20.sp, color = Ink)) { append(String.format(LocalePt, "%.1f", bebida.ratingMedio)) }
            withStyle(SpanStyle(fontSize = 10.sp, color = MutedInk, letterSpacing = 0.3.sp)) {
                append(" /5 · ${bebida.totalReviews} ${if (bebida.totalReviews == 1) "REVIEW" else "REVIEWS"}")
            }
        },
        style = AquaText.Field,
    )
}

// --- A app de mapas (intent do sistema; abrir URLs é o `abrirLink` partilhado de `ui/util`) ---

/** Abre a app de mapas no ponto do produtor; se não houver nenhuma, cai para o Google Maps no browser. */
private fun Context.abrirMapa(produtor: ProdutorDetail) {
    val geo = produtor.geoUri() ?: return
    if (!abrirLink(geo)) produtor.mapaWebUrl()?.let { abrirLink(it) }
}
