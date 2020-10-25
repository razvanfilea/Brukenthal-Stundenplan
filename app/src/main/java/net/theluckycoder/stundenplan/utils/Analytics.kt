package net.theluckycoder.stundenplan.utils

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import net.theluckycoder.stundenplan.TimetableType

object Analytics {

    private val firebase by lazy { Firebase.analytics }

    fun refreshEvent(timetableType: TimetableType) {
        val bundle = Bundle()
        bundle.putString(
            "timetable_type", when (timetableType) {
                TimetableType.HIGH_SCHOOL -> "High School"
                TimetableType.MIDDLE_SCHOOL -> "Middle School"
            }
        )
        firebase.logEvent(REFRESH_EVENT, bundle)
    }

    fun openNotificationEvent() {
        firebase.logEvent(OPEN_NOTIFICATION_EVENT, null)
    }

    private const val REFRESH_EVENT = "timetable_refresh"
    private const val OPEN_NOTIFICATION_EVENT = "open_notification"
}
