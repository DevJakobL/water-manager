package com.dev.jakob.watermanager.data.repository

import com.dev.jakob.watermanager.data.model.Container
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
     * Speichert die insgesamt getrunkene Wassermenge in der lokalen Datenquelle.
     *
     * @param totalWater Die zu speichernde Gesamtmenge an Wasser in Millilitern.
     */
    fun saveTotalWater(totalWater: Int) {
        waterLocalDataSource.saveTotalWater(totalWater)
    }

    /**
     * Lädt die insgesamt getrunkene Wassermenge aus der lokalen Datenquelle.
     *
     * @return Die insgesamt getrunkene Wassermenge in Millilitern, oder 0, wenn kein Wert gespeichert ist.
     */
    fun loadTotalWater(): Int {
        return waterLocalDataSource.loadTotalWater()
    }

    /**
     * Speichert eine Liste von Wasserbehältern in der lokalen Datenquelle.
     *
     * @param containers Die Liste der zu speichernden [Container]-Objekte.
     */
    fun saveContainers(containers: List<Container>) {
        waterLocalDataSource.saveContainers(containers)
    }
}
