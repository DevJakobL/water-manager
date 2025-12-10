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
     * Fügt einen neuen Behälter zur Liste hinzu und aktualisiert die LiveData.
     *
     * @param container Der hinzuzufügende [Container].
     */
    fun addContainer(container: Container) {
        val currentContainers = _containers.value ?: mutableListOf()
        currentContainers.add(container)
        _containers.value = currentContainers // Trigger LiveData update
    }

    /**
     * Entfernt einen Behälter aus der Liste an einem bestimmten Index und aktualisiert die LiveData.
     *
     * @param index Der Index des zu entfernenden Behälters.
     */
    fun removeContainerAt(index: Int) {
        val currentContainers = _containers.value ?: mutableListOf()
        if (index >= 0 && index < currentContainers.size) {
            currentContainers.removeAt(index)
            _containers.value = currentContainers // Trigger LiveData update
        }
    }

    /**
     * Aktualisiert einen Behälter in der Liste an einem bestimmten Index und aktualisiert die LiveData.
     *
     * @param index Der Index des zu aktualisierenden Behälters.
     * @param newContainer Der aktualisierte [Container].
     */
    fun updateContainerAt(index: Int, newContainer: Container) {
        val currentContainers = _containers.value ?: mutableListOf()
        if (index >= 0 && index < currentContainers.size) {
            currentContainers[index] = newContainer
            _containers.value = currentContainers // Trigger LiveData update
        }
    }

    /**
     * Speichert die aktuelle Liste der Behälter über das [WaterRepository].
     */
    fun saveContainers() {
        _containers.value?.let {
            repository.saveContainers(it)
        }
    }
}
