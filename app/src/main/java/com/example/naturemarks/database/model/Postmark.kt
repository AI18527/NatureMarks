package com.example.naturemarks.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Postmark (
    @PrimaryKey val imageId: String,
    @ColumnInfo val name: String,
    @ColumnInfo val description: String,
    @ColumnInfo val city: String,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longitude: Double,
    @ColumnInfo val timestamp: Long
)