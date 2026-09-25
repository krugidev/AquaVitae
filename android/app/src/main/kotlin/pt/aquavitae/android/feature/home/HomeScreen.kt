package pt.aquavitae.android.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.R
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.descricaoJanela
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.model.formatarPrecoPt
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.BebidaCard
import pt.aquavitae.android.ui.components.BottomNavContentPadding
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.iniciaisDe
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder
import pt.aquavitae.android.ui.theme.WineDark
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * A homepage: saudação com o dia, pesquisa, "Escolhido para ti", "As minhas Caves", estatísticas do perfil e o produtor
 * em destaque da semana. Ligada à API real (ver [HomeViewModel]). O logótipo é a imagem entregue pelo utilizador
 * (`ic_wordmark_home`, fundo transparente) — os outros ecrãs de auth ainda usam o texto Inter aproximado.
 */
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onVerCaves: () -> Unit,
    onVerProdutor: (Long) -> Unit,
    onVerSugestoes: () -> Unit,
    onVerPerfil: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    // Voltar do ecrã de perfil (nome/avatar podem ter mudado) desmonta e remonta este Composable — recarrega tudo,
    // o mesmo padrão do FavoritosScreen/WishlistScreen (ver CLAUDE.md). Um pouco mais pesado do que só atualizar o
    // avatar, mas simples e consistente com o resto da app.
    LaunchedEffect(Unit) { viewModel.carregar() }
    // O detalhe de uma bebida é sempre um popup por cima do ecrã (nunca uma rota) — ver BebidaDetalheSheet.
    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is HomeUiState.Ready -> HomeContent(
                state = estado,
                onSearchClick = onSearchClick,
                onBebidaClick = { id -> bebidaSelecionadaId = id },
                onCategoriaClick = viewModel::selecionarCategoria,
                onCaveClick = viewModel::selecionarCave,
                onVerCaves = onVerCaves,
                onVerProdutor = onVerProdutor,
                onVerSugestoes = onVerSugestoes,
                onVerPerfil = onVerPerfil,
            )
        }
    }

    bebidaSelecionadaId?.let { id ->
        BebidaDetalheSheet(
            bebidaId = id,
            onDismiss = { bebidaSelecionadaId = null },
            onAdicionarACave = { bebidaParaAdicionarACave = it },
            onVerProdutor = onVerProdutor,
        )
    }
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

@Composable
private fun HomeContent(
    state: HomeUiState.Ready,
    onSearchClick: () -> Unit,
    onBebidaClick: (Long) -> Unit,
    onCategoriaClick: (Long) -> Unit,
    onCaveClick: (Long) -> Unit,
    onVerCaves: () -> Unit,
    onVerProdutor: (Long) -> Unit,
    onVerSugestoes: () -> Unit,
    onVerPerfil: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 18.dp,
            bottom = BottomNavContentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { HeaderRow(state, onVerPerfil) }
        item { SearchBar(onSearchClick) }
        item {
            EscolhidoParaTiSection(
                categorias = state.categorias,
                categoriaSelecionadaId = state.categoriaSelecionadaId,
                sugestoes = state.sugestoes,
                loading = state.sugestoesLoading,
                onCategoriaClick = onCategoriaClick,
                onBebidaClick = onBebidaClick,
                onVerSugestoes = onVerSugestoes,
            )
        }
        item {
            AsMinhasCavesSection(
                caves = state.caves,
                caveSelecionada = state.caveSelecionada,
                garrafas = state.garrafasCaveSelecionada,
                onCaveClick = onCaveClick,
                onVerCaves = onVerCaves,
            )
        }
        item { EstatisticasSection(state.utilizador.totalProvadas, state.utilizador.totalWishlist, state.utilizador.totalFavoritos) }
        state.produtorDestaque?.let { produtor -> item { ProdutorDestaqueSection(produtor, onVerProdutor) } }
    }
}

