package com.example.naturemarks.data.postmark

import android.location.Location
import com.example.naturemarks.database.model.Postmark
import com.example.naturemarks.ui.model.PostmarkUiModel

object PostmarkMapper {
    fun mapToMarkEntry(markModel: PostmarkUiModel): Postmark =
        Postmark(
            imageId = markModel.imageId,
            name = markModel.name,
            description = markModel.description,
            city = markModel.city,
            latitude = markModel.location.latitude,
            longitude = markModel.location.longitude,
            timestamp = System.currentTimeMillis()
        )

    fun mapToMarkModel(mark: Postmark): PostmarkUiModel =
        PostmarkUiModel(
            imageId = mark.imageId,
            name = mark.name,
            description = mark.description,
            city = mark.city,
            location = Location("MarkLocation").apply {
                latitude = mark.latitude
                longitude = mark.longitude
            }
        )

    fun mapToMarkModel(mark: PostmarkModel) : PostmarkUiModel =
        PostmarkUiModel(
            imageId = mark.imageId,
            name = mark.name,
            description = mark.description,
            city = mark.location.city,
            location = Location("MarkLocation").apply {
                latitude = mark.location.latitude
                longitude = mark.location.longitude
            }
        )
}