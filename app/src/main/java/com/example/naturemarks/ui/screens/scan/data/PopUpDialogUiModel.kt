package com.example.naturemarks.ui.screens.scan.data

data class PopUpDialogUiModel (
    val title : String,
    val text: String,
    val imageId : Int? = null,
    val textConfirm : String,
    val textDismiss : String = "",
    val haveDismiss : Boolean = false,
    val shouldPrepareForCapture : Boolean = false,
    val shouldNavigateBack : Boolean = false,
    val shouldOpenGallery : Boolean = false
)