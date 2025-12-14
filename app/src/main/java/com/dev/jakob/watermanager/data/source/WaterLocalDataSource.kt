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
     *
     * @return A mutable list of [Container] objects. Should be changed to return an immutable list.
     */
    fun loadContainers(): MutableList<Container> {
        val json = sharedPreferences.getString(KEY_CONTAINERS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Container>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Default containers if none are saved
            mutableListOf(
                Container("Glass", 250),
                Container("Bottle", 500),
                Container("Large Bottle", 1000)
            )
        }
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

    companion object {
        private const val PREFS_NAME = "WaterManagerPrefs"
        private const val KEY_CONTAINERS = "containers"
        private const val KEY_WATER_INTAKE = "water_intake"
    }
}
