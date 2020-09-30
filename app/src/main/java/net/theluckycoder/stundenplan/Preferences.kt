package net.theluckycoder.stundenplan

import android.content.Context
import androidx.core.content.edit

class Preferences(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    var useDarkTheme: Boolean
        get() = sharedPreferences.getBoolean(DARK_THEME, false)
        set(value) = sharedPreferences.edit { putBoolean(DARK_THEME, value) }

    private var isHighSchool: Boolean
        get() = sharedPreferences.getBoolean(SCHOOL_TYPE, true)
        set(value) = sharedPreferences.edit { putBoolean(SCHOOL_TYPE, value) }

    var timetableType: TimetableType
        get() = if (isHighSchool) TimetableType.HIGH_SCHOOL else TimetableType.MIDDLE_SCHOOL
        set(value) {
            isHighSchool = (value == TimetableType.HIGH_SCHOOL)
        }

    companion object {
        private const val DARK_THEME = "dark_theme"
        private const val SCHOOL_TYPE = "is_high_school"
    }
}
