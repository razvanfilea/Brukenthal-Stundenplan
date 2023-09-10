package net.theluckycoder.stundenplan.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun VerticallyAnimatedNumber(
    targetState: Number,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedVisibilityScope.(targetState: Number) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            if (targetState.toDouble() > initialState.toDouble()) {
                // If the target number is larger, it slides up and fades in
                // while the initial (smaller) number slides up and fades out.
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                // If the target number is smaller, it slides down and fades in
                // while the initial number slides down and fades out.
                slideInVertically { height -> -height } + fadeIn() togetherWith
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        },
        contentAlignment = contentAlignment,
        content = content,
        label = "number_animation"
    )
}
