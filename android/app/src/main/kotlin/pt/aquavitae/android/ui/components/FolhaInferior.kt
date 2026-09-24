package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * Uma folha que sobe do fundo do ecrã (cantos de cima arredondados, pega no topo), do tamanho do seu conteúdo — a "Conta e
 * segurança" e o "Alterar password" (`android/design/perfil/05-reviews-e-conta-seguranca.png`). Tocar fora fecha-a. Sobe com o
 * teclado (`imePadding`), para os campos de texto não ficarem tapados.
 *
 * Tem o `DialogFillScreen` (sem ele a janela do `Dialog` mede-se pelo conteúdo e a folha não fica no fundo) e lê o inset da barra
 * de navegação **antes** do `Dialog`, onde está certo (ver `CLAUDE.md`, "Um `Dialog` (Compose) a ecrã inteiro precisa de dois truques").
 */
@Composable
fun FolhaInferior(
    onDismiss: () -> Unit,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        DialogScrim()
        DialogFillScreen()
        Box(Modifier.fillMaxSize().imePadding().padding(bottom = alturaBarraNavegacao).pointerInput(Unit) { detectTapGestures { onDismiss() } }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Paper)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(MutedInk.copy(alpha = 0.35f)))
                }
                conteudo()
            }
        }
    }
}

/** O título de uma folha, com a última palavra a grená e em itálico: "Conta e *segurança*". */
fun tituloComDestaque(inicio: String, destaque: String): AnnotatedString = buildAnnotatedString {
    append(inicio)
    withStyle(AquaText.SectionSerifAccent.copy(fontStyle = FontStyle.Italic).toSpanStyle()) { append(destaque) }
}

/** A linha do título de uma folha: o título (serifa, 26 sp) e o "X" de fechar à direita. */
@Composable
fun CabecalhoFolha(titulo: AnnotatedString, onFechar: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = titulo, style = AquaText.SectionSerif.copy(fontSize = 26.sp), modifier = Modifier.weight(1f))
        IconButton(onClick = onFechar) { Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = MutedInk) }
    }
}
