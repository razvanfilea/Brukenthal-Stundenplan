package net.theluckycoder.brukplan.grades.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.theluckycoder.brukplan.grades.model.Subject

@Database(entities = [Subject::class], version = 1, exportSchema = true)
@TypeConverters(DatabaseTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): SubjectDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        private fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app.db"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE!!
        }

        fun getDao(context: Context) = getInstance(context).dao()
    }
}
