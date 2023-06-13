package net.theluckycoder.stundenplan.ui.pdf_rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import java.io.File
import java.io.IOException

/**
 * Decodes and renders a given rect out of a [PdfRenderer.Page] into a [Bitmap]
 */
class PDFRegionDecoder
/**
 * basic constructor for PDFDecoder.
 * @param position:the current position in the pdf
 * @param file: the pdf-file
 * @param scale: the scale factor
 */(
    /**
     * the current page position in the pdf
     */
    private val position: Int,
    /**
     * the pdf file
     */
    private val file: File,
    /**
     * defines the initial scale of the picture
     */
    private val scale: Float,
    private val darkMode: Boolean
) : ImageRegionDecoder {
    /**
     * the page that will be rendered to a bitmap.
     */
    private lateinit var renderer: PdfRenderer

    /**
     * the pdf page
     */
    private lateinit var page: PdfRenderer.Page

    /**
     * the file descriptor
     */
    private lateinit var descriptor: ParcelFileDescriptor

    /**
     * Initializes the region decoder. This method initializes
     * @param context not used here
     * @param uri not used here (file is already loaded)
     * @return the rescaled point
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun init(context: Context, uri: Uri): Point {
        descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(descriptor)
        page = renderer.openPage(position)
        return Point(
            (page.width * scale + 0.5f).toInt(),
            (page.height * scale + 0.5f).toInt()
        )
    }

    /**
     * Creates a [Bitmap] in the correct size and renders the region defined by rect of the
     * [PdfRenderer.Page] into it.
     *
     * @param rect the rect of the [PdfRenderer.Page] to be rendered to the bitmap
     * @param sampleSize the sample size
     * @return a bitmap containing the rendered rect of the page
     */
    override fun decodeRegion(rect: Rect, sampleSize: Int): Bitmap {
        val bitmapWidth = rect.width() / sampleSize
        val bitmapHeight = rect.height() / sampleSize
        val bitmap = Bitmap.createBitmap(
            bitmapWidth, bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
        val matrix = Matrix()
        matrix.setScale(scale / sampleSize, scale / sampleSize)
        matrix.postTranslate(
            (-rect.left / sampleSize).toFloat(),
            (-rect.top / sampleSize).toFloat()
        )
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        if (darkMode) {
            bitmap.inversePixelColors()
        }

        return bitmap
    }

    override fun isReady(): Boolean {
        return true
    }

    /**
     * close everything
     */
    override fun recycle() {
        page.close()
        renderer.close()
        try {
            descriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}