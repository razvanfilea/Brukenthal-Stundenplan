package net.theluckycoder.stundenplan.ui.pdf_rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import java.io.File

/**
 * Decodes and renders a [PdfRenderer.Page] into a [Bitmap]
 */
class PDFDecoder(
    /**
     * the current pdf site
     */
    private val position: Int,
    /**
     * the pdf file to render
     */
    private val file: File,
    /**
     * defines the initial scale of the picture
     */
    private val scale: Float,
    private val darkMode: Boolean
) : ImageDecoder {

    /**
     * Creates a [Bitmap]in the correct size and renders the [PdfRenderer.Page] into it.
     *
     * @param context not used
     * @param uri not used
     * @return a bitmap, containing the pdf page
     * @throws Exception, if rendering fails
     */
    @Throws(Exception::class)
    override fun decode(context: Context, uri: Uri): Bitmap {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val page = renderer.openPage(position)
        val bitmap = Bitmap.createBitmap(
            (page.width * scale + 0.5).toInt(),
            (page.height * scale + 0.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )

        if (darkMode) {
            bitmap.inversePixelColors()
        }

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()
        return bitmap
    }
}