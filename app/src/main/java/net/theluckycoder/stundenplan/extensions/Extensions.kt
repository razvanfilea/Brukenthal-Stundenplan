package net.theluckycoder.stundenplan.extensions

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.app: Application
    get() = getApplication()

