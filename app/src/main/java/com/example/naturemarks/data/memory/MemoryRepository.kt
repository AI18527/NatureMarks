package com.example.naturemarks.data.memory

import com.example.naturemarks.database.AppDatabase
import com.example.naturemarks.database.model.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface MemoryRepositoryInterface {
    suspend fun getMemoryByMarkId(markId: String): Memory
    fun addMemory(markId: String)
    suspend fun updateMemoryPhoto(id: Int, photoPath: String)
    suspend fun updateMemoryNotes(id: Int, notes: String)
}
class MemoryRepository(private val db: AppDatabase): MemoryRepositoryInterface {
    override suspend fun getMemoryByMarkId(markId: String): Memory =
        withContext(Dispatchers.IO) {
            db.memoryDao().getMemoryByMarkId(markId)
        }

    override fun addMemory(markId: String) {
        db.memoryDao().insertNewMemory(markId)
    }

    override suspend fun updateMemoryPhoto(id: Int, photoPath: String) {
        withContext(Dispatchers.IO) {
            db.memoryDao().updateMemoryPhoto(id, photoPath)
        }
    }

    override suspend fun updateMemoryNotes(id: Int, notes: String) {
        withContext(Dispatchers.IO) {
            db.memoryDao().updateMemoryNotes(id, notes)
        }
    }
}