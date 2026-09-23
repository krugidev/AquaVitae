package pt.aquavitae.android.feature.cave

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pt.aquavitae.android.ui.components.DialogFillScreen
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

private const val NOME_MAX = 100
private const val DESCRICAO_MAX = 500

/**
 * O popup "Nova cave" (`android/design/caves/02-nova-cave-popup.png`): nome + descrição opcional. "Já lá dentro"
 * fica sempre vazio (é uma cave nova) — o texto explica que se pode mover garrafas para lá depois de criada.
 */
@Composable
fun NovaCaveSheet(aCriar: Boolean, erro: String?, onCriar: (nome: String, descricao: String?) -> Unit, onDismiss: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
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
                    .fillMaxHeight(0.62f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Nova cave", style = AquaText.SectionSerif, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
                }
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                    Text(
                        text = "Uma cave é uma divisão tua — por local, por ocasião, por ano de compra. As garrafas podem mudar de cave depois.",
                        style = AquaText.Footer.copy(color = Burgundy, fontSize = 12.sp),
                    )
                    Spacer(Modifier.height(18.dp))
                    CampoComContador(rotulo = "NOME", valor = nome, onValueChange = { nome = it.take(NOME_MAX) }, max = NOME_MAX, linhas = 1)
                    Spacer(Modifier.height(16.dp))
                    CampoComContador(
                        rotulo = "DESCRIÇÃO",
                        rotuloExtra = "OPCIONAL",
                        valor = descricao,
                        onValueChange = { descricao = it.take(DESCRICAO_MAX) },
                        max = DESCRICAO_MAX,
                        linhas = 3,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(text = "JÁ LÁ DENTRO", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BurgundyTint.copy(alpha = 0.25f)).padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "Ainda sem garrafas", style = AquaText.Label.copy(fontSize = 14.sp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Depois de criar, podes mover garrafas de outra cave ou adicionar do catálogo.",
                            style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    erro?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(text = it, style = AquaText.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { onCriar(nome, descricao) },
                        enabled = nome.isNotBlank() && !aCriar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
                    ) {
                        Text(if (aCriar) "A criar…" else "Criar cave", style = AquaText.Button.copy(fontSize = 16.sp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Cancelar",
                        style = AquaText.Footer.copy(color = MutedInk, fontSize = 13.sp),
                        modifier = Modifier.pointerInput(Unit) { detectTapGestures { onDismiss() } }.padding(6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoComContador(rotulo: String, rotuloExtra: String? = null, valor: String, onValueChange: (String) -> Unit, max: Int, linhas: Int) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(text = rotulo, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), modifier = Modifier.weight(1f))
            rotuloExtra?.let { Text(text = it, style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp)) }
        }
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BurgundyTint.copy(alpha = 0.25f))
                .padding(horizontal = 14.dp, vertical = if (linhas == 1) 12.dp else 10.dp),
        ) {
            BasicTextField(
                value = valor,
                onValueChange = onValueChange,
                textStyle = AquaText.Field.copy(fontSize = 15.sp),
                singleLine = linhas == 1,
                modifier = Modifier.fillMaxWidth().let { if (linhas > 1) it.height((linhas * 20).dp) else it },
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = "${valor.length} / $max", style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}
