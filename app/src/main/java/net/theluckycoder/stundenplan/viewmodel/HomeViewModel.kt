package net.theluckycoder.stundenplan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.repository.MainRepository
import net.theluckycoder.stundenplan.utils.AppPreferences
import net.theluckycoder.stundenplan.utils.FirebaseConstants
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import android.graphics.ColorMatrix
import android.graphics.Matrix


class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MainRepository(app)
    private val preferences = AppPreferences(app)

    private val pdfRendererDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val lastPdfRenderer = AtomicReference<PdfRenderer?>(null)

    private val timetableStateFlow = MutableStateFlow(TimetableType.HIGH_SCHOOL)
    val timetableFlow = timetableStateFlow.asStateFlow()

    val darkThemeFlow = preferences.darkThemeFlow

    init {
        viewModelScope.launch {
            launch {
                subscribeToFirebaseTopics()
            }

            timetableStateFlow.value = preferences.timetableTypeFlow.first()
        }
    }

    fun switchTheme(useDarkTheme: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferences.updateUseDarkTheme(useDarkTheme)
    }

    fun switchTimetableType(newTimetableType: TimetableType) =
        viewModelScope.launch {
            // Remove the old pdfRenderer
            val pdfRenderer = lastPdfRenderer.getAndSet(null)
            pdfRenderer?.close()

            timetableStateFlow.value = newTimetableType

            preferences.updateTimetableType(newTimetableType)
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun renderPdfAsync(
        width: Int,
        height: Int,
        zoom: Float,
        xOffset: Int = 0, // TODO
        yOffset: Int = 0,
        darkMode: Boolean = false
    ): Bitmap =
        withContext(pdfRendererDispatcher) {
            val pdfRenderer = lastPdfRenderer.get() ?: let {
                val pfd = ParcelFileDescriptor
                    .open(repository.getLastFile(timetableStateFlow.value), MODE_READ_ONLY)
                PdfRenderer(pfd)
            }
            lastPdfRenderer.set(pdfRenderer)

            val scaledWidth = (width * zoom).roundToInt()
//            val scaledHeight = (height * zoom).roundToInt()

            val page = pdfRenderer.openPage(0)

            val bitmap = Bitmap.createBitmap(
                scaledWidth, (scaledWidth.toFloat() / page.width * page.height).toInt(),
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            bitmap
        }

    private fun subscribeToFirebaseTopics() {
        with(Firebase.messaging) {
            subscribeToTopic(FirebaseConstants.TOPIC_ALL)

            if (BuildConfig.DEBUG)
                subscribeToTopic(FirebaseConstants.TOPIC_TEST)
        }
    }

    private companion object {
        private val invertedColorFilter =
            ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
    }
}
