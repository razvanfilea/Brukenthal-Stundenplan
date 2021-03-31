package net.theluckycoder.stundenplan.extensions

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

val AndroidViewModel.app: Application
    get() = getApplication()