// --- Cabeçalho: logótipo, dia da semana, avatar ---

@Composable
private fun HeaderRow(state: HomeUiState.Ready, onVerPerfil: () -> Unit) {
    val hoje = remember(state) { LocalDate.now() }
    val diaSemana = hoje.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale("pt", "PT"))
    val mes = hoje.month.getDisplayName(JavaTextStyle.FULL, Locale("pt", "PT"))
    val dataFormatada = "$diaSemana, ${hoje.dayOfMonth} de $mes".uppercase()

    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_wordmark_home),
                contentDescription = "AquaVitae",
                modifier = Modifier.height(22.dp),
            )
            Spacer(Modifier.weight(1f))
            AvatarBadge(
                avatar = state.utilizador.avatar,
                iniciais = iniciaisDe(state.utilizador.firstName, state.utilizador.lastName, state.utilizador.username),
                onClick = onVerPerfil,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(text = dataFormatada, style = AquaText.Footer.copy(color = MutedInk))
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append("O que queres ")
                withStyle(AquaText.GreetingSerif.copy(color = Burgundy).toSpanStyle()) { append("provar") }
                append(" hoje?")
            },
            style = AquaText.GreetingSerif,
        )
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .border(1.5.dp, Burgundy, shape)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Pesquisar bebida, produtor, casta...",
            style = AquaText.Field.copy(color = MutedInk, fontSize = 14.sp),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(Burgundy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Pesquisar", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// --- Escolhido para ti ---

@Composable
private fun EscolhidoParaTiSection(
    categorias: List<LookupItem>,
    categoriaSelecionadaId: Long?,
    sugestoes: List<BebidaSummary>,
    loading: Boolean,
    onCategoriaClick: (Long) -> Unit,
    onBebidaClick: (Long) -> Unit,
    onVerSugestoes: () -> Unit,
) {
    Column {
        SectionHeader(prefixo = "Escolhido para ", destaque = "ti", link = "Ver mais sugestões", onLinkClick = onVerSugestoes)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categorias, key = { it.id }) { categoria ->
                PillChip(
                    text = categoria.nome.orEmpty(),
                    selected = categoria.id == categoriaSelecionadaId,
                    onClick = { onCategoriaClick(categoria.id) },
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        if (loading) {
            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { LoadingDots(dotSize = 8.dp) }
        } else if (sugestoes.isEmpty()) {
            Text(text = "Sem sugestões nesta categoria.", style = AquaText.SmallLink.copy(color = MutedInk))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sugestoes.forEach { bebida -> BebidaCard(bebida, onClick = { onBebidaClick(bebida.id) }) }
            }
        }
    }
}

// BebidaCard mudou-se para ui/components/BebidaCard.kt (2026-09-23): o catálogo (fatia 2b) precisa do mesmo cartão.

// --- As minhas Caves ---

@Composable
private fun AsMinhasCavesSection(
    caves: List<CaveResponse>,
    caveSelecionada: CaveResponse?,
    garrafas: List<CaveBebidaResponse>,
    onCaveClick: (Long) -> Unit,
    onVerCaves: () -> Unit,
) {
    Column {
        SectionHeader(prefixo = "As minhas ", destaque = "Caves", link = "Todas as caves", onLinkClick = onVerCaves)
        Spacer(Modifier.height(12.dp))
        if (caves.isEmpty()) {
            Text(text = "Ainda não tens nenhuma cave.", style = AquaText.SmallLink.copy(color = MutedInk))
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(caves, key = { it.id }) { cave ->
                    PillChip(text = cave.nome.orEmpty(), selected = cave.id == caveSelecionada?.id, onClick = { onCaveClick(cave.id) })
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(Burgundy).clickable(onClick = onVerCaves),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar garrafa", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        if (garrafas.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Column {
                garrafas.take(3).forEachIndexed { indice, garrafa ->
                    HorizontalDividerFino()
                    CaveBebidaRow(garrafa)
                    if (indice == garrafas.take(3).lastIndex) HorizontalDividerFino()
                }
            }
            Spacer(Modifier.height(6.dp))
            caveSelecionada?.nome?.let { nome ->
                LinkText(text = "VER MAIS DE '${nome.uppercase()}'", onClick = onVerCaves, style = AquaText.SmallLink, textAlign = TextAlign.Start)
            }
        }
    }
}

@Composable
private fun HorizontalDividerFino() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(RoseBorder))
}

