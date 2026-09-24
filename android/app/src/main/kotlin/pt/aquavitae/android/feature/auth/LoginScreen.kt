package pt.aquavitae.android.feature.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.feature.legal.TermsSheet
import pt.aquavitae.android.ui.components.AquaVitaeLogo
import pt.aquavitae.android.ui.components.AuthCard
import pt.aquavitae.android.ui.components.AuthScaffold
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LogoVariant
import pt.aquavitae.android.ui.components.PasswordChangedDialog
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.components.TermsDialog
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.components.overlapTop
import pt.aquavitae.android.ui.theme.AquaText

/**
 * Ecrã de login: um campo "Username ou Email" e a password. Ligado ao [AuthViewModel] e à API real.
 * `showPasswordChanged`: vem da recuperação de password, que acabou de mudar a password — mostra o popup "Password
 * alterada!" por cima do ecrã; `onPasswordChangedDismissed` diz a quem navega que o popup já se fechou.
 */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    showPasswordChanged: Boolean = false,
    onPasswordChangedDismissed: () -> Unit = {},
    // A sessão morreu a meio da utilização (o token não se conseguiu renovar): explica porque se voltou aqui.
    sessaoExpirada: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var identificador by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Done) {
            onLoggedIn()
            viewModel.resetState()
        }
    }

    val error = state as? AuthUiState.Error
    val submit = {
        focusManager.clearFocus()
        viewModel.login(identificador, password)
    }

    AuthScaffold(onOpenTerms = { showTerms = true }) {
        AquaVitaeLogo(LogoVariant.Stacked)
        Spacer(Modifier.height(26.dp))

        AuthCard {
            UnderlineField(
                value = identificador,
                onValueChange = { identificador = it; viewModel.clearError() },
                label = "Username ou Email",
                isError = error?.field == AuthField.Identificador,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, autoCorrect = false),
            )
            Spacer(Modifier.height(4.dp))
            UnderlineField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = "Password",
                isPassword = true,
                isError = error?.field == AuthField.Password,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            LinkText(
                text = "Esqueci-me da password",
                onClick = onForgotPassword,
                style = AquaText.SmallLink,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.End),
            )
            if (error != null) {
                Text(text = error.message, style = AquaText.Error, modifier = Modifier.padding(top = 2.dp))
            } else if (sessaoExpirada) {
                Text(text = "A tua sessão expirou. Entra novamente.", style = AquaText.Hint, modifier = Modifier.padding(top = 2.dp))
            }
            // Espaço para o botão, que se sobrepõe à margem de baixo do cartão.
            Spacer(Modifier.height(30.dp))
        }

        PrimaryButton(
            text = "LOGIN",
            onClick = submit,
            loading = state is AuthUiState.Loading,
            modifier = Modifier.overlapTop(30.dp),
        )
        LinkText(text = "AINDA NÃO TENS CONTA? REGISTA-TE", onClick = onNavigateToRegister)
    }

    (state as? AuthUiState.NeedsTerms)?.let { terms ->
        TermsDialog(
            onAccept = viewModel::acceptTerms,
            onDismiss = viewModel::declineTerms,
            onRead = { showTerms = true },
            loading = terms.loading,
        )
    }

    if (showPasswordChanged) {
        PasswordChangedDialog(onDismiss = onPasswordChangedDismissed)
    }

    // Por último: assim aparece por cima do popup de aceitação quando se abre a partir do "LER OS TERMOS".
    if (showTerms) {
        TermsSheet(onDismiss = { showTerms = false })
    }
}
