package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint

private val OptionShape = RoundedCornerShape(12.dp)

/**
 * Uma opção de uma lista de escolha múltipla (tipos de bebida, castas): linha com borda grená e, à esquerda, um círculo que
 * fica cheio, com um visto, quando está escolhida. O texto vai sempre em maiúsculas, como nos ecrãs de design.
 */
@Composable
fun OptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(OptionShape)
            .background(if (selected) BurgundyTint else Color.Transparent)
            .border(if (selected) 2.dp else 1.5.dp, Burgundy, OptionShape)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) Burgundy else Color.Transparent)
                .border(1.5.dp, Burgundy, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text = text.uppercase(), style = AquaText.Option, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Pílula de filtro (as categorias de avatar): cheia a grená quando escolhida, só com contorno quando não. */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) Burgundy else Color.Transparent)
            .border(1.5.dp, Burgundy, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = AquaText.Label.copy(fontSize = 13.sp, color = if (selected) Color.White else Burgundy))
    }
}

/**
 * Mostra uma lista que vem da API conforme o estado dela: a carregar (os 5 pontos), erro (mensagem e "TENTAR DE NOVO") ou
 * pronta (`content` recebe os dados).
 */
@Composable
fun <T> LookupContent(
    state: LookupState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        LookupState.Loading -> Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { LoadingDots() }

        is LookupState.Error -> Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = state.message, style = AquaText.Error, textAlign = TextAlign.Center)
            LinkText(text = "TENTAR DE NOVO", onClick = onRetry)
        }

        is LookupState.Ready -> content(state.data)
    }
}
