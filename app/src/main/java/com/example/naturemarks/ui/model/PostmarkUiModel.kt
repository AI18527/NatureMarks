package com.example.naturemarks.ui.model

import android.location.Location


data class PostmarkUiModel (
    val imageId: String,
    val name: String,
    val description: String,
    val city: String,
    val location : Location
)