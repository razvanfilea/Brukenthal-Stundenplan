package net.theluckycoder.brukplan.grades.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.theluckycoder.brukplan.grades.model.Subject

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subject ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subject: Subject)

    @Delete
    fun delete(subject: Subject)
}
