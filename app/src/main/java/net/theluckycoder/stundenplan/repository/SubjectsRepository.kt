package net.theluckycoder.stundenplan.repository

import android.content.Context
import net.theluckycoder.brukplan.grades.database.AppDatabase
import net.theluckycoder.brukplan.grades.model.Subject

class SubjectsRepository(context: Context) {

    private val dao = AppDatabase.getDao(context)

    val subjects = dao.getAllFlow()

    fun insert(subject: Subject) = dao.insert(subject)

    fun delete(subject: Subject) = dao.delete(subject)

}
