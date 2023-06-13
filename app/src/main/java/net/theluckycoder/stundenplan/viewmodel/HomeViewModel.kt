package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.extensions.app
import net.theluckycoder.stundenplan.extensions.isNetworkAvailable
import net.theluckycoder.stundenplan.model.NetworkResult
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.utils.AppPreferences
import net.theluckycoder.stundenplan.utils.FirebaseConstants

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MainRepository(app)
    private val preferences = AppPreferences(app)

    private val _timetableStateFlow = MutableStateFlow(TimetableType.HIGH_SCHOOL)
    val timetableStateFlow = _timetableStateFlow.asStateFlow()

    private val _networkStateFlow = MutableStateFlow<NetworkResult?>(null)
    val networkStateFlow = _networkStateFlow.asStateFlow()

    val darkThemeFlow = preferences.darkThemeFlow
    val hasFinishedScaffoldTutorialFlow = preferences.hasFinishedScaffoldTutorialFlow

    val hasSeenUpdateDialogState = mutableStateOf(false)
    val showAppBarState = mutableStateOf(true)

    val timetableFile = timetableStateFlow
        .combine(networkStateFlow) { file, _ ->
            file
        }.map {
            repository.getLastFile(it)
        }

    // region Mutexes

    private val refreshMutex = Mutex()

    // endregion Mutexes

    init {
        viewModelScope.launch {
            launch { subscribeToFirebaseTopics() }

            launch {
                val lastTimetable = preferences.timetableType()
                _timetableStateFlow.value = lastTimetable
                refresh()
            }
        }
    }

    fun refresh() = viewModelScope.launch(Dispatchers.Default) {
        refreshMutex.withLock {
            val isNetworkAvailable = app.isNetworkAvailable()

            if (isNetworkAvailable) {
                downloadTimetable()
            } else {
                // Let the user know that we can't download a newer timetable
                _networkStateFlow.value =
                    NetworkResult.Fail(NetworkResult.Fail.Reason.MissingNetworkConnection)
            }
        }
    }

    fun switchTheme(useDarkTheme: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferences.updateUseDarkTheme(useDarkTheme)
    }

    fun switchTimetableType(newTimetableType: TimetableType) {
        _timetableStateFlow.value = newTimetableType
        refresh()

        viewModelScope.launch(Dispatchers.IO) {
            preferences.updateTimetableType(newTimetableType)
        }
    }

    fun finishedScaffoldTutorial() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.finishedScaffoldTutorial()
        }
    }

    private suspend fun downloadTimetable() = coroutineScope {
        val type = timetableStateFlow.value

        // Let the user know that we are starting the download
        _networkStateFlow.value = NetworkResult.Loading()

        try {
            val timetable = repository.getTimetable(type)
            check(timetable.url.isNotBlank()) { "No PDF url link found" }

            Log.i(PDF_TAG, "Url: ${timetable.url}")

            if (!repository.doesFileExist(timetable)) {
                repository.downloadPdf(timetable)
                    .flowOn(Dispatchers.IO)
                    .collect { networkResult ->
                        ensureActive()
                        _networkStateFlow.value = networkResult
                    }
            } else {
                _networkStateFlow.value = NetworkResult.Success()
            }

            Log.i(PDF_TAG, "Finished downloading")
        } catch (e: Exception) {
            Log.e(PDF_TAG, "Failed to download for $type", e)

            _networkStateFlow.value =
                NetworkResult.Fail(NetworkResult.Fail.Reason.DownloadFailed)
        }
    }

    private fun subscribeToFirebaseTopics() {
        with(Firebase.messaging) {
            subscribeToTopic(FirebaseConstants.TOPIC_ALL)

            if (BuildConfig.DEBUG)
                subscribeToTopic(FirebaseConstants.TOPIC_TEST)
        }
    }

    private companion object {
        private const val PDF_TAG = "PDF Load"
    }
}
