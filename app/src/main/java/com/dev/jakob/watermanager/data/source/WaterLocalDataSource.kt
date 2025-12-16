package com.dev.jakob.watermanager.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Handles local data access, specifically for saving and loading water-related data
 * using [SharedPreferences].
 *
 * **Note for future improvement:** This class performs synchronous I/O operations on the calling thread.
 * For better performance and to avoid blocking the main thread, these operations should be moved
 * to a background thread using coroutines (`withContext(Dispatchers.IO)`).
 * For structured data like this, migrating to a Room database is the recommended long-term solution.
 *
 * @property gson A [Gson] instance for serializing and deserializing objects.
 * @param context The Android context, required to access [SharedPreferences].
 */
class WaterLocalDataSource(context: Context, private val gson: Gson) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Loads the list of predefined water containers from [SharedPreferences].
     * If no containers are stored, it returns a default list.
     * It also ensures that all loaded containers have a unique ID, assigning one if missing.
     *
     * @return An immutable list of [Container] objects.
     */
    fun loadContainers(): List<Container> {
        val json = sharedPreferences.getString(KEY_CONTAINERS, null)
        val loadedContainers: List<Container> = if (json != null) {
            val type = object : TypeToken<List<Container>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Default containers if none are saved.
            listOf(
                Container(name = "Glass", size = 250),
                Container(name = "Bottle", size = 500),
                Container(name = "Large Bottle", size = 1000)
            )
        }

        val containersWithFixedIds = loadedContainers.map { container ->
            container
        }

        return containersWithFixedIds
    }

    /**
     * Saves a list of water containers to [SharedPreferences] as a JSON string.
     *
     * @param containers The list of [Container] objects to be saved.
     */
    fun saveContainers(containers: List<Container>) {
        val json = gson.toJson(containers)
        sharedPreferences.edit { putString(KEY_CONTAINERS, json) }
    }

    /**
     * Saves the list of water intake entries to [SharedPreferences] as a JSON string.
     *
     * @param waterList The list of [Water] objects to be saved.
     */
    fun saveWaterIntake(waterList: List<Water>) {
        val json = gson.toJson(waterList)
        sharedPreferences.edit { putString(KEY_WATER_INTAKE, json) }
    }

    /**
     * Loads the list of water intake entries from [SharedPreferences].
     *
     * @return The list of [Water] objects, or an empty list if no value is stored.
     */
    fun loadWaterIntake(): List<Water> {
        val json = sharedPreferences.getString(KEY_WATER_INTAKE, null)
        return if (json != null) {
            val type = object : TypeToken<List<Water>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    /**
     * Deletes a specific water intake entry from [SharedPreferences].
     * It loads the current list, removes the specified [Water] object, and saves the updated list.
     *
     * @param water The [Water] object to be deleted.
     */
    fun deleteWaterIntake(water: Water) {
        val currentList = loadWaterIntake().toMutableList()
        currentList.remove(water)
        saveWaterIntake(currentList)
    }

    /**
     * Saves the daily water goal to [SharedPreferences].
     *
     * @param goal The daily water goal in milliliters.
     */
    fun saveDailyGoal(goal: Int) {
        sharedPreferences.edit { putInt(KEY_DAILY_GOAL, goal) }
    }

    /**
     * Loads the daily water goal from [SharedPreferences].
     *
     * @return The daily water goal in milliliters, or a default value of 2000 if not set.
     */
    fun loadDailyGoal(): Int {
        return sharedPreferences.getInt(KEY_DAILY_GOAL, 2000) // Default to 2000ml
    }

    /**
     * Saves the user's body weight to [SharedPreferences].
     *
     * @param weight The body weight in kilograms.
     */
    fun saveBodyWeight(weight: Int) {
        sharedPreferences.edit { putInt(KEY_BODY_WEIGHT, weight) }
    }

    /**
     * Loads the user's body weight from [SharedPreferences].
     *
     * @return The body weight in kilograms, or a default value of 70 if not set.
     */
    fun loadBodyWeight(): Int {
        return sharedPreferences.getInt(KEY_BODY_WEIGHT, 70) // Default to 70kg
    }

    companion object {
        private const val PREFS_NAME = "WaterManagerPrefs"
        private const val KEY_CONTAINERS = "containers"
        private const val KEY_WATER_INTAKE = "water_intake"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_BODY_WEIGHT = "body_weight"
    }
}
