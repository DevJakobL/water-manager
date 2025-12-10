package com.dev.jakob.watermanager.ui.welcome.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.jakob.watermanager.data.model.Container // Angepasster Import
import com.dev.jakob.watermanager.data.repository.WaterRepository // Angepasster Import

/**
 * [WelcomeViewModel] ist das ViewModel.
 * Es verwaltet die UI-bezogenen Daten und die Geschäftslogik für die Anzeige
 * der insgesamt getrunkenen Wassermenge und der verfügbaren Wasserbehälter.
 *
 * @param repository Das [WaterRepository], das für den Datenzugriff verwendet wird.
 */
class WelcomeViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _totalWater = MutableLiveData<Int>()
    /**
     * LiveData, die die insgesamt getrunkene Wassermenge in Millilitern enthält.
     * Beobachter können sich hier anmelden, um über Änderungen informiert zu werden.
     */
    val totalWater: LiveData<Int> = _totalWater

    private val _containers = MutableLiveData<List<Container>>()
    /**
     * LiveData, die eine Liste der verfügbaren Wasserbehälter enthält.
     * Beobachter können sich hier anmelden, um über Änderungen informiert zu werden.
     */
    val containers: LiveData<List<Container>> = _containers

    init {
        loadInitialData()
    }

    /**
     * Lädt die initialen Daten für die Wasserbehälter und die insgesamt getrunkene Wassermenge
     * aus dem [WaterRepository].
     */
    private fun loadInitialData() {
        _containers.value = repository.loadContainers()
        _totalWater.value = repository.loadTotalWater()
    }

    /**
     * Fügt die Wassermenge eines bestimmten Behälters zur insgesamt getrunkenen Menge hinzu
     * und speichert den aktualisierten Wert über das [WaterRepository].
     *
     * @param container Der [Container], dessen Wassermenge hinzugefügt werden soll.
     */
    fun addWater(container: Container) {
        val currentWater = _totalWater.value ?: 0
        val newTotal = currentWater + container.size
        _totalWater.value = newTotal
        repository.saveTotalWater(newTotal)
    }

    /**
     * Aktualisiert die Daten, indem die initialen Daten erneut geladen werden.
     * Dies ist nützlich, wenn die Daten außerhalb des ViewModels geändert wurden (z.B. in den Einstellungen).
     */
    fun refreshData() {
        loadInitialData()
    }
}
