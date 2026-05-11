package io.shareit.transfer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SurpriseColorScheme = darkColorScheme(
    primary = LovePink,
    onPrimary = Cream,
    secondary = Gold,
    onSecondary = MidnightDeep,
    tertiary = Lavender,
    onTertiary = MidnightDeep,
    background = MidnightDeep,
    onBackground = Cream,
    surface = MidnightPurple,
    onSurface = Cream,
    primaryContainer = Plum,
    onPrimaryContainer = Champagne,
)

private val ShareItLightScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF546E7A),
    onSecondary = Color.White,
    background = Color(0xFFE3F2FD),
    onBackground = Color(0xFF263238),
    surface = Color.White,
    onSurface = Color(0xFF263238),
)

@Composable
fun ShareItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShareItLightScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun SurpriseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SurpriseColorScheme,
        typography = Typography,
        content = content
    )
}
