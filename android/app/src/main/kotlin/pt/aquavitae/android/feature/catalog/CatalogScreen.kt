package pt.aquavitae.android.feature.catalog

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.feature.bebidadetalhe.BebidaDetalheSheet
import pt.aquavitae.android.feature.cave.AdicionarACaveSheet
import pt.aquavitae.android.data.model.CatalogFiltro
import pt.aquavitae.android.ui.components.BebidaCard
import pt.aquavitae.android.ui.components.BottomNavContentPadding
import pt.aquavitae.android.ui.components.ContagemEOrdenacao
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder

/**
 * Ecrã "Catálogo" (fatia 2b, `android/design/catalogo/`): pesquisa + filtros completos ligados a `GET /api/bebidas`.
 * Chegou-se aqui pela pesquisa/"Ver mais sugestões" da homepage ou pelo separador "Catálogo" da barra inferior — nos
 * dois casos mostra o catálogo todo (sem pré-filtro; um pré-filtro por categoria fica para quando fizer falta).
 *
 * **O mesmo ecrã serve o catálogo de um só produtor** (fatia 6, rota `produtor/{id}/catalogo`): a ViewModel lê o id do
 * argumento de navegação e o ecrã muda de cara — seta de voltar e título do produtor, categorias "Todas" + as dele, sem
 * barra de navegação por baixo, e sem país/região no popup (ver [FiltrosSheet]). Quem o chama nesse modo passa `onVoltar`.
 * `onVerProdutor = null` tira o atalho "VER PRODUTOR" do popup de detalhe (não faz sentido no catálogo do próprio produtor).
 */
@Composable
fun CatalogScreen(
    onVerProdutor: ((Long) -> Unit)? = null,
    onVoltar: (() -> Unit)? = null,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var bebidaSelecionadaId by remember { mutableStateOf<Long?>(null) }
    var bebidaParaAdicionarACave by remember { mutableStateOf<BebidaDetail?>(null) }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            CatalogUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is CatalogUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
                if (onVoltar != null) LinkText(text = "VOLTAR", onClick = onVoltar)
            }

            is CatalogUiState.Ready -> {
                CatalogContent(state = estado, viewModel = viewModel, onVoltar = onVoltar, onBebidaClick = { id -> bebidaSelecionadaId = id })
                if (estado.filtrosAbertos) {
                    FiltrosSheet(state = estado, viewModel = viewModel, onDismiss = viewModel::fecharFiltrosSemAplicar)
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
                        onGuardado = { bebidaParaAdicionarACave = null },
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogContent(state: CatalogUiState.Ready, viewModel: CatalogViewModel, onVoltar: (() -> Unit)?, onBebidaClick: (Long) -> Unit) {
    var busca by remember { mutableStateOf(state.filtro.search.orEmpty()) }
    val produtor = state.produtor

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Só as abas têm a barra de navegação por baixo; o catálogo de um produtor é uma rota à parte, sem ela.
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = if (produtor != null) 8.dp else 18.dp,
            bottom = if (produtor != null) 24.dp else BottomNavContentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onVoltar != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Burgundy) }
                }
            }
        }
        item {
            if (produtor != null) {
                Column {
                    Text(text = "CATÁLOGO DO PRODUTOR", style = AquaText.Footer.copy(fontSize = 10.sp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Garrafas de ")
                            withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append(produtor.nome.orEmpty()) }
                        },
                        style = AquaText.SectionSerif,
                    )
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        append("Explora o ")
                        withStyle(AquaText.SectionSerifAccent.toSpanStyle()) { append("catálogo") }
                        append(" todo")
                    },
                    style = AquaText.SectionSerif,
                )
            }
        }
        item {
            SearchBarComFiltro(
                valor = busca,
                onValorChange = { busca = it },
                onPesquisar = { viewModel.pesquisar(busca) },
                onFiltroClick = viewModel::abrirFiltros,
                dica = if (produtor != null) "Pesquisar bebida ou casta..." else "Pesquisar bebida, produtor, casta...",
            )
        }
        // No catálogo de um produtor as categorias são só as dele, com "Todas" à cabeça; com uma só, não há nada a escolher.
        if (produtor == null || state.categorias.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (produtor != null) {
                        item { PillChip(text = "Todas", selected = state.filtro.categoriaId == null, onClick = { viewModel.selecionarCategoriaAtiva(null) }) }
                    }
                    items(state.categorias, key = { it.id }) { categoria ->
                        PillChip(
                            text = categoria.nome.orEmpty(),
                            selected = categoria.id == state.filtro.categoriaId,
                            onClick = { viewModel.selecionarCategoriaAtiva(categoria.id) },
                        )
                    }
                }
            }
        }
        item {
            ContagemEOrdenacao(
                contagem = "${state.totalElements} ${if (state.totalElements == 1L) "BEBIDA" else "BEBIDAS"}",
                atual = state.sort,
                opcoes = CatalogSort.entries,
                rotulo = { it.label },
                onSelecionar = viewModel::selecionarSort,
            )
        }
        if (state.filtro.totalAtivos > 0) {
            item { PilulasFiltrosAtivos(state, viewModel) }
        }
        if (!state.carregandoMais && state.resultados.isEmpty()) {
            item { Text(text = "Sem bebidas com estes filtros.", style = AquaText.SmallLink.copy(color = MutedInk), modifier = Modifier.padding(vertical = 24.dp)) }
        }
        items(state.resultados, key = { it.id }) { bebida -> BebidaCard(bebida, onClick = { onBebidaClick(bebida.id) }) }
        if (state.carregandoMais) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) { LoadingDots(dotSize = 8.dp) } }
        } else if (state.temMais) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    LinkText(text = "Carregar mais ${state.totalElements - state.resultados.size} bebidas", onClick = viewModel::carregarMais)
                }
            }
        }
    }
}

