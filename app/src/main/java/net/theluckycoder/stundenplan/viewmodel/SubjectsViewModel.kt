package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.theluckycoder.brukplan.grades.model.Subject
import net.theluckycoder.brukplan.grades.round2Decimals
import net.theluckycoder.stundenplan.repository.SubjectsRepository

class SubjectsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SubjectsRepository(app)

    private val _selectedSemesterStateFlow = MutableStateFlow(Subject.Semester.ONE)
    val selectedSemesterStateFlow = _selectedSemesterStateFlow.asStateFlow()

    val subjectsFlow = repository.subjects
    private val _semesterAveragesStateFlow = MutableStateFlow(0f to 0f)
    val semesterAveragesStateFlow = _semesterAveragesStateFlow.asStateFlow()

    val showCreateSubjectDialog = mutableStateOf(false)
    val showEditSubjectDialog = mutableStateOf<Subject?>(null)
    val showDeleteSubjectDialog = mutableStateOf<Subject?>(null)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            subjectsFlow.collectLatest { subjects ->
                ensureActive()

                fun semesterAverage(semester: Subject.Semester) =
                    subjects.map { subject -> subject[semester].average }
                        .filter { it != 0 }
                        .average()
                        .toFloat()
                        .round2Decimals()

                _semesterAveragesStateFlow.value =
                    semesterAverage(Subject.Semester.ONE) to semesterAverage(Subject.Semester.TWO)
            }
        }
    }

    fun setSelectedSemester(semester: Subject.Semester) {
        _selectedSemesterStateFlow.value = semester
    }

    fun insert(subject: Subject) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.insert(subject)
        }
    }

    fun delete(subject: Subject) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.delete(subject)
        }
    }
}