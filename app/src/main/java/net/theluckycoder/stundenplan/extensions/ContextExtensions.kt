package net.theluckycoder.stundenplan.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri

fun Context.isNetworkAvailable(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = manager.activeNetworkInfo
    var connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
    if (!connected) {
        connected = manager.allNetworkInfo.any { it.isConnected }
    }
    return connected
}

fun Context.browseUrl(url: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}
