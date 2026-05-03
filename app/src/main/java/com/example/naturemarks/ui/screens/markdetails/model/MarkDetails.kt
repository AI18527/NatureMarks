package com.example.naturemarks.ui.screens.markdetails.model

import android.net.Uri
import com.example.naturemarks.ui.model.PostmarkUiModel

data class MarkDetails(
    val postmark: PostmarkUiModel,
    val memoryId: Int,
    val notes: String?,
    val photoUri: Uri?
)