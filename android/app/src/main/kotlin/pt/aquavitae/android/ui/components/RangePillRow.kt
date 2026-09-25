package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy

/**
 * O seletor de intervalo 1 a 5 do popup de filtros (acidez, doçura): uma pílula por nível, todas as do intervalo
 * escolhido cheias a grená. Toca-se num nível para o escolher sozinho (`min = max`); toca-se noutro para o intervalo se
 * esticar até lá (o mais próximo do padrão de um seletor de intervalo de datas, sem arrastar) — tocar num nível já
 * dentro do intervalo atual volta a fechá-lo nesse nível sozinho. `min`/`max` nulos = "todos", nenhuma pílula cheia.
 */
@Composable
fun RangePillRow(
    min: Int?,
    max: Int?,
    onRangeChange: (min: Int, max: Int) -> Unit,
    modifier: Modifier = Modifier,
    niveis: IntRange = 1..5,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        niveis.forEach { nivel ->
            val selecionado = min != null && max != null && nivel in min..max
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(shape)
                    .background(if (selecionado) Burgundy else Color.Transparent)
                    .border(1.5.dp, Burgundy, shape)
                    .clickable {
                        val novoIntervalo = when {
                            min == null || max == null -> nivel..nivel
                            nivel in min..max -> nivel..nivel
                            nivel < min -> nivel..max
                            else -> min..nivel
                        }
                        onRangeChange(novoIntervalo.first, novoIntervalo.last)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = nivel.toString(),
                    style = AquaText.Label.copy(color = if (selecionado) Color.White else Burgundy),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
