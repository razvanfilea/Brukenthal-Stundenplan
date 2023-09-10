package net.theluckycoder.brukplan.grades.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import net.theluckycoder.brukplan.grades.model.Subject

@Database(
    entities = [Subject::class],
    version = 3,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = AppDatabase.MigrationVersion1To2::class
        )
    ],
    exportSchema = true
)
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

    @DeleteColumn(tableName = "subject", columnName = "sem1_semesterPaper")
    @DeleteColumn(tableName = "subject", columnName = "sem2_semesterPaper")
    class MigrationVersion1To2 : AutoMigrationSpec {
        @Override
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            // Invoked once auto migration is done
        }
    }

}
