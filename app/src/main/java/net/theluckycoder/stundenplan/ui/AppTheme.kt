package net.theluckycoder.stundenplan.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private object Colors {
    val Primary = Color(0xFFffca28)
    val PrimaryVariant = Color(0xFFc79a00)
    val Secondary = Color(0xFF76ff03)
    val SecondaryVariant = Color(0xFF32cb00)
}

private val LightColors = lightColors(
    primary = Colors.Primary,
    primaryVariant = Colors.PrimaryVariant,
    secondary = Colors.Secondary,
    secondaryVariant = Colors.SecondaryVariant,
    onPrimary = Color.Black,
)

private val DarkColors = darkColors(
    primary = Colors.Primary,
    primaryVariant = Colors.PrimaryVariant,
    secondary = Colors.SecondaryVariant,
)

@Composable
fun AppTheme(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) DarkColors else LightColors
    MaterialTheme(colors = colors, content =  content)
}