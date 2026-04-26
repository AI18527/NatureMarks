package com.example.naturemarks.data.postmark

import kotlinx.serialization.Serializable

@Serializable
data class PostmarkModel (
    val imageId: String,
    val name: String,
    val description: String,
    val location: MarkLocation
)

@Serializable
data class MarkLocation(
    val city: String,
    val latitude: Double,
    val longitude: Double
)