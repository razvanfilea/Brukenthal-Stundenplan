package net.theluckycoder.stundenplan.repository

import android.content.Context
import androidx.core.net.toUri
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
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.TimetableType
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.getConfigKey
import java.io.File

class MainRepository(private val context: Context) {

    private fun getNewFile(timetableType: TimetableType): File {
        val dir = File(context.cacheDir, timetableType.getConfigKey())
        dir.mkdirs()

        val minutes = System.currentTimeMillis() / 1000 / 60
        return File(dir, "$minutes.pdf")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun downloadPdf(timetableType: TimetableType, url: String) = callbackFlow<NetworkResult> {
        val file = getNewFile(timetableType).toUri()

        val request = Request(url, file).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        val listener = object : AbstractFetchListener() {
            override fun onCancelled(download: Download) {
                sendBlocking(NetworkResult.Failed(R.string.error_download_failed))
                close()
            }

            override fun onCompleted(download: Download) {
                sendBlocking(NetworkResult.Success(download.fileUri))
                close()
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                sendBlocking(NetworkResult.Failed(R.string.error_download_failed))
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

        val fetch = Fetch.getInstance(
            FetchConfiguration.Builder(context)
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

    fun getLastFile(timetableType: TimetableType): File? {
        val dir = File(context.cacheDir, timetableType.getConfigKey())
        dir.mkdirs()

        val files = dir.listFiles()
        if (files.isNullOrEmpty())
            return null

        return files.asSequence()
            .filterNotNull()
            .sortedDescending()
            .firstOrNull()
    }

    fun clearCache() {
        File(context.cacheDir, FirebaseConstants.KEY_HIGH_SCHOOL).deleteRecursively()
        File(context.cacheDir, FirebaseConstants.KEY_MIDDLE_SCHOOL).deleteRecursively()
    }
}