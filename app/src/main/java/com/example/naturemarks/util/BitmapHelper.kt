package com.example.naturemarks.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

object BitmapHelper {
    fun getBitmapFromImage(context: Context, drawable: Int): Bitmap {
        val db = ContextCompat.getDrawable(context, drawable)
        val bit = createBitmap(db!!.intrinsicWidth, db.intrinsicHeight)
        val canvas = Canvas(bit)
        db.setBounds(0, 0, canvas.width, canvas.height)
        db.draw(canvas)

        return bit
    }
}