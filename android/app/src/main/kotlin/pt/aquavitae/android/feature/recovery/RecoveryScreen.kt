package pt.aquavitae.android.feature.recovery

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.feature.legal.TermsSheet
import pt.aquavitae.android.ui.components.AquaVitaeLogo
import pt.aquavitae.android.ui.components.AuthScaffold
import pt.aquavitae.android.ui.components.CodeInput
import pt.aquavitae.android.ui.components.LogoVariant
import pt.aquavitae.android.ui.components.NavRow
import pt.aquavitae.android.ui.components.PillCard
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy

/**
 * Recuperar password, em 3 passos dentro do mesmo cartão: (1) username ou email, (2) o código de 6 dígitos, (3) a password
 * nova. Ligado à API real. `onBack` sai para o login (seta para trás no 1.º passo ou o gesto de voltar);
 * `onPasswordChanged` é chamado quando a password muda (o login mostra então o "Password alterada!").
 */
@Composable
fun RecoveryScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showTerms by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val ime = WindowInsets.ime

    LaunchedEffect(state.done) { if (state.done) onPasswordChanged() }
    // O gesto de voltar percorre os passos ao contrário e só no 1.º sai do ecrã.
    BackHandler { if (!viewModel.back()) onBack() }

    // Com o teclado aberto o cartão não cabe todo e os botões de navegação ficavam cortados: acompanha o teclado a subir
    // e rola até ao fim (os campos e os botões estão todos na parte de baixo do conteúdo).
    LaunchedEffect(scroll, ime, density) {
        snapshotFlow { ime.getBottom(density) }.collect { imeBottom ->
            if (imeBottom > 0) scroll.scrollTo(scroll.maxValue)
        }
    }

    AuthScaffold(onOpenTerms = { showTerms = true }, scrollState = scroll) {
        AquaVitaeLogo(LogoVariant.Stacked)
        Spacer(Modifier.height(26.dp))

        PillCard(title = "RECUPERAR PASSWORD") {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                label = "recoveryStep",
            ) { step ->
                Column {
                    when (step) {
                        RecoveryStep.Identify -> IdentifyStep(state, viewModel, onBack)
                        RecoveryStep.Code -> CodeStep(state, viewModel)
                        RecoveryStep.NewPassword -> NewPasswordStep(state, viewModel)
                    }
                }
            }
        }
    }

    if (showTerms) {
        TermsSheet(onDismiss = { showTerms = false })
    }
}

@Composable
private fun IdentifyStep(state: RecoveryUiState, viewModel: RecoveryViewModel, onBack: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        viewModel.submit()
    }

    Text(
        text = "Escreve o teu username ou email e enviamos-te um código único.",
        style = AquaText.Hint,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    UnderlineField(
        value = state.identificador,
        onValueChange = viewModel::onIdentificadorChange,
        label = "Username ou Email",
        isError = state.error?.field == RecoveryField.Identificador,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done, autoCorrect = false),
        keyboardActions = KeyboardActions(onDone = { submit() }),
    )
    ErrorText(state.error)
    Spacer(Modifier.height(22.dp))
    NavRow(onBack = onBack, onForward = submit, forwardLoading = state.loading)
}

@Composable
private fun CodeStep(state: RecoveryUiState, viewModel: RecoveryViewModel) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        viewModel.submit()
    }
    // O email só se mostra (tapado) a quem o escreveu; a quem escreveu um username, uma frase que não revela nada da conta.
    val explicacao = if (looksLikeEmail(state.identificador)) {
        "Enviámos um código único para o email ‘${maskEmail(state.identificador.trim())}’"
    } else {
        "Se a conta existir, enviámos um código único para o email associado."
    }

    Text(text = explicacao, style = AquaText.Hint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(10.dp))
    HorizontalDivider(thickness = 1.5.dp, color = Burgundy)
    Spacer(Modifier.height(16.dp))
    Text(
        text = "INSIRA O CÓDIGO ABAIXO",
        style = AquaText.Question,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    CodeInput(
        value = state.codigo,
        onValueChange = viewModel::onCodigoChange,
        length = CODE_LENGTH,
        isError = state.error?.field == RecoveryField.Codigo,
        onDone = submit,
    )
    ErrorText(state.error)
    Spacer(Modifier.height(22.dp))
    NavRow(onBack = { viewModel.back() }, onForward = submit, forwardLoading = state.loading)
}

@Composable
private fun NewPasswordStep(state: RecoveryUiState, viewModel: RecoveryViewModel) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        viewModel.submit()
    }

    UnderlineField(
        value = state.novaPassword,
        onValueChange = viewModel::onNovaPasswordChange,
        label = "Nova Password",
        isPassword = true,
        isError = state.error?.field == RecoveryField.NovaPassword,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
    )
    Spacer(Modifier.height(4.dp))
    UnderlineField(
        value = state.repetirPassword,
        onValueChange = viewModel::onRepetirPasswordChange,
        label = "Repetir a Password",
        isPassword = true,
        isError = state.error?.field == RecoveryField.RepetirPassword,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submit() }),
    )
    ErrorText(state.error)
    Spacer(Modifier.height(22.dp))
    NavRow(onBack = { viewModel.back() }, onForward = submit, forwardLoading = state.loading)
}

@Composable
private fun ErrorText(error: RecoveryProblem?) {
    if (error != null) {
        Text(text = error.message, style = AquaText.Error, modifier = Modifier.padding(top = 8.dp))
    }
}
