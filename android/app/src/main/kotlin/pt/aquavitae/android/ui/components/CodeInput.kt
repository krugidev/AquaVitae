package pt.aquavitae.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ErrorRed

private val CodeBoxShape = RoundedCornerShape(20.dp)

/**
 * O campo do código de recuperação: uma caixa arredondada com `length` espaços sublinhados, um por dígito. É um único campo
 * de texto por baixo (teclado numérico, dá para colar o código do email); os dígitos desenham-se nos espaços. Pede o
 * foco ao aparecer, para o teclado abrir logo.
 */
@Composable
fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onDone: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val lineColor = if (isError) ErrorRed else Burgundy

    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(length)) },
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .border(1.5.dp, lineColor, CodeBoxShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                // O campo verdadeiro fica por baixo, invisível: recebe o foco e o teclado; quem se vê são os espaços.
                Box(Modifier.matchParentSize().alpha(0f)) { innerTextField() }
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    repeat(length) { index ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = value.getOrNull(index)?.toString().orEmpty(),
                                style = AquaText.CodeDigit,
                                modifier = Modifier.height(34.dp),
                            )
                            HorizontalDivider(thickness = if (index == value.length) 3.dp else 1.5.dp, color = lineColor)
                        }
                    }
                }
            }
        },
    )
}
