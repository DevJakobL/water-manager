package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WaterRepository(private val waterLocalDataSource: WaterLocalDataSource) {

    fun getAllWaterIntake(): Flow<List<Water>> = waterLocalDataSource.getAllWaterIntake()

    suspend fun insertWater(water: Water) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.insertWater(water)
        }
    }

    suspend fun deleteWater(water: Water) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.deleteWater(water)
        }
    }

    fun getAllContainers(): Flow<List<Container>> = waterLocalDataSource.getAllContainers()

    suspend fun insertContainer(container: Container) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.insertContainer(container)
        }
    }

    suspend fun deleteContainer(container: Container) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.deleteContainer(container)
        }
    }

    fun getDailyGoal(): Flow<Int> = waterLocalDataSource.getDailyGoal()

    suspend fun saveDailyGoal(goal: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveDailyGoal(goal)
        }
    }

    fun getBodyWeight(): Flow<Int> = waterLocalDataSource.getBodyWeight()

    suspend fun saveBodyWeight(weight: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveBodyWeight(weight)
        }
    }
}
