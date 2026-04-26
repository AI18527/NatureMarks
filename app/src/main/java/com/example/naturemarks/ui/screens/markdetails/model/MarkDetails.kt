package com.example.naturemarks.ui.screens.markdetails.model

import android.net.Uri
import com.example.naturemarks.database.model.Postmark

data class MarkDetails(
    val postmark: Postmark,
    val memoryId: Int,
    val notes: String?,
    val photoUri: Uri?
)