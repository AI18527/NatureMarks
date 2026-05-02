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
import androidx.core.graphics.scale

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

        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    fun overlayMark(
        photo: Bitmap,
        mark: Bitmap
    ): Bitmap {
        val config = photo.config ?: Bitmap.Config.ARGB_8888
        val result = photo.copy(config, true)
        val canvas = Canvas(result)

        val metrics = Resources.getSystem().displayMetrics
        val screenWidth = metrics.widthPixels.toFloat()

        val markWidthOnScreenPx = 160 * metrics.density

        val ratio = markWidthOnScreenPx / screenWidth

        val finalMarkWidth = (photo.width * ratio).toInt()
        val finalMarkHeight = ((finalMarkWidth.toFloat() / mark.width) * mark.height).toInt()

        val paddingOnPhoto = (24 * metrics.density) * (photo.width.toFloat() / screenWidth)

        val scaledMark = mark.scale(finalMarkWidth, finalMarkHeight)

        val left = photo.width - finalMarkWidth - paddingOnPhoto
        val top = photo.height - finalMarkHeight - (2 * paddingOnPhoto)

        canvas.drawBitmap(scaledMark, left, top, null)

        return result
    }
}