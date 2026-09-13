package pt.aquavitae.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WinePrimary,
    secondary = GrapeSecondary,
    tertiary = Gold,
    background = BackgroundLight,
)

private val DarkColors = darkColorScheme(
    primary = WinePrimaryDark,
    secondary = GrapeSecondaryDark,
    tertiary = GoldDark,
    background = BackgroundDark,
)

@Composable
fun AquaVitaeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AquaVitaeTypography,
        content = content,
    )
}
