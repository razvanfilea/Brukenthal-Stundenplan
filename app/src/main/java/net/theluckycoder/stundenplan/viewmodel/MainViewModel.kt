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
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.TimetableType
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.utils.AppPreferences
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.app
import net.theluckycoder.stundenplan.utils.getConfigKey
import net.theluckycoder.stundenplan.utils.getFirebaseTopic
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MainRepository(app)
    private val preferences = AppPreferences(app)
    private val isDownloading = AtomicBoolean(false)

    private val stateData = MutableLiveData<NetworkResult>()

    val darkThemeData = preferences.darkThemeFlow.asLiveData()
    val timetableTypeData = preferences.timetableTypeFlow.asLiveData()

    fun getStateLiveData(): LiveData<NetworkResult> = stateData

    fun switchTheme(useDarkTheme: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferences.updateUseDarkTheme(useDarkTheme)
    }

    fun switchTimetableType(newTimetableType: TimetableType) =
        viewModelScope.launch(Dispatchers.IO) {
            val oldTimetableType =
                if (newTimetableType == TimetableType.HIGH_SCHOOL) TimetableType.MIDDLE_SCHOOL else TimetableType.HIGH_SCHOOL
            Firebase.messaging.unsubscribeFromTopic(oldTimetableType.getFirebaseTopic())

            Firebase.messaging.subscribeToTopic(newTimetableType.getFirebaseTopic())
            preferences.updateTimetableType(newTimetableType)

            reload(newTimetableType)
        }

    fun preload(timetableType: TimetableType) = viewModelScope.launch {
        val fileUri = withContext(Dispatchers.IO) { repository.getLastFile(timetableType)?.toUri() }

        if (fileUri != null)
            NetworkResult.Success(fileUri)
    }

    fun reload(timetableType: TimetableType) = viewModelScope.launch {
        if (!app.isNetworkAvailable()) {
            stateData.value = NetworkResult.Failed(R.string.error_network_connection)
            return@launch
        }

        if (isDownloading.get())
            return@launch

        isDownloading.set(true)

        val remoteConfig = Firebase.remoteConfig
        stateData.value = NetworkResult.Loading(true, 0)

        try {
            val successful = remoteConfig.fetchAndActivate().await()
            if (!successful)
                Log.i(PDF_TAG, "Remote Config fetch not successful")

            val pdfUrl = remoteConfig[timetableType.getConfigKey()].asString()
            check(pdfUrl.isNotBlank())
            Log.i(PDF_TAG, "Url: $pdfUrl")

            repository.downloadPdf(timetableType, pdfUrl).flowOn(Dispatchers.IO).collect {
                stateData.value = it
            }

            Log.i(PDF_TAG, "Finished Loading")
        } catch (e: Exception) {
            stateData.value = NetworkResult.Failed(R.string.error_download_failed)
            Log.e(PDF_TAG, "Failed to download", e)
        }

        isDownloading.set(false)
    }

    private companion object {
        private const val PDF_TAG = "PDF Load"
    }
}
