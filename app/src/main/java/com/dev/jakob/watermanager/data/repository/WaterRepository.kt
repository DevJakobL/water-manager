package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository-Klasse, die als einzige Quelle der Wahrheit für Wasser- und Containerdaten dient.
 *
 * Diese Klasse abstrahiert die Datenquellen (lokal) vom Rest der Anwendung
 * und bietet eine saubere API für den Datenzugriff.
 *
 * @param waterLocalDataSource Die lokale Datenquelle für Wasser- und Containerdaten.
 */
class WaterRepository(private val waterLocalDataSource: WaterLocalDataSource) {

    /**
     * Ruft alle Wasseraufnahmen als [Flow] ab.
     *
     * @return Ein [Flow] von einer Liste von [Water]-Objekten.
     */
    fun getAllWaterIntake(): Flow<List<Water>> = waterLocalDataSource.getAllWaterIntake()

    /**
     * Fügt eine neue Wasseraufnahme hinzu.
     *
     * @param water Das [Water]-Objekt, das hinzugefügt werden soll.
     */
    suspend fun insertWater(water: Water) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.insertWater(water)
        }
    }

    /**
     * Löscht eine Wasseraufnahme.
     *
     * @param water Das [Water]-Objekt, das gelöscht werden soll.
     */
    suspend fun deleteWater(water: Water) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.deleteWater(water)
        }
    }

    /**
     * Ruft alle Container als [Flow] ab.
     *
     * @return Ein [Flow] von einer Liste von [Container]-Objekten.
     */
    fun getAllContainers(): Flow<List<Container>> = waterLocalDataSource.getAllContainers()

    /**
     * Fügt einen neuen Container hinzu oder aktualisiert einen bestehenden.
     *
     * @param container Der [Container], der hinzugefügt oder aktualisiert werden soll.
     * @return Die ID des eingefügten oder aktualisierten Containers.
     */
    suspend fun insertContainer(container: Container): Long {
        return withContext(Dispatchers.IO) {
            waterLocalDataSource.insertContainer(container)
        }
    }

    /**
     * Löscht einen Container.
     *
     * @param container Der [Container], der gelöscht werden soll.
     */
    suspend fun deleteContainer(container: Container) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.deleteContainer(container)
        }
    }

    /**
     * Ruft das tägliche Wasserziel als [Flow] ab.
     *
     * @return Ein [Flow] des täglichen Wasserziels in Millilitern.
     */
    fun getDailyGoal(): Flow<Int> = waterLocalDataSource.getDailyGoal()

    /**
     * Speichert das tägliche Wasserziel.
     *
     * @param goal Das tägliche Wasserziel in Millilitern.
     */
    suspend fun saveDailyGoal(goal: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveDailyGoal(goal)
        }
    }

    /**
     * Ruft das Körpergewicht als [Flow] ab.
     *
     * @return Ein [Flow] des Körpergewichts in Kilogramm.
     */
    fun getBodyWeight(): Flow<Int> = waterLocalDataSource.getBodyWeight()

    /**
     * Speichert das Körpergewicht.
     *
     * @param weight Das Körpergewicht in Kilogramm.
     */
    suspend fun saveBodyWeight(weight: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveBodyWeight(weight)
        }
    }

    // --- Notification Settings ---

    fun getNotificationsEnabled(): Flow<Boolean> = waterLocalDataSource.getNotificationsEnabled()

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveNotificationsEnabled(enabled)
        }
    }

    fun getNotificationStartHour(): Flow<Int> = waterLocalDataSource.getNotificationStartHour()

    suspend fun saveNotificationStartHour(hour: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveNotificationStartHour(hour)
        }
    }

    fun getNotificationEndHour(): Flow<Int> = waterLocalDataSource.getNotificationEndHour()

    suspend fun saveNotificationEndHour(hour: Int) {
        withContext(Dispatchers.IO) {
            waterLocalDataSource.saveNotificationEndHour(hour)
        }
    }
}
