package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.TimetableType
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.utils.AppPreferences
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.app
import net.theluckycoder.stundenplan.utils.getConfigKey
import net.theluckycoder.stundenplan.utils.getFirebaseTopic
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MainRepository(app)
    private val isDownloading = AtomicBoolean(false)
    private val preferences = AppPreferences(app)

    private val stateLiveData = MutableLiveData<NetworkResult>()

    init {
        val timetableType = preferences.timetableType

        viewModelScope.launch(Dispatchers.IO) {
            with(Firebase.messaging) {
                subscribeToTopic(FirebaseConstants.TOPIC_ALL)
                subscribeToTopic(timetableType.getFirebaseTopic())
            }
        }

        // Load last file first, then attempt to download a new one
        // It's very likely that the last downloaded PDF is also the most recent one
        repository.getLastFile(timetableType)?.toUri()?.let {
            NetworkResult.Success(it, preferences.useDarkTheme)
        }

        reload(timetableType)
    }

    fun getStateLiveData(): LiveData<NetworkResult> = stateLiveData

    fun switchTheme(): Boolean {
        val newValue = !preferences.useDarkTheme
        preferences.useDarkTheme = newValue
        return newValue
    }

    fun switchTimetableType(timetableType: TimetableType) =
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.messaging.unsubscribeFromTopic(timetableType.getFirebaseTopic())

            val newTimetableType =
                if (timetableType == TimetableType.HIGH_SCHOOL) TimetableType.MIDDLE_SCHOOL else TimetableType.HIGH_SCHOOL
            Firebase.messaging.subscribeToTopic(newTimetableType.getFirebaseTopic())
            preferences.timetableType = newTimetableType

            reload(newTimetableType)
        }

    fun reload(timetableType: TimetableType = preferences.timetableType) = viewModelScope.launch {
        if (!app.isNetworkAvailable()) {
            stateLiveData.value = NetworkResult.Failed(R.string.error_network_connection)
            return@launch
        }

        if (isDownloading.get())
            return@launch

        isDownloading.set(true)

        val remoteConfig = Firebase.remoteConfig
        stateLiveData.value = NetworkResult.Loading(true, 0)

        try {
            val successful = remoteConfig.fetchAndActivate().await()
            if (!successful)
                Log.i(PDF_TAG, "Remote Config fetch not successful")

            val pdfUrl = remoteConfig[timetableType.getConfigKey()].asString()
            check(pdfUrl.isNotBlank())
            Log.i(PDF_TAG, "Url: $pdfUrl")

            repository.downloadPdf(timetableType, pdfUrl).flowOn(Dispatchers.IO).collect {
                stateLiveData.value = it
            }

            Log.i(PDF_TAG, "Finished Loading")
        } catch (e: Exception) {
            stateLiveData.value = NetworkResult.Failed(R.string.error_download_failed)
            Log.e(PDF_TAG, "Failed to download", e)
        }

        isDownloading.set(false)
    }

    private companion object {
        private const val PDF_TAG = "PDF Load"
    }
}
