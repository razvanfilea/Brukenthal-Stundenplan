package net.theluckycoder.stundenplan.utils

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.theluckycoder.stundenplan.model.TimetableType

class AppPreferences(private val context: Application) {

    val darkThemeFlow: Flow<Boolean> = context.appDataStore.data
        .map { preferences ->
            preferences[DARK_THEME] ?: false
        }

    suspend fun updateUseDarkTheme(useDarkTheme: Boolean) =
        context.appDataStore.edit { preferences ->
            preferences[DARK_THEME] = useDarkTheme
        }

    val timetableTypeFlow: Flow<TimetableType> = context.appDataStore.data
        .map { preferences ->
            if (preferences[TIMETABLE_TYPE] == false)
                TimetableType.MIDDLE_SCHOOL
            else
                TimetableType.HIGH_SCHOOL
        }

    suspend fun timetableType(): TimetableType = timetableTypeFlow.first()


    suspend fun updateTimetableType(timetableType: TimetableType) =
        context.appDataStore.edit { preferences ->
            preferences[TIMETABLE_TYPE] = timetableType == TimetableType.HIGH_SCHOOL
        }

    companion object {
        private const val DATA_STORE_NAME = "user_prefs"

        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val TIMETABLE_TYPE = booleanPreferencesKey("timetable_type")

        private val Context.appDataStore by preferencesDataStore(DATA_STORE_NAME)
    }
}
