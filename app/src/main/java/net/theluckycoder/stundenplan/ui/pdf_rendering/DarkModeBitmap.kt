package net.theluckycoder.stundenplan.ui.pdf_rendering

import android.graphics.Bitmap
import android.graphics.Color

fun Bitmap.inversePixelColors() {
    val length = width * height
    val pixels = IntArray(length)
    getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in 0 until length) {
        val c = pixels[i]
        // Invert all the colors that aren't transparent
        val alpha = Color.alpha(c)
        if (alpha != 0) {
            pixels[i] = Color.argb(
                alpha,
                255 - Color.red(c),
                255 - Color.green(c),
                255 - Color.blue(c)
            )
        }
    }

    setPixels(pixels, 0, width, 0, 0, width, height)
}