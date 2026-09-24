package pt.aquavitae.android.feature.bebidadetalhe

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.model.OfertaCompra
import pt.aquavitae.android.data.model.atualizadoTexto
import pt.aquavitae.android.data.model.maisBarataDisponivel
import pt.aquavitae.android.data.model.temComparacao
import pt.aquavitae.android.data.model.temLink
import pt.aquavitae.android.data.model.ReviewResponse
import pt.aquavitae.android.data.model.ReviewsResponse
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.data.model.formatarPrecoPt
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.DialogFillScreen
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.iniciaisDe
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.WineDark
import pt.aquavitae.android.ui.util.abrirLink

private val EstiloSeccao = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
private val FormaFolha = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/**
 * O popup de detalhe de uma bebida (`android/design/caves/03-...png`, `05-...png`): abre-se ao tocar (curto ou longo,
 * ver [pt.aquavitae.android.ui.components.BebidaCard]) num cartão de bebida, em qualquer sítio da app — catálogo,
 * caves, favoritos, wishlist. Duas tabs: "Detalhes" (atributos, produtor) e "Reviews" (a tua + as da comunidade).
 * `onAdicionarACave` é chamado ao tocar no "+" — o popup de "Adicionar à cave" propriamente dito é outro ecrã.
 * `onVerProdutor` (fatia 6): tornar o cartão do produtor, no fim da tab "Detalhes", um atalho para a página dele — o
 * popup fecha-se e o ecrã que o abriu navega. `null` (o valor por omissão) tira o atalho: nas páginas do próprio
 * produtor não faz sentido ir "ver o produtor" onde já estamos.
 */
