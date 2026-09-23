package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.CheckCircle
import pt.aquavitae.android.ui.theme.DialogShape

private const val AUTO_DISMISS_MS = 2600L

/**
 * O popup "Password alterada!" (grená, com um visto), por cima do login escurecido. Fecha-se sozinho passados uns segundos,
 * ou ao tocar fora dele: não tem botões (o desenho não os tem).
 */
@Composable
fun PasswordChangedDialog(onDismiss: () -> Unit) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(Unit) {
        delay(AUTO_DISMISS_MS)
        currentOnDismiss()
    }
    Dialog(onDismissRequest = onDismiss) {
        DialogScrim()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DialogShape)
                .background(Burgundy)
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(CheckCircle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Text(text = "Password alterada!", style = AquaText.Title.copy(fontSize = 24.sp))
        }
    }
}
