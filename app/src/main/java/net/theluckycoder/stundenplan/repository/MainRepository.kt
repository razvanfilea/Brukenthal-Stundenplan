package net.theluckycoder.stundenplan.repository

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import net.theluckycoder.stundenplan.model.Timetable
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.getConfigKey
import retrofit2.Retrofit
import retrofit2.create
import java.io.File

class MainRepository(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .build()

    private val downloadApi = retrofit.create<DownloadApi>()

    private fun getTimetableName(timetable: Timetable) =
        timetable.url.substringAfterLast('/')

    private fun getNewFile(timetable: Timetable): File {
        val dir = File(context.cacheDir, timetable.type.getConfigKey())
        if (!dir.exists())
            dir.mkdirs()

        return File(dir, getTimetableName(timetable))
    }

    fun doesFileExist(timetable: Timetable): Boolean {
        return File(
            File(context.cacheDir, timetable.type.getConfigKey()),
            getTimetableName(timetable)
        ).exists()
    }

    suspend fun getTimetable(timetableType: TimetableType): Timetable {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.fetchAndActivate().await()
        val pdfUrl = remoteConfig[timetableType.getConfigKey()].asString()

        return Timetable(timetableType, pdfUrl)
    }

    suspend fun downloadPdf(timetable: Timetable) = flow<NetworkResult> {
        val file = getNewFile(timetable)

        try {
            downloadApi.download(timetable.url).byteStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Fail(NetworkResult.Fail.Reason.DownloadFailed))
        }
    }

    fun getLastFile(timetableType: TimetableType): File? {
        val dir = File(context.cacheDir, timetableType.getConfigKey())
        dir.mkdirs()

        val files = dir.listFiles() ?: emptyArray()

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
