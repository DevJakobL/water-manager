package com.dev.jakob.watermanager.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water

@Database(entities = [Water::class, Container::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
    abstract fun containerDao(): ContainerDao
}
