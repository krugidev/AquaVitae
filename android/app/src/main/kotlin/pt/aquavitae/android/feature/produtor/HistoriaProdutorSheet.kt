package pt.aquavitae.android.feature.produtor

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pt.aquavitae.android.ui.components.DialogFillScreen
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * O popup "Ler a história completa" da página do produtor: a `produtor_historia` inteira, a deslizar. Só se abre com
 * história (a página nem mostra a ligação sem ela). Os parágrafos vêm da BD separados por linhas em branco e mantêm-se.
 */
@Composable
fun HistoriaProdutorSheet(nome: String, historia: String, onDismiss: () -> Unit) {
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        DialogScrim()
        DialogFillScreen()
        Box(Modifier.fillMaxSize().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "A HISTÓRIA DE", style = AquaText.Footer.copy(fontSize = 10.sp))
                        Text(text = nome, style = AquaText.SectionSerif.copy(fontSize = 26.sp, lineHeight = 30.sp))
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
                }
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                    Text(text = historia, style = AquaText.Field.copy(fontSize = 15.sp, lineHeight = 23.sp))
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}
