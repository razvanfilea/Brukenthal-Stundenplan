package net.theluckycoder.stundenplan

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 2 * 60
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
    }
}
