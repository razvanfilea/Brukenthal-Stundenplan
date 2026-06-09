package net.theluckycoder.stundenplan.ui

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


val primaryAppColor = Color(0xFFef6c00)

private val primaryLight = Color(0xFF8C4E29)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFFDBCA)
private val onPrimaryContainerLight = Color(0xFF703714)
private val secondaryLight = Color(0xFF765848)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFFFDBCA)
private val onSecondaryContainerLight = Color(0xFF5C4132)
private val tertiaryLight = Color(0xFF28638A)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFCAE6FF)
private val onTertiaryContainerLight = Color(0xFF004B70)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFF8F6)
private val onBackgroundLight = Color(0xFF221A15)
private val surfaceLight = Color(0xFFFFF8F6)
private val onSurfaceLight = Color(0xFF221A15)
private val surfaceVariantLight = Color(0xFFF4DED4)
private val onSurfaceVariantLight = Color(0xFF52443D)
private val outlineLight = Color(0xFF85746B)
private val outlineVariantLight = Color(0xFFD7C2B9)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF382E29)
private val inverseOnSurfaceLight = Color(0xFFFFEDE6)
private val inversePrimaryLight = Color(0xFFFFB68F)
private val surfaceDimLight = Color(0xFFE8D7CF)
private val surfaceBrightLight = Color(0xFFFFF8F6)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFFF1EB)
private val surfaceContainerLight = Color(0xFFFCEAE3)
private val surfaceContainerHighLight = Color(0xFFF6E5DD)
private val surfaceContainerHighestLight = Color(0xFFF0DFD8)

private val primaryDark = Color(0xFFFFB68F)
private val onPrimaryDark = Color(0xFF532201)
private val primaryContainerDark = Color(0xFF703714)
private val onPrimaryContainerDark = Color(0xFFFFDBCA)
private val secondaryDark = Color(0xFFE6BEAB)
private val onSecondaryDark = Color(0xFF432B1D)
private val secondaryContainerDark = Color(0xFF5C4132)
private val onSecondaryContainerDark = Color(0xFFFFDBCA)
private val tertiaryDark = Color(0xFF96CCF8)
private val onTertiaryDark = Color(0xFF00344F)
private val tertiaryContainerDark = Color(0xFF004B70)
private val onTertiaryContainerDark = Color(0xFFCAE6FF)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF1A120D)
private val onBackgroundDark = Color(0xFFF0DFD8)
private val surfaceDark = Color(0xFF1A120D)
private val onSurfaceDark = Color(0xFFF0DFD8)
private val surfaceVariantDark = Color(0xFF52443D)
private val onSurfaceVariantDark = Color(0xFFD7C2B9)
private val outlineDark = Color(0xFF9F8D84)
private val outlineVariantDark = Color(0xFF52443D)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF0DFD8)
private val inverseOnSurfaceDark = Color(0xFF382E29)
private val inversePrimaryDark = Color(0xFF8C4E29)
private val surfaceDimDark = Color(0xFF1A120D)
private val surfaceBrightDark = Color(0xFF413732)
private val surfaceContainerLowestDark = Color(0xFF140C09)
private val surfaceContainerLowDark = Color(0xFF221A15)
private val surfaceContainerDark = Color(0xFF271E19)
private val surfaceContainerHighDark = Color(0xFF322823)
private val surfaceContainerHighestDark = Color(0xFF3D332E)

private val lightScheme = lightColorScheme(
    primary = primaryAppColor,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val colorSpec = spring<Color>(stiffness = Spring.StiffnessLow)

@Composable
private fun appMaterialColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    val context = LocalContext.current
    val target = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> darkScheme
        else -> lightScheme
    }

    return target.copy(
        primary = animateColorAsState(target.primary, colorSpec).value,
        onPrimary = animateColorAsState(target.onPrimary, colorSpec).value,
        primaryContainer = animateColorAsState(target.primaryContainer, colorSpec).value,
        onPrimaryContainer = animateColorAsState(target.onPrimaryContainer, colorSpec).value,
        inversePrimary = animateColorAsState(target.inversePrimary, colorSpec).value,
        secondary = animateColorAsState(target.secondary, colorSpec).value,
        onSecondary = animateColorAsState(target.onSecondary, colorSpec).value,
        secondaryContainer = animateColorAsState(target.secondaryContainer, colorSpec).value,
        onSecondaryContainer = animateColorAsState(target.onSecondaryContainer, colorSpec).value,
        tertiary = animateColorAsState(target.tertiary, colorSpec).value,
        onTertiary = animateColorAsState(target.onTertiary, colorSpec).value,
        tertiaryContainer = animateColorAsState(target.tertiaryContainer, colorSpec).value,
        onTertiaryContainer = animateColorAsState(target.onTertiaryContainer, colorSpec).value,
        background = animateColorAsState(target.background, colorSpec).value,
        onBackground = animateColorAsState(target.onBackground, colorSpec).value,
        surface = animateColorAsState(target.surface, colorSpec).value,
        onSurface = animateColorAsState(target.onSurface, colorSpec).value,
        surfaceVariant = animateColorAsState(target.surfaceVariant, colorSpec).value,
        onSurfaceVariant = animateColorAsState(target.onSurfaceVariant, colorSpec).value,
        surfaceTint = animateColorAsState(target.surfaceTint, colorSpec).value,
        inverseSurface = animateColorAsState(target.inverseSurface, colorSpec).value,
        inverseOnSurface = animateColorAsState(target.inverseOnSurface, colorSpec).value,
        error = animateColorAsState(target.error, colorSpec).value,
        onError = animateColorAsState(target.onError, colorSpec).value,
        errorContainer = animateColorAsState(target.errorContainer, colorSpec).value,
        onErrorContainer = animateColorAsState(target.onErrorContainer, colorSpec).value,
        outline = animateColorAsState(target.outline, colorSpec).value,
        outlineVariant = animateColorAsState(target.outlineVariant, colorSpec).value,
        scrim = animateColorAsState(target.scrim, colorSpec).value,
    )
}

@Composable
fun AppTheme(
    isDark: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = appMaterialColorScheme(isDark, dynamicColor)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
