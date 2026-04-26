package com.example.naturemarks.app

import android.app.Application
import androidx.room.Room
import com.example.naturemarks.database.AppDatabase

class NatureMarksApplication: Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nature-marks"
        ).build()
    }
}