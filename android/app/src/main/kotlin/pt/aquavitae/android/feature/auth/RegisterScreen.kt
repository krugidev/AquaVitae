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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.feature.legal.TermsSheet
import pt.aquavitae.android.ui.components.AquaVitaeLogo
import pt.aquavitae.android.ui.components.AuthCard
import pt.aquavitae.android.ui.components.AuthScaffold
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LogoVariant
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.components.TermsDialog
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.components.overlapTop
import pt.aquavitae.android.ui.theme.AquaText

/**
 * Ecrã de registo, fase 1 (username, email, password). O nome, a nacionalidade, o avatar e as preferências vêm nos ecrãs
 * seguintes, já com a conta criada. "COMEÇAR" valida, mostra o popup dos termos e só depois cria a conta.
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        if (state is AuthUiState.Done) {
            onRegistered()
            viewModel.resetState()
        }
    }

    val error = state as? AuthUiState.Error
    val submit = {
        focusManager.clearFocus()
        viewModel.register(username, email, password)
    }

    AuthScaffold(onOpenTerms = { showTerms = true }) {
        AquaVitaeLogo(LogoVariant.Stacked)
        Spacer(Modifier.height(26.dp))

        AuthCard {
            UnderlineField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = "Username",
                isError = error?.field == AuthField.Username,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next, autoCorrect = false),
            )
            Spacer(Modifier.height(4.dp))
            UnderlineField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = "Email",
                isError = error?.field == AuthField.Email,
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
            if (error != null) {
                Text(text = error.message, style = AquaText.Error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(30.dp))
        }

        PrimaryButton(
            text = "COMEÇAR",
            onClick = submit,
            loading = state is AuthUiState.Loading,
            modifier = Modifier.overlapTop(30.dp),
        )
        LinkText(text = "JÁ TENS UMA CONTA? LOGIN", onClick = onNavigateToLogin)
    }

    (state as? AuthUiState.NeedsTerms)?.let { terms ->
        TermsDialog(
            onAccept = viewModel::acceptTerms,
            onDismiss = viewModel::declineTerms,
            onRead = { showTerms = true },
            loading = terms.loading,
        )
    }

    // Por último: assim aparece por cima do popup de aceitação quando se abre a partir do "LER OS TERMOS".
    if (showTerms) {
        TermsSheet(onDismiss = { showTerms = false })
    }
}
