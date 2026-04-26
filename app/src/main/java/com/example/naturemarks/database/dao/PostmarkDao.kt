package com.example.naturemarks.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.naturemarks.database.model.Postmark

@Dao
interface PostmarkDao {
    @Query("SELECT * FROM postmark")
    fun getAll(): List<Postmark>?

    @Query("SELECT * FROM postmark WHERE imageId = :imageId LIMIT 1")
    fun getMarkById(imageId: String): Postmark

    @Insert
    suspend fun insert(vararg postmark: Postmark)
}