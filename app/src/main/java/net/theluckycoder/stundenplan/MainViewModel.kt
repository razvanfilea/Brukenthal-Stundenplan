package net.theluckycoder.stundenplan

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch.Impl.getInstance
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("EXPERIMENTAL_API_USAGE")
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private val isDownloading = AtomicBoolean(false)
    private val preferences = Preferences(getApplication())

    private val stateLiveData = MutableLiveData<Result>()

    init {
        val timetableType = preferences.timetableType

        viewModelScope.launch(Dispatchers.IO) {
            firebaseMessaging.subscribeToTopic(TOPIC_ALL)

            firebaseMessaging.subscribeToTopic(
                if (timetableType == TimetableType.HIGH_SCHOOL) TOPIC_HIGH_SCHOOL else TOPIC_MIDDLE_SCHOOL
            )
        }

        getLastFile(timetableType)?.toUri()?.let {
            Result.Success(it)
        }

        reload(timetableType)
    }

    fun getStateLiveData(): LiveData<Result> = stateLiveData

    fun switchTimetableType() = viewModelScope.launch(Dispatchers.IO) {
        val timetableType = preferences.timetableType
        val newTimetableType =
            if (timetableType == TimetableType.HIGH_SCHOOL) TimetableType.MIDDLE_SCHOOL else TimetableType.HIGH_SCHOOL
        preferences.timetableType = newTimetableType

        when (newTimetableType) {
            TimetableType.HIGH_SCHOOL -> {
                firebaseMessaging.subscribeToTopic(TOPIC_HIGH_SCHOOL)
                firebaseMessaging.unsubscribeFromTopic(TOPIC_MIDDLE_SCHOOL)
            }
            TimetableType.MIDDLE_SCHOOL -> {
                firebaseMessaging.subscribeToTopic(TOPIC_MIDDLE_SCHOOL)
                firebaseMessaging.unsubscribeFromTopic(TOPIC_HIGH_SCHOOL)
            }
        }

        reload(newTimetableType)
    }

    fun reload(timetableType: TimetableType = preferences.timetableType) = viewModelScope.launch {
        if (isDownloading.get())
            return@launch
        isDownloading.set(true)

        val remoteConfig = Firebase.remoteConfig
        stateLiveData.value = Result.Loading(true, 0)

        try {
            val successful = remoteConfig.fetchAndActivate().await()
            if (!successful)
                Log.i(PDF_TAG, "Remote Config fetch not successful")

            val pdfUrl = remoteConfig[timetableType.getUrl()].asString()
            check(pdfUrl.isNotBlank())
            Log.i(PDF_TAG, "Url: $pdfUrl")

            downloadPdf(timetableType, pdfUrl).flowOn(Dispatchers.IO).collect {
                stateLiveData.value = it
            }

            Log.i(PDF_TAG, "Finished Loading")
        } catch (e: Exception) {
            stateLiveData.value = Result.Failed()
            Log.e(PDF_TAG, "Failed to download", e)
        }

        isDownloading.set(false)
    }

    private suspend fun downloadPdf(timetableType: TimetableType, url: String) = callbackFlow<Result> {
        val file = getNewFile(timetableType).toUri()

        val request = Request(url, file).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        val listener = object : AbstractFetchListener() {
            override fun onCancelled(download: Download) {
                sendBlocking(Result.Failed())
                close()
            }

            override fun onCompleted(download: Download) {
                sendBlocking(Result.Success(download.fileUri))
                close()
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                sendBlocking(Result.Failed())
                close()
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                sendBlocking(Result.Loading(false, download.progress))
            }

            override fun onRemoved(download: Download) {
                close()
            }

            override fun onDeleted(download: Download) {
                close()
            }
        }

        val fetch = getInstance(
            FetchConfiguration.Builder(getApplication())
                .setDownloadConcurrentLimit(3)
                .build()
        )

        fetch.addListener(listener)
        fetch.enqueue(request, { }) { error -> error.throwable?.let { throw it } }

        awaitClose {
            fetch.removeListener(listener)
            fetch.close()
        }
    }

    private fun getNewFile(timetableType: TimetableType): File {
        val dir = File(getApplication<Application>().applicationContext.cacheDir, timetableType.getUrl())
        dir.mkdirs()

        val minutes = System.currentTimeMillis() / 1000 / 60
        return File(dir, "$minutes.pdf")
    }

    private fun getLastFile(timetableType: TimetableType): File? {
        val dir = File(getApplication<Application>().applicationContext.cacheDir, timetableType.getUrl())
        dir.mkdirs()

        val files = dir.listFiles()
        if (files.isNullOrEmpty())
            return null

        return files.asSequence()
            .filterNotNull()
            .sortedDescending()
            .firstOrNull()
    }

    private companion object {
        private const val PDF_TAG = "PDF Load"

        private const val TOPIC_ALL = "all"
        private const val TOPIC_HIGH_SCHOOL = "high_school"
        private const val TOPIC_MIDDLE_SCHOOL = "middle_school"
    }
}
