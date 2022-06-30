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

    fun getNewVersionUrl(): String {
        val remoteConfig = Firebase.remoteConfig
        val latestVersion = remoteConfig.getLong(KEY_CURRENT_VERSION).toInt()

        val patch = latestVersion % 10
        val minor = (latestVersion / 10) % 10
        val major = (latestVersion / 100) % 10

        return GITHUB_RELEASES_URL + "v$major.$minor.$patch"
    }

    const val KEY_CURRENT_VERSION = "latest_version"
    private const val GITHUB_RELEASES_URL = "https://github.com/TheLuckyCoder/Brukenthal-Stundenplan/releases/tag/"
}
