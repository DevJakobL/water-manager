package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water // Import hinzugefügt
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource

/**
 * [WaterRepository] ist für die Verwaltung der Wasserdaten der Anwendung zuständig.
 * Es agiert als Vermittler zwischen der Datenquelle ([WaterLocalDataSource]) und dem Rest der Anwendung.
 *
 * @param waterLocalDataSource Die lokale Datenquelle, die für den Zugriff auf gespeicherte Wasserdaten verwendet wird.
 */
class WaterRepository(private val waterLocalDataSource: WaterLocalDataSource) {

    /**
     * Lädt die Liste der vordefinierten Wasserbehälter aus der lokalen Datenquelle.
     *
     * @return Eine veränderliche Liste von [Container]-Objekten.
     */
    fun loadContainers(): MutableList<Container> {
        return waterLocalDataSource.loadContainers()
    }

    /**
     * Speichert eine Liste von Wasserbehältern in der lokalen Datenquelle.
     *
     * @param containers Die Liste der zu speichernden [Container]-Objekte.
     */
    fun saveContainers(containers: List<Container>) {
        waterLocalDataSource.saveContainers(containers)
    }

    /**
     * Speichert die Liste der getrunkenen Wassermengen in der lokalen Datenquelle.
     *
     * @param waterList Die Liste der zu speichernden [Water]-Objekte.
     */
    fun saveWaterIntake(waterList: List<Water>) {
        waterLocalDataSource.saveWaterIntake(waterList)
    }

    /**
     * Lädt die Liste der getrunkenen Wassermengen aus der lokalen Datenquelle.
     *
     * @return Die Liste der [Water]-Objekte, oder eine leere Liste, wenn kein Wert gespeichert ist.
     */
    fun loadWaterIntake(): List<Water> {
        return waterLocalDataSource.loadWaterIntake()
    }
}
