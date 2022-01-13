package net.theluckycoder.stundenplan.ui

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState found!") }
