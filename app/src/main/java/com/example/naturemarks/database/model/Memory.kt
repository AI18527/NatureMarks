package com.example.naturemarks.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory",
    foreignKeys = [ForeignKey(
        entity = Postmark::class,
        parentColumns = ["imageId"],
        childColumns = ["markId"],
        onDelete = ForeignKey.CASCADE
    )])
data class Memory (
    @PrimaryKey(autoGenerate = true) val id: Int = 1,
    @ColumnInfo val markId: String,
    @ColumnInfo val photoPath: String?,
    @ColumnInfo val notes: String?,
)