@Composable
private fun CaveBebidaRow(garrafa: CaveBebidaResponse) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = garrafa.quantidade.toString(),
            style = AquaText.BebidaNomeSerif.copy(fontSize = 26.sp, color = Burgundy),
            modifier = Modifier.width(34.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(text = garrafa.bebidaNome.orEmpty(), style = AquaText.BebidaNomeSerif)
            Text(text = garrafa.descricaoJanela(), style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        }
        Column(horizontalAlignment = Alignment.End) {
            garrafa.precoPago?.let { Text(text = formatarPrecoPt(it), style = AquaText.Label) }
            Text(text = "/UN", style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
        }
    }
}

// --- Estatísticas ---

@Composable
private fun EstatisticasSection(provadas: Int, wishlist: Int, favoritos: Int) {
    Column {
        Text(text = "Estatísticas", style = AquaText.SectionSerif)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox("PROVADAS", provadas, Modifier.weight(1f))
            StatBox("WISHLIST", wishlist, Modifier.weight(1f))
            StatBox("FAVORITOS", favoritos, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(label: String, valor: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier.clip(shape).border(1.dp, RoseBorder, shape).padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = valor.toString(), style = AquaText.BebidaNomeSerif.copy(fontSize = 24.sp, color = Burgundy))
        Text(text = label, style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
    }
}

// --- Produtor em destaque ---

@Composable
private fun ProdutorDestaqueSection(produtor: ProdutorDetail, onVerProdutor: (Long) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        // O cartão todo abre a página do produtor (além do botão "VER PRODUTOR" lá dentro).
        modifier = Modifier.fillMaxWidth().clip(shape).background(WineDark).clickable(onClick = { onVerProdutor(produtor.id) }).padding(20.dp),
    ) {
        Text(
            text = "PRODUTOR EM DESTAQUE",
            style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp),
        )
        Spacer(Modifier.height(6.dp))
        Text(text = produtor.nome.orEmpty(), style = AquaText.BebidaNomeSerif.copy(color = Color.White, fontSize = 22.sp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = listOfNotNull(
                produtor.regiao ?: produtor.paisNome,
                produtor.anoFundacao?.let { "fundada em $it" },
                "recebe visitas".takeIf { produtor.permiteVisitas },
            ).joinToString(" • "),
            style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp),
        )
        produtor.historia?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it,
                style = AquaText.Field.copy(color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp, lineHeight = 18.sp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val outlineShape = RoundedCornerShape(50)
            Text(
                text = "VER PRODUTOR",
                style = AquaText.Link.copy(color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                modifier = Modifier
                    .clip(outlineShape)
                    .border(1.5.dp, Color.White, outlineShape)
                    .clickable(onClick = { onVerProdutor(produtor.id) })
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${produtor.totalProdutos} garrafas no catálogo",
                style = AquaText.Footer.copy(color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp),
            )
        }
    }
}

// --- Partilhado ---

@Composable
private fun SectionHeader(prefixo: String, destaque: String, link: String?, onLinkClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = buildAnnotatedString {
                append(prefixo)
                withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append(destaque) }
            },
            style = AquaText.SectionSerif,
            modifier = Modifier.weight(1f),
        )
        if (link != null) {
            Text(
                text = link,
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
                modifier = Modifier.clickable(onClick = onLinkClick),
            )
        }
    }
}
