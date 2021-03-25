package net.theluckycoder.stundenplan.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.theluckycoder.stundenplan.TimetableType

private val Context.appDataStore by preferencesDataStore(AppPreferences.DATA_STORE_NAME)

class AppPreferences(private val context: Context) {

    val darkThemeFlow: Flow<Boolean> = context.appDataStore.data
        .map { preferences ->
            preferences[DARK_THEME] ?: false
        }.distinctUntilChanged()

    suspend fun updateUseDarkTheme(useDarkTheme: Boolean) = context.appDataStore.edit { preferences ->
        preferences[DARK_THEME] = useDarkTheme
    }

    val timetableTypeFlow: Flow<TimetableType> = context.appDataStore.data
        .map { preferences ->
            if (preferences[TIMETABLE_TYPE] == false)
                TimetableType.MIDDLE_SCHOOL
            else
                TimetableType.HIGH_SCHOOL
        }.distinctUntilChanged()

    suspend fun updateTimetableType(timetableType: TimetableType) = context.appDataStore.edit { preferences ->
        preferences[TIMETABLE_TYPE] = timetableType == TimetableType.HIGH_SCHOOL
    }

    companion object {
        const val DATA_STORE_NAME = "user_prefs"

        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val TIMETABLE_TYPE = booleanPreferencesKey("timetable_type")
    }
}
