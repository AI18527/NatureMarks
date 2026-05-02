package com.example.naturemarks.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.OutputStream
import java.io.File

object BitmapHelper {
    fun Bitmap.toMutableBitmap(): Bitmap {
        return this.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun Bitmap.toJpegStream(stream: OutputStream, quality: Int = 100) {
        this.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    }

    fun fileToBitmap(context: Context, file: File): Bitmap {
        val uri = Uri.fromFile(file)
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source)
    }

    fun drawableToBitmap(
        context: Context,
        @DrawableRes drawableRes: Int
    ): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
            ?: throw IllegalArgumentException("Drawable not found: $drawableRes")

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val sizePx = (180 * Resources.getSystem().displayMetrics.density).toInt()

        val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight

        val width = sizePx
        val height = (sizePx / ratio).toInt()

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    fun overlayMark(
        photo: Bitmap,
        mark: Bitmap
    ): Bitmap? {
        val result = photo.config?.let {
            createBitmap(photo.width, photo.height, it)
        }

        val canvas = result?.let { Canvas(it) }
        canvas?.drawBitmap(photo, 0f, 0f, null)

        val density = Resources.getSystem().displayMetrics.density
        val paddingPx = (24 * density)
        val left = photo.width - mark.width - paddingPx
        val top = photo.height - mark.height - paddingPx

        canvas?.drawBitmap(mark, left, top, null)

        return result
    }
}