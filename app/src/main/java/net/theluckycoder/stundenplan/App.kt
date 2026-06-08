package net.theluckycoder.stundenplan

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 20 * 60 // 20 minutes
        }

        Firebase.remoteConfig.also {
            it.setConfigSettingsAsync(configSettings)

            it.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("RemoteConfig", "Remote Config Fetched Successfully")
                }
            }
        }
    }
}
