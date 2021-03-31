package net.theluckycoder.stundenplan.utils

import net.theluckycoder.stundenplan.model.TimetableType

object FirebaseConstants {
    const val KEY_HIGH_SCHOOL = "url_high_school"
    const val KEY_MIDDLE_SCHOOL = "url_middle_school"

    const val TOPIC_ALL = "all"
    const val TOPIC_TEST = "test"
    const val TOPIC_HIGH_SCHOOL = "high_school"
    const val TOPIC_MIDDLE_SCHOOL = "middle_school"
}

fun TimetableType.getConfigKey() = when (this) {
    TimetableType.HIGH_SCHOOL -> FirebaseConstants.KEY_HIGH_SCHOOL
    TimetableType.MIDDLE_SCHOOL -> FirebaseConstants.KEY_MIDDLE_SCHOOL
}
