package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing all water-related data for the application.
 * It acts as a single source of truth and abstracts the data source from the rest of the app.
 * It ensures that all I/O operations are performed off the main thread using coroutines.
 *
 * @property waterLocalDataSource The local data source used to access stored water data.
 */
class WaterRepository(private val waterLocalDataSource: WaterLocalDataSource) {

    /**
     * Loads the list of predefined water containers from the local data source on a background thread.
     *
     * @return An immutable list of [Container] objects.
     */
    suspend fun loadContainers(): List<Container> {
        return withContext(Dispatchers.IO) {
            waterLocalDataSource.loadContainers().toList() // Convert to immutable list
        }
    }

    /**
     * Saves a list of water containers to the local data source on a background thread.
     *
     * @param containers The list of [Container] objects to be saved.
     */
    suspend fun saveContainers(containers: List<Container>) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveContainers(containers)
        }
    }

    /**
     * Saves the list of water intake entries to the local data source on a background thread.
     *
     * @param waterList The list of [Water] objects to be saved.
     */
    suspend fun saveWaterIntake(waterList: List<Water>) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveWaterIntake(waterList)
        }
    }

    /**
     * Loads the list of water intake entries from the local data source on a background thread.
     *
     * @return The list of [Water] objects, or an empty list if no data is stored.
     */
    suspend fun loadWaterIntake(): List<Water> {
        return withContext(Dispatchers.IO) {
            waterLocalDataSource.loadWaterIntake()
        }
    }

    /**
     * Deletes a specific water intake entry from the local data source on a background thread.
     *
     * @param water The [Water] object to be deleted.
     */
    suspend fun deleteWaterIntake(water: Water) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.deleteWaterIntake(water)
        }
    }

    /**
     * Saves the daily water goal to the local data source on a background thread.
     *
     * @param goal The daily water goal in milliliters.
     */
    suspend fun saveDailyGoal(goal: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveDailyGoal(goal)
        }
    }

    /**
     * Loads the daily water goal from the local data source on a background thread.
     *
     * @return The daily water goal in milliliters.
     */
    suspend fun loadDailyGoal(): Int {
        return withContext(Dispatchers.IO) {
            waterLocalDataSource.loadDailyGoal()
        }
    }

    /**
     * Saves the user's body weight to the local data source on a background thread.
     *
     * @param weight The body weight in kilograms.
     */
    suspend fun saveBodyWeight(weight: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveBodyWeight(weight)
        }
    }

    /**
     * Loads the user's body weight from the local data source on a background thread.
     *
     * @return The body weight in kilograms.
     */
    suspend fun loadBodyWeight(): Int {
        return withContext(Dispatchers.IO) {
            waterLocalDataSource.loadBodyWeight()
        }
    }
}
