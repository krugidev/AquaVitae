package pt.aquavitae.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ErrorRed

/**
 * Campo de texto dos ecrãs de design: só uma linha grená por baixo e o rótulo a negrito. O rótulo ocupa o lugar do
 * texto e, quando o campo tem foco ou texto, sobe e encolhe (como no Material) para não desaparecer enquanto se escreve.
 */
@Composable
fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var focused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val lineColor = if (isError) ErrorRed else Burgundy

    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            FloatingLabel(
                label = label,
                floated = focused || value.isNotEmpty(),
                color = lineColor,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Row(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
                    singleLine = true,
                    textStyle = AquaText.Field,
                    cursorBrush = SolidColor(Burgundy),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                )
                if (isPassword) {
                    // O olho não recebe o foco do teclado: senão a tecla "Seguinte" parava nele em vez de ir para o campo seguinte.
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(28.dp).focusProperties { canFocus = false },
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Esconder a password" else "Mostrar a password",
                            tint = Burgundy,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.5.dp, color = lineColor)
    }
}

/**
 * O rótulo de um campo: ocupa o lugar do texto e, quando `floated` (o campo tem foco ou valor), sobe e encolhe (como no
 * Material) para não desaparecer. Partilhado pelos campos que não são de escrever (ex.: a nacionalidade).
 */
@Composable
internal fun FloatingLabel(label: String, floated: Boolean, color: Color, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (floated) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "labelFloat",
    )
    Text(
        text = label,
        style = AquaText.Label.copy(color = color),
        modifier = modifier.graphicsLayer {
            val scale = 1f - 0.27f * progress
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0.5f)
            translationY = -14.dp.toPx() * progress
        },
    )
}
