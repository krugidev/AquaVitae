package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.feature.recovery.CODE_LENGTH
import pt.aquavitae.android.feature.recovery.RecoveryField
import pt.aquavitae.android.feature.recovery.RecoveryProblem
import pt.aquavitae.android.feature.recovery.maskEmail
import pt.aquavitae.android.ui.components.CabecalhoFolha
import pt.aquavitae.android.ui.components.CodeInput
import pt.aquavitae.android.ui.components.CodigoValidade
import pt.aquavitae.android.ui.components.FolhaInferior
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.components.tituloComDestaque
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk

/**
 * "Alterar password", aberto da folha "Conta e segurança": três passos com o sistema do código da recuperação de password (enviar o
 * código para o email da conta → escrevê-lo, com o temporizador de 15 min e "pedir novo" → a password nova duas vezes) e o fim. Ver
 * [AlterarPasswordViewModel]. Sem mockup próprio: usa a mesma linguagem visual da recuperação (campos sublinhados, código em caixa).
 */
@Composable
fun AlterarPasswordSheet(
    email: String,
    onDismiss: () -> Unit,
    onSessaoTerminada: () -> Unit,
    viewModel: AlterarPasswordViewModel = hiltViewModel(key = "alterar-password"),
) {
    LaunchedEffect(email) { viewModel.iniciar(email) }
    val state by viewModel.state.collectAsState()
    // Se a password mudou mas não se conseguiu entrar de novo, a sessão deste telemóvel já não existe: fechar leva ao login.
    val fechar = { if (state.passo == AlterarPasswordPasso.Concluido && !state.sessaoMantida) onSessaoTerminada() else onDismiss() }

    FolhaInferior(onDismiss = fechar) {
        CabecalhoFolha(titulo = tituloComDestaque("Alterar ", "password"), onFechar = fechar)
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp)) {
            when (state.passo) {
                AlterarPasswordPasso.Enviar -> PassoEnviar(state, viewModel, onCancelar = onDismiss)
                AlterarPasswordPasso.Codigo -> PassoCodigo(state, viewModel)
                AlterarPasswordPasso.NovaPassword -> PassoNovaPassword(state, viewModel)
                AlterarPasswordPasso.Concluido -> PassoConcluido(state, onFechar = fechar)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PassoEnviar(state: AlterarPasswordUiState, viewModel: AlterarPasswordViewModel, onCancelar: () -> Unit) {
    Text(
        text = "Para confirmar que és tu, enviamos um código de $CODE_LENGTH dígitos para o email ‘${maskEmail(state.email)}’.",
        style = AquaText.Hint,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    ErroDoPasso(state.erro)
    Spacer(Modifier.height(18.dp))
    PrimaryButton(text = "ENVIAR CÓDIGO", onClick = viewModel::enviarCodigo, loading = state.aCarregar, enabled = state.email.isNotBlank())
    LinkText(text = "CANCELAR", onClick = onCancelar, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PassoCodigo(state: AlterarPasswordUiState, viewModel: AlterarPasswordViewModel) {
    val focusManager = LocalFocusManager.current
    val continuar = {
        focusManager.clearFocus()
        viewModel.verificarCodigo()
    }
    Text(
        text = "Enviámos um código único para o email ‘${maskEmail(state.email)}’",
        style = AquaText.Hint,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    HorizontalDivider(thickness = 1.5.dp, color = Burgundy)
    Spacer(Modifier.height(16.dp))
    Text(text = "INSIRA O CÓDIGO ABAIXO", style = AquaText.Question, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(10.dp))
    CodeInput(
        value = state.codigo,
        onValueChange = viewModel::onCodigoChange,
        length = CODE_LENGTH,
        isError = state.erro?.field == RecoveryField.Codigo,
        onDone = continuar,
    )
    ErroDoPasso(state.erro)
    Spacer(Modifier.height(10.dp))
    CodigoValidade(contagem = state.contagem, onPedirNovo = viewModel::pedirNovoCodigo, aPedir = state.aPedirNovo)
    Spacer(Modifier.height(14.dp))
    PrimaryButton(text = "CONTINUAR", onClick = continuar, loading = state.aCarregar)
    LinkText(text = "VOLTAR", onClick = { viewModel.voltar() }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PassoNovaPassword(state: AlterarPasswordUiState, viewModel: AlterarPasswordViewModel) {
    val focusManager = LocalFocusManager.current
    val guardar = { guardar(focusManager, viewModel) }
    UnderlineField(
        value = state.novaPassword,
        onValueChange = viewModel::onNovaPasswordChange,
        label = "Nova Password",
        isPassword = true,
        isError = state.erro?.field == RecoveryField.NovaPassword,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
    )
    Spacer(Modifier.height(4.dp))
    UnderlineField(
        value = state.repetirPassword,
        onValueChange = viewModel::onRepetirPasswordChange,
        label = "Repetir a Password",
        isPassword = true,
        isError = state.erro?.field == RecoveryField.RepetirPassword,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { guardar() }),
    )
    ErroDoPasso(state.erro)
    Spacer(Modifier.height(18.dp))
    PrimaryButton(text = "GUARDAR PASSWORD", onClick = guardar, loading = state.aCarregar)
    LinkText(text = "VOLTAR", onClick = { viewModel.voltar() }, modifier = Modifier.fillMaxWidth())
}

private fun guardar(focusManager: FocusManager, viewModel: AlterarPasswordViewModel) {
    focusManager.clearFocus()
    viewModel.guardar()
}

@Composable
private fun PassoConcluido(state: AlterarPasswordUiState, onFechar: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(Burgundy), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
    }
    Spacer(Modifier.height(14.dp))
    Text(text = "Password alterada!", style = AquaText.Question.copy(fontSize = 18.sp), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Text(
        text = if (state.sessaoMantida) {
            "Terminámos a sessão nos outros dispositivos onde tinhas entrado. Neste continuas com a sessão iniciada."
        } else {
            "Entra outra vez com a password nova."
        },
        style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(18.dp))
    PrimaryButton(text = if (state.sessaoMantida) "FECHAR" else "ENTRAR", onClick = onFechar)
}

@Composable
private fun ErroDoPasso(erro: RecoveryProblem?) {
    if (erro != null) Text(text = erro.message, style = AquaText.Error, modifier = Modifier.padding(top = 8.dp))
}
