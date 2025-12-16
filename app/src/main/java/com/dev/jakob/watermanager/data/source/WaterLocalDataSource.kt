package com.dev.jakob.watermanager.data.source

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.google.gson.Gson // Gson wird nicht mehr direkt für SharedPreferences-Migration benötigt, aber vielleicht noch für andere Zwecke? Wenn nicht, kann es entfernt werden.
import kotlinx.coroutines.flow.Flow

/**
 * Handles local data access using Room for structured data (Water, Container)
 * and DataStore for preferences (dailyGoal, bodyWeight).
 *
 * @property waterDao The Room DAO for Water entities.
 * @property containerDao The Room DAO for Container entities.
 * @property settingsDataStore The DataStore for user settings.
 */
class WaterLocalDataSource(
    private val waterDao: WaterDao,
    private val containerDao: ContainerDao,
    private val settingsDataStore: SettingsDataStore,
    private val gson: Gson // Behalten, falls es noch woanders verwendet wird, sonst entfernen
) {
    // --- Room-Zugriffe ---

    // Water
    fun getAllWaterIntake(): Flow<List<Water>> = waterDao.getAllWaterIntake()
    suspend fun insertWater(water: Water) = waterDao.insertWater(water)
    suspend fun deleteWater(water: Water) = waterDao.deleteWater(water)

    // Containers
    fun getAllContainers(): Flow<List<Container>> = containerDao.getAllContainers()
    suspend fun insertContainer(container: Container) = containerDao.insertContainer(container)
    suspend fun deleteContainer(container: Container) = containerDao.deleteContainer(container)

    // --- DataStore-Zugriffe ---

    // Settings
    fun getDailyGoal(): Flow<Int> = settingsDataStore.dailyGoal
    suspend fun saveDailyGoal(goal: Int) = settingsDataStore.saveDailyGoal(goal)

    fun getBodyWeight(): Flow<Int> = settingsDataStore.bodyWeight
    suspend fun saveBodyWeight(weight: Int) = settingsDataStore.saveBodyWeight(weight)
}
