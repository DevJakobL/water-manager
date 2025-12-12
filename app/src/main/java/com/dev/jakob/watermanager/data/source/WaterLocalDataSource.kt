package com.dev.jakob.watermanager.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water // Import hinzugefügt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * [WaterLocalDataSource] ist für den lokalen Datenzugriff zuständig,
 * insbesondere für das Speichern und Laden von Wasserdaten über [SharedPreferences].
 *
 * @param context Der Android-Kontext, der für den Zugriff auf [SharedPreferences] benötigt wird.
 * @param gson Eine [Gson]-Instanz für die Serialisierung und Deserialisierung von Objekten.
 */
class WaterLocalDataSource(context: Context, private val gson: Gson) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Lädt die Liste der vordefinierten Wasserbehälter aus den [SharedPreferences].
     * Wenn keine Behälter gespeichert sind, wird eine Standardliste zurückgegeben.
     *
     * @return Eine veränderliche Liste von [Container]-Objekten.
     */
    fun loadContainers(): MutableList<Container> {
        val json = sharedPreferences.getString(KEY_CONTAINERS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Container>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Default containers
            mutableListOf(
                Container("Glass", 250),
                Container("Bottle", 500),
                Container("Large Bottle", 1000)
            )
        }
    }

    /**
     * Speichert eine Liste von Wasserbehältern in den [SharedPreferences].
     *
     * @param containers Die Liste der zu speichernden [Container]-Objekte.
     */
    fun saveContainers(containers: List<Container>) {
        val json = gson.toJson(containers)
        sharedPreferences.edit { putString(KEY_CONTAINERS, json) }
    }

    /**
     * Speichert die Liste der getrunkenen Wassermengen in den [SharedPreferences].
     *
     * @param waterList Die Liste der zu speichernden [Water]-Objekte.
     */
    fun saveWaterIntake(waterList: List<Water>) {
        val json = gson.toJson(waterList)
        sharedPreferences.edit { putString(KEY_WATER_INTAKE, json) }
    }

    /**
     * Lädt die Liste der getrunkenen Wassermengen aus den [SharedPreferences].
     *
     * @return Die Liste der [Water]-Objekte, oder eine leere Liste, wenn kein Wert gespeichert ist.
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
        private const val KEY_WATER_INTAKE = "water_intake" // Neuer Schlüssel
    }
}
