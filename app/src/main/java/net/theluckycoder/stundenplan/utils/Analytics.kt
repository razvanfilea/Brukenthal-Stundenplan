package net.theluckycoder.stundenplan.utils

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object Analytics {

    private val firebase by lazy { Firebase.analytics }

    fun openNotificationEvent() {
        firebase.logEvent(OPEN_NOTIFICATION_EVENT, null)
    }

    private const val OPEN_NOTIFICATION_EVENT = "open_notification"
}
