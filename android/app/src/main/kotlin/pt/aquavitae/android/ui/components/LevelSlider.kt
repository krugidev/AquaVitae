package pt.aquavitae.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.SliderBlue
import pt.aquavitae.android.ui.theme.SliderTop

/**
 * O slider vertical dos ecrãs de doçura e acidez: uma barra em degradê (do quase branco ao azul), um ponteiro e os rótulos
 * dos níveis, um por cada `labels` (de cima para baixo: nível 1 a `labels.size`). Escolhe-se tocando num nível ou
 * arrastando o dedo pela barra. `level == null` = ainda não escolheu nada: o ponteiro fica esbatido a meio e não conta
 * como resposta. Precisa de uma altura limitada (ex.: `Modifier.weight(1f)`).
 */
@Composable
fun LevelSlider(
    labels: List<String>,
    level: Int?,
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = labels.size
    var heightPx by remember { mutableIntStateOf(0) }
    val currentOnChange by rememberUpdatedState(onLevelChange)
    fun levelAt(y: Float): Int = ((y / heightPx.coerceAtLeast(1)) * count).toInt().coerceIn(0, count - 1) + 1

    // Onde fica o ponteiro, como fração da altura: o centro da faixa do nível (ou a meio, esbatido, sem resposta).
    val pointerFraction by animateFloatAsState(
        targetValue = ((level ?: ((count + 1) / 2)) - 0.5f) / count,
        label = "sliderPointer",
    )
    val barShape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { heightPx = it.height }
            .pointerInput(count) { detectTapGestures { currentOnChange(levelAt(it.y)) } }
            .pointerInput(count) {
                detectVerticalDragGestures(onDragStart = { currentOnChange(levelAt(it.y)) }) { change, _ ->
                    change.consume()
                    currentOnChange(levelAt(change.position.y))
                }
            }
            .semantics {
                contentDescription = "Escala com ${labels.size} níveis"
                stateDescription = level?.let { labels.getOrNull(it - 1) } ?: "Sem resposta"
            },
    ) {
        Box(
            Modifier
                .width(16.dp)
                .fillMaxHeight()
                .clip(barShape)
                .background(Brush.verticalGradient(listOf(SliderTop, SliderBlue)))
                .border(1.dp, Burgundy.copy(alpha = 0.18f), barShape),
        )
        Canvas(Modifier.width(24.dp).fillMaxHeight()) {
            val cy = size.height * pointerFraction
            val left = 6.dp.toPx()
            val w = 10.dp.toPx()
            val half = 8.dp.toPx()
            val ponteiro = Path().apply {
                moveTo(left, cy - half)
                lineTo(left + w, cy)
                lineTo(left, cy + half)
                close()
            }
            drawPath(ponteiro, color = Burgundy.copy(alpha = if (level == null) 0.25f else 1f))
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            labels.forEachIndexed { index, label ->
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    val emphasis = if (level == null || level == index + 1) 1f else 0.6f
                    Text(text = label.uppercase(), style = AquaText.Option.copy(color = Burgundy.copy(alpha = emphasis)))
                }
            }
        }
    }
}
