package net.theluckycoder.stundenplan.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import net.theluckycoder.stundenplan.BuildConfig

class UpdateChecker(onUpdateNeeded: () -> Unit) {

    init {
        val remoteConfig = Firebase.remoteConfig
        val latestVersion = remoteConfig.getLong(KEY_CURRENT_VERSION).toInt()

        if (latestVersion > BuildConfig.VERSION_CODE) {
            Log.v(UpdateChecker::class.java.name, "New Update available: $latestVersion")
            onUpdateNeeded()
        }
    }

    companion object {
        const val KEY_CURRENT_VERSION = "latest_version"
    }
}
