package net.theluckycoder.stundenplan.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.ui.MainActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannels(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "New Message Received")
        val data = remoteMessage.data

        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $data")

            val title = data["title"] ?: getString(R.string.app_name)
            val body = data["body"] ?: getString(R.string.new_timetable_notification)
            val url = data["url"]
            val channelId = data["channel_id"]

            val pendingIntent = if (url != null)
                getUrlPendingIntent(url)
            else
                getActivityPendingIntent()

            GlobalScope.launch(Dispatchers.IO) {
                updateRemoteConfig()
                cleanCacheDir(applicationContext)
            }

            val id = if (url != null) NOTIFICATION_WITH_URL_ID else NOTIFICATION_ID

            NotificationHelper.postNotification(this, title, body, pendingIntent, id, channelId)
        }
    }

    private fun getActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getUrlPendingIntent(url: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private const val TAG = "FirebaseNotifications"

        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_WITH_URL_ID = 2

        private suspend fun updateRemoteConfig() {
            val remoteConfig = Firebase.remoteConfig

            try {
                // Fetch the new URLs
                remoteConfig.fetch(1).await()
                Log.d(TAG, "Remote Config Fetched")

                remoteConfig.activate().await()
                Log.d(TAG, "Remote Config Activated")
            } catch (e: Exception) {
                Log.e(TAG, "Remote Config Fetch/Activation Failed")
            }
        }


        private fun cleanCacheDir(applicationContext: Context) {
            MainRepository(applicationContext).clearCache()
        }
    }
}
