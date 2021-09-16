package net.theluckycoder.stundenplan.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import net.theluckycoder.stundenplan.BuildConfig

object UpdateChecker {

    fun isUpdateNeeded(): Boolean {
        val remoteConfig = Firebase.remoteConfig
        val latestVersion = remoteConfig.getLong(KEY_CURRENT_VERSION).toInt()

        val result = latestVersion > BuildConfig.VERSION_CODE
        if (result) {
            Log.v(UpdateChecker::class.java.name, "New Update available: $latestVersion")
        }

        return result
    }

    const val KEY_CURRENT_VERSION = "latest_version"
}
