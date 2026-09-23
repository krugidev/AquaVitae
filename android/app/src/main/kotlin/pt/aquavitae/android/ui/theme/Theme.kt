package pt.aquavitae.android.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Só existe tema claro: o design da app é claro (cartões cinzentos sobre fundo branco), por isso a app não segue o
// modo escuro do sistema — um modo escuro teria de ser desenhado de propósito.
private val LightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    secondary = Burgundy,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    error = ErrorRed,
    onError = Color.White,
    outline = Burgundy,
)

@Composable
fun AquaVitaeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AquaVitaeTypography,
    ) {
        // Cursor e seleção de texto na cor da marca.
        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = Burgundy,
                backgroundColor = Burgundy.copy(alpha = 0.25f),
            ),
            content = content,
        )
    }
}
