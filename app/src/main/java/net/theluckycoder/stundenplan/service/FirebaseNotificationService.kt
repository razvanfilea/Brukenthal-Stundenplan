package net.theluckycoder.stundenplan.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.ui.MainActivity

class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "New Message Received")
        val data = remoteMessage.data

        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val title = data["title"] ?: getString(R.string.app_name)
            val body = data["body"] ?: getString(R.string.new_timetable_notification)
            val link = data["url"]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel()

            val pendingIntent = if (link != null)
                getUrlPendingIntent(link)
            else
                getActivityPendingIntent()

            sendNotification(title, body, pendingIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notifications_channel_title),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun sendNotification(title: String, text: String, pendingIntent: PendingIntent) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(this, R.color.color_primary))
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)

        updateRemoteConfig()
        cleanCacheDir()
    }

    private fun updateRemoteConfig() = GlobalScope.launch(Dispatchers.IO) {
        val remoteConfig = Firebase.remoteConfig

        // Fetch the new URLs
        remoteConfig.fetch(1).await()
        Log.d(TAG, "New Remote Config Fetched")

        remoteConfig.activate().await()
        Log.d(TAG, "New Remote Config Activated")
    }

    private fun cleanCacheDir() {
        val appContext = applicationContext

        GlobalScope.launch(Dispatchers.IO) {
            MainRepository(appContext).clearCache()
        }
    }

    private fun getActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getUrlPendingIntent(url: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    companion object {
        private const val TAG = "FirebaseNotifications"
        private const val NOTIFICATION_CHANNEL_ID = "notifications"

        const val NOTIFICATION_ID = 1
    }
}
