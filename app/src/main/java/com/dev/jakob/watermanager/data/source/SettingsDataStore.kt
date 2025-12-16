package com.dev.jakob.watermanager.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val BODY_WEIGHT = intPreferencesKey("body_weight")
        val MIGRATION_COMPLETED = intPreferencesKey("migration_completed")
    }

    val dailyGoal: Flow<Int> = context.settingsDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL] ?: 2000
        }

    suspend fun saveDailyGoal(goal: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL] = goal
        }
    }

    val bodyWeight: Flow<Int> = context.settingsDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BODY_WEIGHT] ?: 70
        }

    suspend fun saveBodyWeight(weight: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.BODY_WEIGHT] = weight
        }
    }

    val migrationCompleted: Flow<Int> = context.settingsDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MIGRATION_COMPLETED] ?: 0
        }

    suspend fun setMigrationCompleted() {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MIGRATION_COMPLETED] = 1
        }
    }
}
