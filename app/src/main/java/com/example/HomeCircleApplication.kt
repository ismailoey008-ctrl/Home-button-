package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.SettingsRepository

class HomeCircleApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "home_circle.db"
        ).fallbackToDestructiveMigration().build()

        repository = SettingsRepository(database.settingsDao(), applicationContext)
    }

    companion object {
        lateinit var instance: HomeCircleApplication
            private set
    }
}
