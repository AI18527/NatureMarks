package com.example.naturemarks.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.naturemarks.database.dao.MemoryDao
import com.example.naturemarks.database.dao.PostmarkDao
import com.example.naturemarks.database.model.Memory
import com.example.naturemarks.database.model.Postmark

@Database(entities = [Postmark::class, Memory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postmarkDao(): PostmarkDao

    abstract fun memoryDao() : MemoryDao
}