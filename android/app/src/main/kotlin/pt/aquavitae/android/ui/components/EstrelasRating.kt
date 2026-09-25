package pt.aquavitae.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.Burgundy

/** Que ícone leva a estrela `posicao` (1 a 5) para uma nota: cheia, meia ou vazia. Uma nota de 4,5 dá 4 cheias e 1 meia. */
enum class TipoEstrela { Cheia, Meia, Vazia }

fun tipoDaEstrela(nota: Double, posicao: Int): TipoEstrela = when {
    nota >= posicao -> TipoEstrela.Cheia
    nota >= posicao - 0.5 -> TipoEstrela.Meia
    else -> TipoEstrela.Vazia
}

/** As 5 estrelas de uma nota, com meias estrelas (4,5 → ★★★★½), como no mockup de "As minhas reviews". Só mostra, não se toca. */
@Composable
fun EstrelasRating(nota: Double, modifier: Modifier = Modifier, tamanho: Dp = 14.dp) {
    Row(modifier) {
        for (posicao in 1..5) {
            val icone = when (tipoDaEstrela(nota, posicao)) {
                TipoEstrela.Cheia -> Icons.Filled.Star
                TipoEstrela.Meia -> Icons.Filled.StarHalf
                TipoEstrela.Vazia -> Icons.Outlined.StarBorder
            }
            Icon(imageVector = icone, contentDescription = null, tint = Burgundy, modifier = Modifier.size(tamanho))
        }
    }
}
