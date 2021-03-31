package net.theluckycoder.stundenplan.repository

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.model.Timetable
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.getConfigKey
import java.io.File

class MainRepository(private val context: Context) {

    private fun getNewFile(timetable: Timetable): File {
        val dir = File(context.cacheDir, timetable.type.getConfigKey())
        dir.mkdirs()

        val name = timetable.url.substringAfterLast('/')

        return File(dir, name)
    }

    fun doesFileExist(timetable: Timetable): Boolean {
        return File(
            File(context.cacheDir, timetable.type.getConfigKey()),
            timetable.url.substringAfter('/')
        ).exists()
    }

    suspend fun getTimetable(timetableType: TimetableType): Timetable {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.fetchAndActivate().await()
        val pdfUrl = remoteConfig[timetableType.getConfigKey()].asString()

        return Timetable(timetableType, pdfUrl)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun downloadPdf(timetable: Timetable) = callbackFlow<NetworkResult> {
        val file = getNewFile(timetable).toUri()

        val request = Request(timetable.url, file).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        val listener = object : AbstractFetchListener() {
            override fun onCancelled(download: Download) {
                sendBlocking(NetworkResult.Failed(NetworkResult.FailReason.DownloadFailed))
                close()
            }

            override fun onCompleted(download: Download) {
                sendBlocking(NetworkResult.Success(download.fileUri))
                close()
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                sendBlocking(NetworkResult.Failed(NetworkResult.FailReason.DownloadFailed))
                close()
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                sendBlocking(NetworkResult.Loading(false, download.progress))
            }

            override fun onRemoved(download: Download) {
                close()
            }

            override fun onDeleted(download: Download) {
                close()
            }
        }

        val fetch = Fetch.getInstance(FetchConfiguration.Builder(context).build())

        fetch.addListener(listener)
        fetch.enqueue(request, { }) { error -> error.throwable?.let { throw it } }

        awaitClose {
            fetch.removeListener(listener)
            fetch.close()
        }
    }

    fun getLastFile(timetableType: TimetableType): File? {
        val dir = File(context.cacheDir, timetableType.getConfigKey())
        dir.mkdirs()

        val files = dir.listFiles()
        if (files.isNullOrEmpty())
            return null

        return files.asSequence()
            .filterNotNull()
            .sortedByDescending { it.lastModified() }
            .firstOrNull()
    }

    fun clearCache() {
        File(context.cacheDir, FirebaseConstants.KEY_HIGH_SCHOOL).deleteRecursively()
        File(context.cacheDir, FirebaseConstants.KEY_MIDDLE_SCHOOL).deleteRecursively()
    }
}