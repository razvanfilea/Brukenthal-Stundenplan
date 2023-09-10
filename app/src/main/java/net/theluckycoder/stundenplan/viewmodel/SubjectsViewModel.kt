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

    val subjectsFlow = repository.subjects
    private val _totalAveragesStateFlow = MutableStateFlow(0f)
    val totalAveragesStateFlow = _totalAveragesStateFlow.asStateFlow()

    val showCreateSubjectDialog = mutableStateOf(false)
    val showEditSubjectDialog = mutableStateOf<Subject?>(null)
    val showDeleteSubjectDialog = mutableStateOf<Subject?>(null)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            subjectsFlow.collectLatest { subjects ->
                ensureActive()

                _totalAveragesStateFlow.value = subjects.map { subject -> subject.gradesAverage }
                    .filter { it != 0 }
                    .average()
                    .toFloat()
                    .round2Decimals()
            }
        }
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