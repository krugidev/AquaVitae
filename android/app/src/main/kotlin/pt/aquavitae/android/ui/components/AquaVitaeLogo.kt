package pt.aquavitae.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.R
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.InterFamily
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk

/** As três versões do logótipo que aparecem nos ecrãs de design. */
enum class LogoVariant {
    /** Emblema por cima do nome: login, registo, recuperar password. */
    Stacked,

    /** Emblema ao lado do nome, com a etiqueta "ESPIRITUOSAS & FACTOS": ecrã de loading. */
    Horizontal,

    /** Emblema pequeno ao lado do nome: topo dos ecrãs de preferências. */
    Compact,
}

@Composable
fun AquaVitaeLogo(variant: LogoVariant, modifier: Modifier = Modifier) {
    when (variant) {
        LogoVariant.Stacked -> Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Emblem(size = 92.dp)
            Spacer(Modifier.height(12.dp))
            Wordmark(fontSize = 26.sp)
        }

        LogoVariant.Horizontal -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            Emblem(size = 72.dp)
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Wordmark(fontSize = 30.sp)
                Text(
                    text = "ESPIRITUOSAS & FACTOS",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 1.6.sp,
                    color = MutedInk,
                )
            }
        }

        LogoVariant.Compact -> Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            Emblem(size = 44.dp)
            Spacer(Modifier.width(10.dp))
            Wordmark(fontSize = 22.sp)
        }
    }
}

@Composable
private fun Emblem(size: Dp) {
    Image(
        painter = painterResource(R.drawable.ic_logo_emblem),
        contentDescription = null,
        modifier = Modifier.size(size),
    )
}

// "AQUA" a tinta e "VITAE" a grená. O tipo de letra do nome é aproximado (Inter): quando houver o SVG do
// logótipo, substituir este texto pelo desenho exato.
@Composable
private fun Wordmark(fontSize: TextUnit) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.Normal)) { append("AQUA") }
            withStyle(SpanStyle(color = Burgundy, fontWeight = FontWeight.Light)) { append("VITAE") }
        },
        fontFamily = InterFamily,
        fontSize = fontSize,
        letterSpacing = 0.09.em,
    )
}
