package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository

/**
 * [SettingsViewModel] ist das ViewModel für die Einstellungen.
 * Es verwaltet die Liste der Wasserbehälter und interagiert mit dem [WaterRepository],
 * um diese zu laden und zu speichern.
 *
 * @param repository Das [WaterRepository], das für den Datenzugriff verwendet wird.
 */
class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _containers = MutableLiveData<MutableList<Container>>()
    /**
     * LiveData, die eine veränderliche Liste der Wasserbehälter enthält.
     * Beobachter können sich hier anmelden, um über Änderungen informiert zu werden.
     */
    val containers: LiveData<MutableList<Container>> = _containers

    init {
        loadContainers()
    }

    /**
     * Lädt die Liste der Wasserbehälter aus dem [WaterRepository] und aktualisiert die LiveData.
     */
    fun loadContainers() {
        _containers.value = repository.loadContainers()
    }

    /**
     * Speichert die übergebene Liste von Behältern über das [WaterRepository]
     * und aktualisiert die LiveData.
     *
     * @param containersToSave Die Liste der zu speichernden [Container].
     */
    fun saveContainers(containersToSave: List<Container>) {
        repository.saveContainers(containersToSave)
        _containers.value = containersToSave.toMutableList()
    }
}
