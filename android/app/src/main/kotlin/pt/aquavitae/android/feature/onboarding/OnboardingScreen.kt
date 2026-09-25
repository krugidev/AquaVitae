package pt.aquavitae.android.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.ui.components.AquaVitaeLogo
import pt.aquavitae.android.ui.components.LogoVariant
import pt.aquavitae.android.ui.theme.Paper

/**
 * O onboarding de quem acabou de se registar, em 7 ecrãs (nome → nacionalidade e descrição → avatar → tipos de bebida →
 * doçura → acidez → castas, que só aparece a quem escolheu vinho). Tudo é opcional e ligado à API real; no fim vai para o
 * catálogo. Os 3 primeiros ecrãs levam o logótipo empilhado e os das preferências o compacto, como nos desenhos.
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.done) { if (state.done) onOnboardingComplete() }
    // O gesto de voltar percorre os ecrãs ao contrário; no 1.º sai da app (o registo já não está por baixo).
    BackHandler(enabled = state.step != OnboardingStep.Nome) { viewModel.back() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 36.dp),
    ) {
        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "onboardingStep",
        ) { step ->
            when (step) {
                // Ecrãs curtos, com o logótipo empilhado: rolam (o teclado tapa uma parte).
                OnboardingStep.Nome -> ProfileScreenBody { NameStep(state, viewModel) }
                OnboardingStep.Nacionalidade -> ProfileScreenBody { NationalityStep(state, viewModel) }
                OnboardingStep.Avatar -> ProfileScreenBody { AvatarStep(state, viewModel) }

                // Ecrãs de preferências: logótipo compacto e um cartão que ocupa a altura que sobra, com a navegação ao fundo.
                OnboardingStep.Tipos -> PreferenceScreenBody { TypesStep(state, viewModel, it) }
                OnboardingStep.Docura -> PreferenceScreenBody {
                    LevelStep(
                        question = "GOSTAS DE BEBIDAS MAIS DOCES OU SECAS?",
                        labels = NIVEIS_DOCURA,
                        level = state.docura,
                        onLevelChange = viewModel::setDocura,
                        state = state,
                        viewModel = viewModel,
                        modifier = it,
                    )
                }

                OnboardingStep.Acidez -> PreferenceScreenBody {
                    LevelStep(
                        question = "GOSTAS DE BEBIDAS MAIS MACIAS OU FRESCAS?",
                        labels = NIVEIS_ACIDEZ,
                        level = state.acidez,
                        onLevelChange = viewModel::setAcidez,
                        state = state,
                        viewModel = viewModel,
                        modifier = it,
                    )
                }

                OnboardingStep.Castas -> PreferenceScreenBody { CastasStep(state, viewModel, it) }
            }
        }
    }
}

// Logótipo empilhado por cima do cartão, tudo dentro de uma coluna que rola.
@Composable
private fun ProfileScreenBody(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AquaVitaeLogo(LogoVariant.Stacked)
        Spacer(Modifier.height(26.dp))
        content()
    }
}

// Logótipo compacto no canto superior esquerdo e o cartão a ocupar o resto (recebe o modifier que o faz encher).
@Composable
private fun PreferenceScreenBody(content: @Composable (Modifier) -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 20.dp, bottom = 24.dp)) {
        Box(Modifier.fillMaxWidth()) { AquaVitaeLogo(LogoVariant.Compact, Modifier.align(Alignment.CenterStart)) }
        Spacer(Modifier.height(14.dp))
        content(Modifier.weight(1f).fillMaxHeight())
    }
}