@Composable
private fun SearchBarComFiltro(valor: String, onValorChange: (String) -> Unit, onPesquisar: () -> Unit, onFiltroClick: () -> Unit, dica: String) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(shape).background(Color.White).border(1.dp, Burgundy, shape).padding(start = 20.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (valor.isEmpty()) Text(text = dica, style = AquaText.Field.copy(fontSize = 14.sp, color = MutedInk))
            BasicTextField(
                value = valor,
                onValueChange = onValorChange,
                textStyle = AquaText.Field.copy(fontSize = 14.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onPesquisar() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Burgundy).clickable(onClick = onFiltroClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Filled.FilterList, contentDescription = "Filtros", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PilulasFiltrosAtivos(state: CatalogUiState.Ready, viewModel: CatalogViewModel) {
    val nomesRegiao = (state.regioes as? pt.aquavitae.android.data.model.LookupState.Ready)?.data.orEmpty()
    val nomesPais = (state.paises as? pt.aquavitae.android.data.model.LookupState.Ready)?.data.orEmpty()
    val nomesCorpo = (state.corpos as? pt.aquavitae.android.data.model.LookupState.Ready)?.data.orEmpty()
    val nomesTipo = (state.tipos as? pt.aquavitae.android.data.model.LookupState.Ready)?.data.orEmpty()

    val pilulas = buildList {
        state.filtro.regiaoIds.forEach { id ->
            nomesRegiao.firstOrNull { it.id == id }?.nome?.let { add(it to { f: CatalogFiltro -> f.copy(regiaoIds = f.regiaoIds - id) }) }
        }
        if (state.filtro.paisId != null && state.filtro.paisId != CatalogFiltro.PAIS_PORTUGAL_ID) {
            nomesPais.firstOrNull { it.id == state.filtro.paisId }?.nome?.let { add(it to { f: CatalogFiltro -> f.copy(paisId = null) }) }
        }
        if (state.filtro.precoMin != null || state.filtro.precoMax != null) {
            val max = state.filtro.precoMax
            add((if (max != null) "Até ${max.toInt()}€" else "Preço") to { f: CatalogFiltro -> f.copy(precoMin = null, precoMax = null) })
        }
        state.filtro.ratingMin?.let { rating -> add("Rating $rating+" to { f: CatalogFiltro -> f.copy(ratingMin = null) }) }
        state.filtro.corpoId?.let { id -> nomesCorpo.firstOrNull { it.id == id }?.nome?.let { add(it to { f: CatalogFiltro -> f.copy(corpoId = null) }) } }
        state.filtro.tipoId?.let { id -> nomesTipo.firstOrNull { it.id == id }?.nome?.let { add(it to { f: CatalogFiltro -> f.copy(tipoId = null) }) } }
        if (state.filtro.castaIds.isNotEmpty()) {
            add("${state.filtro.castaIds.size} castas" to { f: CatalogFiltro -> f.copy(castaIds = emptySet()) })
        }
    }
    if (pilulas.isEmpty()) return

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(pilulas.size) { indice ->
            val (texto, remover) = pilulas[indice]
            Row(
                modifier = Modifier.height(30.dp).clip(RoundedCornerShape(50)).border(1.dp, RoseBorder, RoundedCornerShape(50)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = texto, style = AquaText.Footer.copy(color = Burgundy, fontSize = 10.sp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "×",
                    style = AquaText.Footer.copy(color = Burgundy, fontSize = 12.sp),
                    modifier = Modifier.clickable { viewModel.removerFiltro(remover) },
                )
            }
        }
    }
}
