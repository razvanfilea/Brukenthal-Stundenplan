package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
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

    private var lastPdfRenderer: PdfRenderer? = null

    private val timetableStateFlow = MutableStateFlow(TimetableType.HIGH_SCHOOL)
    val timetableFlow = timetableStateFlow.asStateFlow()

    private val networkStateFlow = MutableStateFlow<NetworkResult?>(null)
    val networkFlow = networkStateFlow.asStateFlow()

    val darkThemeFlow = preferences.darkThemeFlow

    val hasSeenUpdateDialog = mutableStateOf(false)
    val showAppBar = mutableStateOf(true)

    // region Mutexes

    private val pdfRendererMutex = Mutex()
    private val refreshMutex = Mutex()

    // endregion Mutexes

    init {
        viewModelScope.launch {
            launch {
                subscribeToFirebaseTopics()
            }

            launch {
                val lastTimetable = preferences.timetableType()
                timetableStateFlow.value = lastTimetable
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
                networkStateFlow.value =
                    NetworkResult.Fail(NetworkResult.Fail.Reason.MissingNetworkConnection)
            }
        }
    }

    fun switchTheme(useDarkTheme: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferences.updateUseDarkTheme(useDarkTheme)
    }

    fun switchTimetableType(newTimetableType: TimetableType) {
        timetableStateFlow.value = newTimetableType
        refresh()

        viewModelScope.launch(Dispatchers.Default) {
            // Remove the old pdfRenderer
            pdfRendererMutex.withLock {
                lastPdfRenderer?.close()
                lastPdfRenderer = null
            }

            preferences.updateTimetableType(newTimetableType)
        }
    }

    suspend fun renderPdf(
        width: Int,
        zoom: Int = 1,
        darkMode: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {

        val scaledWidth = (width * zoom)

        val bitmap = pdfRendererMutex.withLock {
            val pdfRenderer = lastPdfRenderer ?: withContext(Dispatchers.IO) {
                getNewPdfRenderer(timetableStateFlow.value)
            }

            lastPdfRenderer = pdfRenderer

            ensureActive()

            pdfRenderer.openPage(0).use { page ->
                val bitmap = Bitmap.createBitmap(
                    scaledWidth, (scaledWidth.toFloat() / page.width * page.height).toInt(),
                    Bitmap.Config.ARGB_8888
                )

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                bitmap
            }
        }

        Log.d("Pdf Render", "Rendered Bitmap (${bitmap.width}, ${bitmap.height}); Zoom $zoom; DarkMode $darkMode")

        if (darkMode) {
            val length = bitmap.width * bitmap.height
            val pixels = IntArray(length)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            for (i in 0 until length) {
                val c = pixels[i]
                // Invert all the colors that aren't transparent
                if (Color.alpha(c) != 0) {
                    pixels[i] = Color.argb(
                        Color.alpha(c),
                        255 - Color.red(c),
                        255 - Color.green(c),
                        255 - Color.blue(c)
                    )
                }
            }

            bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        }

        bitmap
    }

    private fun getNewPdfRenderer(type: TimetableType): PdfRenderer {
        val file = repository.getLastFile(type)
        val pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY)
        return PdfRenderer(pfd)
    }

    private suspend fun downloadTimetable() = coroutineScope {
        val type = timetableFlow.value

        // Let the user know that we are starting the download
        networkStateFlow.value = NetworkResult.Loading()

        try {
            val timetable = repository.getTimetable(type)
            check(timetable.url.isNotBlank()) { "No PDF url link found" }

            Log.i(PDF_TAG, "Url: ${timetable.url}")

            if (!repository.doesFileExist(timetable)) {
                repository.downloadPdf(timetable)
                    .flowOn(Dispatchers.IO)
                    .collect { networkResult ->
                        ensureActive()
                        networkStateFlow.value = networkResult
                    }
            } else {
                networkStateFlow.value = NetworkResult.Success()
            }

            Log.i(PDF_TAG, "Finished downloading")
        } catch (e: Exception) {
            Log.e(PDF_TAG, "Failed to download for $type", e)

            networkStateFlow.value =
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
