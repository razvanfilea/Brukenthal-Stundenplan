package net.theluckycoder.stundenplan.extensions

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.app: Application
    get() = getApplication()

fun Offset.coerceIn(min: Offset, max: Offset) = Offset(
    x = x.coerceIn(min.x, max.x),
    y = y.coerceIn(min.y, max.y),
)
