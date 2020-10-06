package net.theluckycoder.stundenplan

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 5 * 60 // 5 minutes
        }

        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)
    }
}
