package pt.aquavitae.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk

/**
 * A linha "N BEBIDAS ............ ORDENAR: X ▾" por cima das listas (catálogo e catálogo do produtor): a contagem à
 * esquerda e, à direita, o menu com as opções de ordenação. Genérica no tipo da opção para não depender de nenhum enum.
 */
@Composable
fun <T> ContagemEOrdenacao(
    contagem: String,
    atual: T,
    opcoes: List<T>,
    rotulo: (T) -> String,
    onSelecionar: (T) -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = contagem, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp), modifier = Modifier.weight(1f))
        Box {
            Text(
                text = "ORDENAR: ${rotulo(atual).uppercase()} ▾",
                style = AquaText.Footer.copy(color = Burgundy, fontSize = 11.sp),
                modifier = Modifier.clickable { aberto = true },
            )
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                opcoes.forEach { opcao ->
                    DropdownMenuItem(text = { Text(rotulo(opcao)) }, onClick = { onSelecionar(opcao); aberto = false })
                }
            }
        }
    }
}
