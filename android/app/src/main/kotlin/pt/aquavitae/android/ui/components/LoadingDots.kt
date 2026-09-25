package pt.aquavitae.android.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.DotsRed

private const val DOT_COUNT = 5
private const val CYCLE_MS = 1300
private const val STAGGER_MS = 130
private const val MIN_SCALE = 0.4f

/**
 * Cinco pontos vermelhos que crescem e encolhem em sequência (uma "onda" da esquerda para a direita).
 * É o indicador de carregamento da app.
 */
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    color: Color = DotsRed,
) {
    val transition = rememberInfiniteTransition(label = "loadingDots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOT_COUNT) { index ->
            val scale by transition.animateFloat(
                initialValue = MIN_SCALE,
                targetValue = MIN_SCALE,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = CYCLE_MS
                        MIN_SCALE at 0 using LinearOutSlowInEasing
                        1f at 320 using LinearOutSlowInEasing
                        MIN_SCALE at 640
                        MIN_SCALE at CYCLE_MS
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * STAGGER_MS),
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(color, CircleShape),
            )
        }
    }
}
