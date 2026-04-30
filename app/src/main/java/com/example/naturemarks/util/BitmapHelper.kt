package com.example.naturemarks.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.OutputStream
import androidx.core.graphics.scale

object BitmapHelper {
    fun Bitmap.toMutableBitmap(): Bitmap {
        return this.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun Bitmap.toJpegStream(stream: OutputStream, quality: Int = 100) {
        this.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    }
    fun drawableToBitmap(
        context: Context,
        @DrawableRes drawableRes: Int
    ): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
            ?: throw IllegalArgumentException("Drawable not found: $drawableRes")

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val sizePx = (screenWidth * 0.8f).toInt()

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

        // Position postmark
        val left = photo.width - mark.width - 32f
        val top = photo.height - mark.height - 32f

        canvas?.drawBitmap(mark, left, top, null)

        return result
    }
}