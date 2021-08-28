package net.theluckycoder.stundenplan.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFFffca28)
    val PrimaryVariant = Color(0xFFc79a00)
    val Secondary = Color(0xFF76ff03)
    val SecondaryVariant = Color(0xFF32cb00)
}

private val LightColors = lightColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryVariant,
    secondary = AppColors.Secondary,
    secondaryVariant = AppColors.SecondaryVariant,
    onPrimary = Color.Black,
)

private val DarkColors = darkColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryVariant,
    secondary = AppColors.SecondaryVariant,
)

@Composable
fun AppTheme(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) DarkColors else LightColors
    MaterialTheme(colors = colors, content =  content)
}