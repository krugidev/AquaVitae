package pt.aquavitae.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.ui.theme.Burgundy

/**
 * A bandeira de um país, em emoji, a partir do código ISO 3166-1 alfa-2 ("PT" → 🇵🇹): cada letra passa ao "símbolo de
 * indicador regional" correspondente e o Android desenha o par como a bandeira. `null` se o código não for válido.
 */
fun flagEmoji(codigoPais: String?): String? {
    val codigo = codigoPais?.trim()?.uppercase() ?: return null
    if (codigo.length != 2 || codigo.any { it !in 'A'..'Z' }) return null
    return codigo.map { String(Character.toChars(REGIONAL_INDICATOR_A + (it - 'A'))) }.joinToString("")
}

private const val REGIONAL_INDICATOR_A = 0x1F1E6

/** O rebordo pequeno com a bandeira e a seta de lista ("🇵🇹 ▾"), ao lado do campo da nacionalidade. */
@Composable
fun FlagChip(flag: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier.clip(shape).border(1.5.dp, Burgundy, shape).padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (flag != null) Text(text = flag, fontSize = 18.sp)
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Escolher", tint = Burgundy, modifier = Modifier.size(20.dp))
    }
}
