package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.utils.AppPreferences
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.extensions.app

/**
 * https://developer.android.com/jetpack/guide
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MainRepository(app)
    private val preferences = AppPreferences(app)

    private val stateData = MutableLiveData<NetworkResult>()
    private var downloadJob: Job? = null

    val darkTheme = preferences.darkThemeFlow.asLiveData()
    val networkState: LiveData<NetworkResult> get() = stateData

    var hasSeenUpdateDialog = false

    init {
        try {
            subscribeToFirebaseTopics()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelScope.launch {
            preferences.timetableTypeFlow.collectLatest {
                ensureActive()
                refresh(it)
            }
        }
    }

    fun switchTheme(useDarkTheme: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferences.updateUseDarkTheme(useDarkTheme)
    }

    fun switchTimetableType(newTimetableType: TimetableType) =
        viewModelScope.launch(Dispatchers.IO) {
            preferences.updateTimetableType(newTimetableType)
        }

    suspend fun timetableType(): TimetableType = preferences.timetableType()

    private fun loadLastTimetable(timetableType: TimetableType) = viewModelScope.launch {
        val fileUri = withContext(Dispatchers.IO) { repository.getLastFile(timetableType)?.toUri() }

        if (fileUri != null)
            stateData.value = NetworkResult.Success(fileUri)
    }

    fun refresh(timetableType: TimetableType? = null, force: Boolean = false) =
        viewModelScope.launch {
            downloadJob?.cancelAndJoin()

            downloadJob = viewModelScope.launch {
                val type =
                    timetableType ?: withContext(Dispatchers.IO) { preferences.timetableType() }

                val isNetworkAvailable = app.isNetworkAvailable()
                val preloadJob = loadLastTimetable(type)

                if (isNetworkAvailable) {
                    // Let the user know that we are starting to download a new timetable
                    stateData.value = NetworkResult.Loading(true, 0)

                    try {
                        val timetable = repository.getTimetable(type)
                        check(timetable.url.isNotBlank()) { "No PDF url link found" }
                        Log.i(PDF_TAG, "Url: ${timetable.url}")

                        // Show the pre-existing PDF before
                        preloadJob.join()

                        if (force || !repository.doesFileExist(timetable)) {
                            repository.downloadPdf(timetable)
                                .flowOn(Dispatchers.IO)
                                .collect { networkResult ->
                                    ensureActive()
                                    stateData.postValue(networkResult)
                                }
                        }

                        Log.i(PDF_TAG, "Finished downloading")
                    } catch (e: Exception) {
                        stateData.value = NetworkResult.Failed(NetworkResult.FailReason.DownloadFailed)
                        Log.e(PDF_TAG, "Failed to download", e)
                    }
                } else {
                    preloadJob.join() // Only load the last downloaded one

                    // Let the user know that we can't download a newer timetable
                    stateData.value = NetworkResult.Failed(NetworkResult.FailReason.MissingNetworkConnection)
                }
            }
        }

    private fun subscribeToFirebaseTopics() {
        with(Firebase.messaging) {
            subscribeToTopic(FirebaseConstants.TOPIC_ALL)

            if (BuildConfig.DEBUG)
                subscribeToTopic(FirebaseConstants.TOPIC_TEST)

            unsubscribeFromTopic(FirebaseConstants.TOPIC_HIGH_SCHOOL)
            unsubscribeFromTopic(FirebaseConstants.TOPIC_MIDDLE_SCHOOL)
        }
    }

    private companion object {
        private const val PDF_TAG = "PDF Load"
    }
}
