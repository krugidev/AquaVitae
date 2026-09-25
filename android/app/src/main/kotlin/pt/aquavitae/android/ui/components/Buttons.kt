package pt.aquavitae.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ButtonShape

/** O botão grande grená dos ecrãs de design ("LOGIN", "COMEÇAR"). Com `loading` mostra uma roda em vez do texto. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Burgundy,
            contentColor = Color.White,
            disabledContainerColor = Burgundy,
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp, disabledElevation = 3.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Text(text = text, style = AquaText.Button)
        }
    }
}

/** Texto clicável em maiúsculas por baixo do cartão ("AINDA NÃO TENS CONTA? REGISTA-TE") ou "Esqueci-me da password". */
@Composable
fun LinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = AquaText.Link,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
    )
}
