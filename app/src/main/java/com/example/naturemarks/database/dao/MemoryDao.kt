package com.example.naturemarks.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.naturemarks.database.model.Memory

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory WHERE markId = :markId LIMIT 1")
    fun getMemoryByMarkId(markId: String): Memory
    @Query("INSERT INTO memory (markId) VALUES (:markId)")
    fun insertNewMemory(markId: String)
    @Query("INSERT INTO memory (markId, photoPath) VALUES (:markId, :photoPath)")
    fun insertNewMemoryWithPhoto(markId: String, photoPath: String)
    @Query("UPDATE memory SET photoPath = :photoPath WHERE id = :id")
    fun updateMemoryPhoto(id: Int, photoPath: String)
    @Query("UPDATE memory SET notes = :notes WHERE id = :id")
    fun updateMemoryNotes(id: Int, notes: String)
}