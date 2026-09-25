package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint

/**
 * O avatar do utilizador (canto superior direito da homepage, e onde mais for preciso): o SVG escolhido no onboarding, ou,
 * sem avatar, as iniciais do nome (nome + apelido; sem apelido, as duas primeiras letras do username). Com `onClick`, abre
 * o ecrã de perfil (fatia 5) — sem ele, fica só decorativo (nenhum ecrã hoje o deixa sem destino, mas o parâmetro é
 * opcional para não obrigar todos os sítios que já o usavam a passá-lo).
 */
@Composable
fun AvatarBadge(avatar: Avatar?, iniciais: String, modifier: Modifier = Modifier, size: Dp = 40.dp, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(BurgundyTint)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        if (avatar?.path != null) {
            AsyncImage(model = resolveImageUrl(avatar.path), contentDescription = null, modifier = Modifier.size(size * 0.7f))
        } else {
            Text(text = iniciais, style = AquaText.Label.copy(color = Burgundy, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36).sp))
        }
    }
}

/** As iniciais a mostrar quando não há avatar: 1.ª letra do nome + 1.ª do apelido, ou as 2 primeiras do username. */
fun iniciaisDe(firstName: String?, lastName: String?, username: String?): String {
    val nome = firstName?.trim()?.firstOrNull()
    val apelido = lastName?.trim()?.firstOrNull()
    return when {
        nome != null && apelido != null -> "$nome$apelido"
        nome != null -> nome.toString()
        else -> username?.trim()?.take(2)?.ifEmpty { null } ?: "?"
    }.uppercase()
}
