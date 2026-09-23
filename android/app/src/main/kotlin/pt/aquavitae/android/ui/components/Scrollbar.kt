package pt.aquavitae.android.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.Burgundy

/**
 * A barra de deslocamento fina do lado direito de uma lista (a dos ecrãs de design). Só aparece se a lista tem mais itens
 * do que os que cabem. Numa lista que vai crescendo ("carregar mais") o cursor encolhe à medida que chegam itens.
 */
fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Burgundy.copy(alpha = 0.4f),
): Modifier = drawWithContent {
    drawContent()
    val info = state.layoutInfo
    val total = info.totalItemsCount
    val visible = info.visibleItemsInfo
    if (total == 0 || visible.isEmpty() || visible.size >= total) return@drawWithContent

    val thumbHeight = (size.height * visible.size / total).coerceAtLeast(28.dp.toPx())
    val first = visible.first()
    val scrolled = first.index + (-first.offset).toFloat() / first.size.coerceAtLeast(1)
    val progress = (scrolled / (total - visible.size).coerceAtLeast(1)).coerceIn(0f, 1f)
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - width.toPx(), (size.height - thumbHeight) * progress),
        size = Size(width.toPx(), thumbHeight),
        cornerRadius = CornerRadius(width.toPx() / 2),
    )
}