@Composable
fun BebidaDetalheSheet(
    bebidaId: Long,
    onDismiss: () -> Unit,
    onAdicionarACave: (BebidaDetail) -> Unit,
    onVerProdutor: ((Long) -> Unit)? = null,
    viewModel: BebidaDetalheViewModel = hiltViewModel(key = "bebida-detalhe-$bebidaId"),
) {
    LaunchedEffect(bebidaId) { viewModel.carregar(bebidaId) }
    val state by viewModel.state.collectAsState()
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        DialogScrim()
        DialogFillScreen()
        Box(
            Modifier.fillMaxSize().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } },
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
                when (val estado = state) {
                    BebidaDetalheUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }
                    is BebidaDetalheUiState.Error -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                    }
                    is BebidaDetalheUiState.Ready -> BebidaDetalheConteudo(estado, viewModel, onDismiss, onAdicionarACave, onVerProdutor)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.BebidaDetalheConteudo(
    state: BebidaDetalheUiState.Ready,
    viewModel: BebidaDetalheViewModel,
    onDismiss: () -> Unit,
    onAdicionarACave: (BebidaDetail) -> Unit,
    onVerProdutor: ((Long) -> Unit)?,
) {
    val bebida = state.bebida
    // Fecha o popup antes de navegar, senão ele ficaria por cima da página do produtor.
    val verProdutor: ((Long) -> Unit)? = onVerProdutor?.let { ver -> { id: Long -> onDismiss(); ver(id) } }
    // Comprar: abre a loja (o link de afiliado) no mesmo toque e regista o clique em segundo plano — nunca espera por ele.
    val context = LocalContext.current
    val comprar: (Long, String?) -> Unit = { linkId, url ->
        if (!url.isNullOrBlank() && context.abrirLink(url)) viewModel.registarClique(linkId)
    }
    Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk)
        }
    }
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        CabecalhoBebida(bebida)
        Spacer(Modifier.height(14.dp))
        LinhaAcoes(bebida, state, viewModel, onAdicionarACave, comprar)
        Spacer(Modifier.height(16.dp))
        TabsDetalhe(state, viewModel)
        Spacer(Modifier.height(16.dp))
        when (state.tab) {
            DetalheTab.DETALHES -> TabDetalhes(bebida, state.ofertas, comprar, verProdutor)
            DetalheTab.REVIEWS -> TabReviews(state, viewModel)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CabecalhoBebida(bebida: BebidaDetail) {
    val cabecalho = listOfNotNull(bebida.categoriaNome, bebida.vinhoDetalhe?.tipo).joinToString(" ").uppercase(LocalePt)
    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)).background(CardGray),
            contentAlignment = Alignment.Center,
        ) {
            val url = resolveImageUrl(bebida.imagePath)
            if (url != null) {
                AsyncImage(model = url, contentDescription = bebida.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text(text = "Imagem\nda bebida", style = AquaText.Footer.copy(color = MutedInk), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = listOfNotNull(cabecalho.ifBlank { null }, bebida.anoProducao?.toString()).joinToString(" • "), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
            Text(text = bebida.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 19.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            val produtorLinha = listOfNotNull(bebida.produtorNome, bebida.produtorResumo?.regiao).joinToString(" • ")
            if (produtorLinha.isNotEmpty()) Text(text = produtorLinha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp))
            Spacer(Modifier.height(6.dp))
            Text(text = String.format(LocalePt, "%.1f/5 • %d reviews", bebida.ratingMedio, bebida.totalReviews), style = AquaText.Label.copy(fontSize = 15.sp))
        }
    }
}

@Composable
private fun LinhaAcoes(
    bebida: BebidaDetail,
    state: BebidaDetalheUiState.Ready,
    viewModel: BebidaDetalheViewModel,
    onAdicionarACave: (BebidaDetail) -> Unit,
    onComprar: (Long, String?) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val link = bebida.linkCompra
        if (link != null) {
            // A oferta mais barata é o botão "Comprar" principal: o toque abre a loja e regista o clique.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Burgundy)
                    .clickable(enabled = !link.url.isNullOrBlank()) { onComprar(link.id, link.url) }
                    .padding(start = 16.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                link.preco?.let { Text(text = formatarPrecoPt(it), style = AquaText.Label.copy(color = Color.White, fontSize = 14.sp)) }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = link.retalhistaNome?.uppercase(LocalePt).orEmpty(),
                    style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Comprar", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        AcaoCircular(
            icone = if (state.isFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            descricao = "Favorito",
            selecionado = state.isFavorito,
            onClick = viewModel::alternarFavorito,
        )
        AcaoCircular(
            icone = if (state.isWishlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            descricao = "Wishlist",
            selecionado = state.isWishlist,
            onClick = viewModel::alternarWishlist,
        )
        AcaoCircular(icone = Icons.Filled.Add, descricao = "Adicionar à cave", selecionado = false, onClick = { onAdicionarACave(bebida) })
    }
}

@Composable
private fun AcaoCircular(icone: androidx.compose.ui.graphics.vector.ImageVector, descricao: String, selecionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(if (selecionado) Burgundy else Color.Transparent)
            .border(1.5.dp, if (selecionado) Burgundy else BurgundyTint, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = descricao, tint = if (selecionado) Color.White else Burgundy, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TabsDetalhe(state: BebidaDetalheUiState.Ready, viewModel: BebidaDetalheViewModel) {
    val totalReviews = (state.reviews as? LookupState.Ready)?.data?.reviews?.size ?: state.bebida.totalReviews
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        AbaTexto("Detalhes", state.tab == DetalheTab.DETALHES) { viewModel.selecionarTab(DetalheTab.DETALHES) }
        AbaTexto("Reviews · $totalReviews", state.tab == DetalheTab.REVIEWS) { viewModel.selecionarTab(DetalheTab.REVIEWS) }
    }
}

@Composable
private fun AbaTexto(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Text(text = texto, style = AquaText.Label.copy(fontSize = 15.sp, color = if (selecionado) Burgundy else MutedInk))
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(if (selecionado) 56.dp else 0.dp).height(2.dp).background(Burgundy))
    }
}

// --- Tab "Detalhes" ---

@Composable
private fun TabDetalhes(
    bebida: BebidaDetail,
    ofertas: LookupState<List<OfertaCompra>>,
    onComprar: (Long, String?) -> Unit,
    onVerProdutor: ((Long) -> Unit)?,
) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            CampoInfo("Teor alcoólico", bebida.teorAlcoolico?.let { String.format(LocalePt, "%.1f%% vol", it) }, Modifier.weight(1f))
            CampoInfo("Volume", bebida.volumeMl?.let { "${it.toInt()} ml" }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            CampoInfo("País de origem", bebida.paisOrigemNome, Modifier.weight(1f))
            CampoInfo("Ano de produção", bebida.anoProducao?.toString(), Modifier.weight(1f))
        }
        val vinho = bebida.vinhoDetalhe
        if (vinho != null) {
            Spacer(Modifier.height(20.dp))
            Text(text = "PERFIL SENSORIAL", style = EstiloSeccao)
            Spacer(Modifier.height(10.dp))
            vinho.nivelAcidez?.let { BarraNivel("Acidez", it) }
            vinho.nivelDocura?.let { Spacer(Modifier.height(10.dp)); BarraNivel("Doçura", it) }
            Spacer(Modifier.height(14.dp))
            val pilulas = listOfNotNull(
                vinho.corpo?.let { "CORPO $it" },
                vinho.tanino?.let { "TANINO $it" },
                vinho.tipo?.let { "VINHO ${it.uppercase(LocalePt)}" },
            )
            if (pilulas.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pilulas) { PillChip(text = it, selected = false, onClick = {}) }
                }
            }
            if (vinho.castas.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text(text = "CASTAS", style = EstiloSeccao)
                Spacer(Modifier.height(8.dp))
                vinho.castas.forEach { casta ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(text = casta.casta.orEmpty(), style = AquaText.Field.copy(fontSize = 14.sp), modifier = Modifier.weight(1f))
                        casta.percentagem?.let { Text(text = "${it.toInt()}%", style = AquaText.Footer.copy(color = MutedInk)) }
                    }
                }
            }
        }
        // "Onde comprar": só quando há mais do que uma oferta (com uma só, o botão do cabeçalho já é essa oferta).
        val listaOfertas = (ofertas as? LookupState.Ready)?.data.orEmpty()
        if (listaOfertas.size > 1) {
            Spacer(Modifier.height(20.dp))
            SeccaoOndeComprar(listaOfertas, onComprar)
        }
        val produtor = bebida.produtorResumo
        if (produtor != null) {
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WineDark)
                    .let { if (onVerProdutor != null) it.clickable { onVerProdutor(produtor.id) } else it }
                    .padding(18.dp),
            ) {
                Text(text = "PRODUTOR", style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp))
                Spacer(Modifier.height(4.dp))
                Text(text = produtor.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(color = Color.White, fontSize = 17.sp))
                Spacer(Modifier.height(2.dp))
                val linha = listOfNotNull(
                    produtor.regiao,
                    produtor.anoFundacao?.let { "fundada em $it" },
                    if (produtor.permiteVisitas) "recebe visitas" else null,
                ).joinToString(" · ")
                Text(text = linha, style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp))
                if (onVerProdutor != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(text = "VER PRODUTOR →", style = AquaText.Footer.copy(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * "Onde comprar": todas as ofertas dos retalhistas, a mais barata assinalada quando há mais do que uma disponível. Cada
 * oferta disponível tem o seu botão "COMPRAR" (abre a loja e regista o clique); as indisponíveis ficam esbatidas, com o
 * motivo ("Sem stock", "Página indisponível") e o último preço conhecido riscado, e sem botão.
 */
@Composable
private fun SeccaoOndeComprar(ofertas: List<OfertaCompra>, onComprar: (Long, String?) -> Unit) {
    val maisBarata = ofertas.maisBarataDisponivel()
    Column(Modifier.fillMaxWidth()) {
        Text(text = "ONDE COMPRAR", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ofertas.forEach { oferta ->
                val utilizavel = oferta.disponivel && oferta.temLink
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (oferta.disponivel) CardGray else Color.Transparent)
                        .border(1.dp, if (oferta.disponivel) Color.Transparent else BurgundyTint, RoundedCornerShape(12.dp))
                        .clickable(enabled = utilizavel) { onComprar(oferta.id, oferta.url) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        // O selo vai por cima do nome (ao lado, o preço e o botão não deixavam lugar e o nome ficava truncado).
                        if (oferta.id == maisBarata?.id && ofertas.temComparacao) {
                            Text(
                                text = "MAIS BARATO",
                                style = AquaText.Footer.copy(color = Burgundy, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(BurgundyTint).padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = oferta.retalhistaNome.orEmpty(),
                            style = AquaText.Label.copy(fontSize = 14.sp, color = if (oferta.disponivel) Burgundy else MutedInk),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val subtitulo = if (oferta.disponivel) oferta.atualizadoTexto() else oferta.motivoIndisponivel ?: "Indisponível"
                        subtitulo?.let { Text(text = it, style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp)) }
                    }
                    oferta.preco?.let {
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = formatarPrecoPt(it),
                            style = AquaText.Label.copy(fontSize = 15.sp, color = if (oferta.disponivel) Ink else MutedInk),
                            textDecoration = if (oferta.disponivel) null else TextDecoration.LineThrough,
                        )
                    }
                    if (utilizavel) {
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "COMPRAR",
                            style = AquaText.Footer.copy(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(Burgundy).padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoInfo(rotulo: String, valor: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(text = rotulo.uppercase(LocalePt), style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
        Text(text = valor ?: "—", style = AquaText.Field.copy(fontSize = 14.sp))
    }
}

@Composable
private fun BarraNivel(rotulo: String, nivel: Int, escala: Int = 5) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = rotulo, style = AquaText.Field.copy(fontSize = 13.sp), modifier = Modifier.width(64.dp))
        Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(BurgundyTint)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (nivel.toFloat() / escala).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(Burgundy),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text = "$nivel/$escala", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
    }
}

// --- Tab "Reviews" ---

@Composable
private fun TabReviews(state: BebidaDetalheUiState.Ready, viewModel: BebidaDetalheViewModel) {
    Column {
        MinhaReviewForm(state, viewModel)
        Spacer(Modifier.height(20.dp))
        when (val reviews = state.reviews) {
            LookupState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { LoadingDots(dotSize = 8.dp) }
            is LookupState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = reviews.message, style = AquaText.Error, textAlign = TextAlign.Center)
            }
            is LookupState.Ready -> ConteudoReviews(reviews.data, state.bebida.ratingMedio)
        }
    }
}

/**
 * Pedido do utilizador (2026-09-23): uma review só existe para quem já tem a bebida na lista de "já provadas" —
 * marcada com "Consumir" numa cave, ou adicionada diretamente na lista. Sem isso, mostra-se a explicação em vez do
 * formulário (não se marca nada sozinho a publicar, ao contrário do que este popup fazia antes).
 */
@Composable
private fun MinhaReviewForm(state: BebidaDetalheUiState.Ready, viewModel: BebidaDetalheViewModel) {
    if (state.bebida.isProvada != true) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BurgundyTint.copy(alpha = 0.3f)).padding(16.dp),
        ) {
            Text(text = "AINDA NÃO PROVASTE ESTA BEBIDA", style = EstiloSeccao)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Só se pode avaliar uma bebida já marcada como consumida. Marca-a com \"Consumir\" numa cave, ou " +
                    "adiciona-a diretamente à lista de já provadas (ecrã \"Já provadas\", nas Caves).",
                style = AquaText.Field.copy(fontSize = 13.sp),
            )
        }
        return
    }
    Column {
        Text(text = "A TUA REVIEW", style = EstiloSeccao)
        Spacer(Modifier.height(10.dp))
        Row {
            (1..5).forEach { estrela ->
                Icon(
                    imageVector = if (estrela <= state.minhaEstrelas) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$estrela estrelas",
                    tint = Burgundy,
                    modifier = Modifier.size(28.dp).clickable { viewModel.onMinhasEstrelas(estrela.toDouble()) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardGray).padding(12.dp),
        ) {
            BasicTextField(
                value = state.meuComentario,
                onValueChange = viewModel::onMeuComentario,
                textStyle = AquaText.Field.copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                decorationBox = { inner ->
                    if (state.meuComentario.isEmpty()) {
                        Text("O que achaste desta garrafa? Aroma, boca, com o que a bebeste...", style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk))
                    }
                    inner()
                },
            )
        }
        state.erroReview?.let {
            Spacer(Modifier.height(6.dp))
            Text(text = it, style = AquaText.Error)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = viewModel::publicarReview,
            enabled = state.minhaEstrelas > 0.0 && !state.aSubmeterReview,
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
            modifier = Modifier.align(Alignment.End).height(42.dp),
        ) {
            Text(if (state.aSubmeterReview) "A publicar…" else "Publicar", style = AquaText.Button.copy(fontSize = 14.sp))
        }
    }
}

@Composable
private fun ConteudoReviews(reviews: ReviewsResponse, ratingMedio: Double) {
    val total = reviews.reviews.size
    val maxContagem = (reviews.distribuicao.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Row(verticalAlignment = Alignment.CenterVertically) {
        // A média precisa (a mesma do cabeçalho, bebida.ratingMedio) — não recalculada a partir da distribuição, que
        // vem arredondada por estrela (1-5) e por isso perderia casas decimais (ex.: um 4,5 cai no balde "5").
        Text(text = String.format(LocalePt, "%.1f", ratingMedio), style = AquaText.BebidaNomeSerif.copy(fontSize = 34.sp), modifier = Modifier.width(70.dp))
        Column(Modifier.weight(1f)) {
            (5 downTo 1).forEach { estrela ->
                val contagem = reviews.distribuicao[estrela.toString()] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "$estrela", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp), modifier = Modifier.width(10.dp))
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(BurgundyTint)) {
                        Box(
                            Modifier.fillMaxHeight().fillMaxWidth(fraction = contagem.toFloat() / maxContagem).clip(RoundedCornerShape(3.dp)).background(Burgundy),
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(text = "$total reviews", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
    Spacer(Modifier.height(18.dp))
    Text(text = "DA COMUNIDADE", style = EstiloSeccao)
    Spacer(Modifier.height(10.dp))
    if (reviews.reviews.isEmpty()) {
        Text(text = "Ainda sem reviews desta bebida.", style = AquaText.SmallLink.copy(color = MutedInk))
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            reviews.reviews.forEach { ReviewRow(it) }
        }
    }
}

@Composable
private fun ReviewRow(review: ReviewResponse) {
    val nomeOuUsername = review.utilizadorNome ?: review.utilizadorUsername
    val partes = nomeOuUsername?.trim()?.split(" ")?.filter { it.isNotBlank() }.orEmpty()
    val iniciais = iniciaisDe(partes.getOrNull(0), partes.getOrNull(1), review.utilizadorUsername)
    val avatar = review.utilizadorAvatar?.let { Avatar(id = 0, nome = null, path = it, categoriaId = null, categoriaNome = null) }

    Row(Modifier.fillMaxWidth()) {
        AvatarBadge(avatar = avatar, iniciais = iniciais, size = 34.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth()) {
                Text(text = nomeOuUsername.orEmpty(), style = AquaText.Label.copy(fontSize = 13.sp), modifier = Modifier.weight(1f))
                review.rating?.let { Text(text = String.format(LocalePt, "%.1f", it), style = AquaText.Label.copy(fontSize = 13.sp)) }
            }
            Text(text = formatarDataRelativa(review.createdAt), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
            review.comment?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = it, style = AquaText.Field.copy(fontSize = 13.sp))
            }
        }
    }
}

/** "há N dias/semanas", "em MÊS" ou o ano — aproximação simples a partir do ISO instant, sem biblioteca extra. */
private fun formatarDataRelativa(isoInstant: String?): String {
    if (isoInstant == null) return ""
    return runCatching {
        val instant = java.time.Instant.parse(isoInstant)
        val dias = java.time.Duration.between(instant, java.time.Instant.now()).toDays()
        when {
            dias < 1 -> "hoje"
            dias == 1L -> "há 1 dia"
            dias < 7 -> "há $dias dias"
            dias < 30 -> "há ${dias / 7} semana${if (dias / 7 > 1) "s" else ""}"
            else -> {
                val data = instant.atZone(java.time.ZoneOffset.UTC)
                val mes = data.month.getDisplayName(java.time.format.TextStyle.FULL, LocalePt)
                if (data.year == java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).year) "em $mes" else "em $mes de ${data.year}"
            }
        }
    }.getOrDefault("")
}
