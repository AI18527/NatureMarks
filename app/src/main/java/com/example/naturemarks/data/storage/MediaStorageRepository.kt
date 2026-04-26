package com.example.naturemarks.data.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.example.naturemarks.util.BitmapHelper.toJpegStream

interface PhotoStorageRepositoryInterface {
    fun savePhotoToGallery(bitmap: Bitmap): Uri?
}

class MediaStorageRepository(
    private val context: Context
): PhotoStorageRepositoryInterface {

    override fun savePhotoToGallery(bitmap: Bitmap): Uri? {
        val filename = String.format(System.currentTimeMillis().toString(), FILE_NAME_FORMAT)

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_PATH)
        }

        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            resolver.openOutputStream(it)?.use { stream ->
                bitmap.toJpegStream(stream)
            }
        }

        return uri
    }

    companion object {
        const val FILE_NAME_FORMAT = "Mark_%d.jpg"
        const val MIME_TYPE = "image/jpeg"
        const val DIRECTORY_PATH = "Pictures/NatureMarks"
    }

}