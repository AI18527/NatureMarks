package com.example.naturemarks.data.postmark

import com.example.naturemarks.database.model.Postmark

object PostmarkMapper {
    fun mapToMarkEntry(markModel: PostmarkModel): Postmark =
        Postmark(
            imageId = markModel.imageId,
            name = markModel.name,
            description = markModel.description,
            city = markModel.location.city,
            latitude = markModel.location.latitude,
            longitude = markModel.location.longitude,
            timestamp = System.currentTimeMillis()
        )

    fun mapToMarkModel(mark: Postmark): PostmarkModel =
        PostmarkModel(
            imageId = mark.imageId,
            name = mark.name,
            description = mark.description,
            location = MarkLocation(
                city = mark.city,
                latitude = mark.latitude,
                longitude = mark.longitude
            )
        )
}
