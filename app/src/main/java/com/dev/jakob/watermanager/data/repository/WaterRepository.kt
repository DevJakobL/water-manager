package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource

/**
 * Repository for managing all water-related data for the application.
 * It acts as a single source of truth and abstracts the data source from the rest of the app.
 *
 * **Note for future improvement:** The methods in this repository perform synchronous I/O operations.
 * They should be converted to `suspend` functions to be executed on a background thread using coroutines.
 *
 * @property waterLocalDataSource The local data source used to access stored water data.
 */
class WaterRepository(private val waterLocalDataSource: WaterLocalDataSource) {

    /**
     * Loads the list of predefined water containers from the local data source.
     *
     * **Improvement suggestion:** This method should return an immutable `List<Container>`
     * to prevent modification from outside the repository.
     *
     * @return A mutable list of [Container] objects.
     */
    fun loadContainers(): MutableList<Container> {
        return waterLocalDataSource.loadContainers()
    }

    /**
     * Saves a list of water containers to the local data source.
     *
     * @param containers The list of [Container] objects to be saved.
     */
    fun saveContainers(containers: List<Container>) {
        waterLocalDataSource.saveContainers(containers)
    }

    /**
     * Saves the list of water intake entries to the local data source.
     *
     * @param waterList The list of [Water] objects to be saved.
     */
    fun saveWaterIntake(waterList: List<Water>) {
        waterLocalDataSource.saveWaterIntake(waterList)
    }

    /**
     * Loads the list of water intake entries from the local data source.
     *
     * @return The list of [Water] objects, or an empty list if no data is stored.
     */
    fun loadWaterIntake(): List<Water> {
        return waterLocalDataSource.loadWaterIntake()
    }
}
