package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.DialogShape
import pt.aquavitae.android.ui.theme.InterFamily

/**
 * Popup dos termos e condições, no estilo do popup "Password alterada!" (grená com texto branco). Aparece antes de criar
 * a conta e depois do login de quem ainda não aceitou (ou aceitou uma versão anterior). `onRead` ("LER OS TERMOS") abre o
 * popup de baixo com o texto (`TermsSheet`, que o vai buscar à API).
 */
@Composable
fun TermsDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onRead: () -> Unit,
    loading: Boolean,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        DialogScrim()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DialogShape)
                .background(Burgundy)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Termos e Condições", style = AquaText.Title)
            Text(
                text = "Para continuares tens de aceitar os Termos e Condições da AquaVitae.",
                style = TextStyle(fontFamily = InterFamily, fontSize = 15.sp, color = Color.White),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "LER OS TERMOS",
                style = TextStyle(
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                ),
                modifier = Modifier.clickable(enabled = !loading, onClick = onRead).padding(vertical = 6.dp),
            )
            Button(
                onClick = onAccept,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Burgundy,
                    disabledContainerColor = Color.White,
                    disabledContentColor = Burgundy,
                ),
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Burgundy, strokeWidth = 2.5.dp)
                } else {
                    Text("ACEITAR E CONTINUAR", style = AquaText.Button.copy(fontSize = 16.sp, color = Burgundy))
                }
            }
            Text(
                text = "Agora não",
                style = TextStyle(fontFamily = InterFamily, fontSize = 14.sp, color = Color.White),
                modifier = Modifier.clickable(enabled = !loading, onClick = onDismiss).padding(vertical = 6.dp),
            )
        }
    }
}
