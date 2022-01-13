package net.theluckycoder.stundenplan.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFFef6c00)
    val PrimaryVariant = Color(0xFFb53d00)
    val SecondaryLight = Color(0xFF00b0ff)
    val Secondary = Color(0xFF00b0ff)
    val SecondaryVariant = Color(0xFF0081cb)
}

private val LightColors = lightColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryVariant,
    secondary = AppColors.Secondary,
    secondaryVariant = AppColors.SecondaryVariant,
)

private val DarkColors = darkColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryVariant,
    secondary = AppColors.SecondaryVariant,
)

private val colorSpec = spring<Color>(stiffness = Spring.StiffnessLow)

@Composable
private fun appMaterialColors(useDarkTheme: Boolean): Colors {
    val target = if (useDarkTheme) DarkColors else LightColors
    return Colors(
        primary = animateColorAsState(target.primary, colorSpec).value,
        primaryVariant = animateColorAsState(target.primaryVariant, colorSpec).value,
        secondary = animateColorAsState(target.secondary, colorSpec).value,
        secondaryVariant = animateColorAsState(target.secondaryVariant, colorSpec).value,
        background = animateColorAsState(target.background, colorSpec).value,
        surface = animateColorAsState(target.surface, colorSpec).value,
        error = target.error,
        onPrimary = animateColorAsState(target.onPrimary, colorSpec).value,
        onSecondary = animateColorAsState(target.onSecondary, colorSpec).value,
        onBackground = animateColorAsState(target.onBackground, colorSpec).value,
        onSurface = animateColorAsState(target.onSurface, colorSpec).value,
        onError = target.onError,
        isLight = target.isLight
    )
}

@Composable
fun AppTheme(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = appMaterialColors(isDark), content = content)
}
