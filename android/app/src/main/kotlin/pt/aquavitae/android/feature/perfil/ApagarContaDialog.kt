package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.DialogShape
import pt.aquavitae.android.ui.theme.ErrorRed
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * O popup de verificação de "Apagar conta" (pedido do utilizador): diz **o que se perde** (os totais do perfil), avisa que não se
 * desfaz e só apaga depois de escrever a password da conta. Ver [ApagarContaViewModel]. Se correr bem, `onSessaoTerminada` leva ao
 * login, que diz "A tua conta foi apagada.".
 */
@Composable
fun ApagarContaDialog(
    utilizador: UtilizadorMe?,
    onDismiss: () -> Unit,
    onSessaoTerminada: () -> Unit,
    viewModel: ApagarContaViewModel = hiltViewModel(key = "apagar-conta"),
) {
    LaunchedEffect(Unit) { viewModel.iniciar() }
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val apagar = {
        focusManager.clearFocus()
        viewModel.apagar(onApagada = onSessaoTerminada)
    }
    val perde = resumoDoQueSePerde(utilizador)

    Dialog(
        onDismissRequest = { if (!state.aApagar) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = !state.aApagar),
    ) {
        DialogScrim()
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().clip(DialogShape).background(Paper).padding(horizontal = 24.dp, vertical = 22.dp),
        ) {
            Text(text = "Apagar a conta?", style = AquaText.SectionSerif.copy(fontSize = 24.sp))
            Spacer(Modifier.height(10.dp))
            if (perde.isEmpty()) {
                Text(text = "Ainda não guardaste nada: só a conta é apagada.", style = AquaText.Field.copy(fontSize = 14.sp))
            } else {
                Text(text = "Vais perder, para sempre:", style = AquaText.Field.copy(fontSize = 14.sp))
                Spacer(Modifier.height(6.dp))
                perde.forEach { linha -> Text(text = "•  $linha", style = AquaText.Field.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) }
            }
            Spacer(Modifier.height(10.dp))
            Text(text = "Não pode ser desfeito.", style = AquaText.Error.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            UnderlineField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "A tua password",
                isPassword = true,
                isError = state.erro != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { apagar() }),
            )
            state.erro?.let { Text(text = it, style = AquaText.Error, modifier = Modifier.padding(top = 8.dp)) }
            Text(
                text = "Escreve a password da tua conta para confirmar.",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !state.aApagar,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = ButtonShape,
                    border = BorderStroke(1.dp, Ink),
                ) { Text(text = "Cancelar", style = AquaText.Label.copy(color = Ink, fontSize = 14.sp)) }
                Button(
                    onClick = apagar,
                    enabled = !state.aApagar,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = ButtonShape,
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White,
                        disabledContainerColor = ErrorRed,
                        disabledContentColor = Color.White,
                    ),
                ) {
                    if (state.aApagar) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text(text = "Apagar conta", style = AquaText.Label.copy(color = Color.White, fontSize = 13.sp), maxLines = 1)
                    }
                }
            }
        }
    }
}
