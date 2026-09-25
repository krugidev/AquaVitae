package pt.aquavitae.android.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.components.LevelSlider
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LookupContent
import pt.aquavitae.android.ui.components.NavRow
import pt.aquavitae.android.ui.components.NextButton
import pt.aquavitae.android.ui.components.OptionRow
import pt.aquavitae.android.ui.components.PillCard
import pt.aquavitae.android.ui.components.verticalScrollbar
import pt.aquavitae.android.ui.theme.AquaText

private const val TITULO_PREFERENCIAS = "COMPLETA O PERFIL"

/** Ecrã 10 — "QUE TIPOS DE BEBIDA PREFERES?": escolha múltipla (as categorias vêm da API). */
@Composable
fun TypesStep(state: OnboardingUiState, viewModel: OnboardingViewModel, modifier: Modifier = Modifier) {
    PillCard(title = TITULO_PREFERENCIAS, fillHeight = true, modifier = modifier) {
        Question("QUE TIPOS DE BEBIDA PREFERES?")
        Spacer(Modifier.height(14.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LookupContent(state.categorias, onRetry = viewModel::carregarLookups) { categorias ->
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categorias.forEach { categoria ->
                        OptionRow(
                            text = categoria.nome.orEmpty(),
                            selected = categoria.id in state.categoriaIds,
                            onClick = { viewModel.toggleCategoria(categoria.id) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        NextButton(onClick = viewModel::next)
    }
}

/** Ecrãs 11 e 12 — doçura e acidez: um slider vertical de 5 níveis. "IGNORAR" não guarda resposta. */
@Composable
fun LevelStep(
    question: String,
    labels: List<String>,
    level: Int?,
    onLevelChange: (Int) -> Unit,
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    PillCard(title = TITULO_PREFERENCIAS, fillHeight = true, modifier = modifier) {
        Question(question)
        Spacer(Modifier.height(14.dp))
        LevelSlider(
            labels = labels,
            level = level,
            onLevelChange = onLevelChange,
            modifier = Modifier.weight(1f).heightIn(min = 200.dp).padding(horizontal = 6.dp),
        )
        Spacer(Modifier.height(10.dp))
        StepNavigation(state, viewModel)
    }
}

/**
 * Ecrã 13 — "VINHO: QUE TIPOS DE CASTAS MAIS APRECIAS?": escolha múltipla numa lista que mostra 10 castas e, ao chegar ao
 * fim, mais 10 (as 277 castas já estão na app; as de destaque vêm primeiro). Só aparece a quem escolheu vinho.
 */
@Composable
fun CastasStep(state: OnboardingUiState, viewModel: OnboardingViewModel, modifier: Modifier = Modifier) {
    PillCard(title = TITULO_PREFERENCIAS, fillHeight = true, modifier = modifier) {
        Question("VINHO: QUE TIPOS DE CASTAS MAIS APRECIAS?")
        Spacer(Modifier.height(12.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LookupContent(state.castas, onRetry = viewModel::carregarLookups) { castas ->
                val listState = rememberLazyListState()
                val visiveis = castas.take(state.castasVisiveis)

                // Quando o fim da lista aparece no ecrã, mostra mais 10. Se as 10 caberem todas no ecrã, continua até encher.
                LaunchedEffect(listState, visiveis.size) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
                        .collect { ultimo -> if (ultimo >= visiveis.size - 1) viewModel.carregarMaisCastas() }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().verticalScrollbar(listState),
                    contentPadding = PaddingValues(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visiveis, key = { it.id }) { casta ->
                        OptionRow(
                            text = casta.nome.orEmpty(),
                            selected = casta.id in state.castaIds,
                            onClick = { viewModel.toggleCasta(casta.id) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        StepNavigation(state, viewModel)
    }
}

/** A linha do fundo dos ecrãs 11 a 13: voltar, "IGNORAR" e seguir (que, no último ecrã, guarda tudo). */
@Composable
private fun StepNavigation(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    state.error?.let {
        Text(text = it, style = AquaText.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
    }
    NavRow(
        onBack = { viewModel.back() },
        onForward = viewModel::next,
        forwardLoading = state.saving,
        middle = { LinkText(text = "IGNORAR", onClick = viewModel::skip, style = AquaText.SmallLink) },
    )
}

@Composable
private fun Question(text: String) {
    Text(text = text, style = AquaText.Question, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}
