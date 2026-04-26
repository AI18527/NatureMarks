package com.example.naturemarks.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File

class CameraHelper(private val context: Context) {

    fun takePicture(
        imageCapture: ImageCapture,
        onResult: (Bitmap) -> Unit
    ) {
        val file = createTempFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri ?: return
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    onResult(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun createTempFile(context: Context): File {
        return File.createTempFile("photo_", ".jpg", context.cacheDir)
    }
}