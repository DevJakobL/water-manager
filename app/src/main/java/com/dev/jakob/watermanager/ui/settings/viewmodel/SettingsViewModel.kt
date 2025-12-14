package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository

/**
 * ViewModel for the Settings screen.
 * It manages the list of water containers and interacts with the [WaterRepository]
 * to load and save them.
 *
 * **Note for future improvement:** This ViewModel currently uses synchronous calls to the repository.
 * These should be replaced with asynchronous calls using `viewModelScope.launch` once the repository
 * methods are converted to `suspend` functions.
 *
 * @property repository The [WaterRepository] used for data access.
 */
class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _containers = MutableLiveData<List<Container>>()
    /**
     * LiveData holding the list of water containers.
     * It exposes an immutable list to the UI to ensure data integrity.
     */
    val containers: LiveData<List<Container>> = _containers

    init {
        loadContainers()
    }

    /**
     * Loads the list of water containers from the [WaterRepository] and updates the LiveData.
     */
    fun loadContainers() {
        // In a coroutine-based architecture, this would be wrapped in viewModelScope.launch
        _containers.value = repository.loadContainers()
    }

    /**
     * Saves the given list of containers via the [WaterRepository]
     * and updates the LiveData.
     *
     * @param containersToSave The list of [Container] objects to be saved.
     */
    fun saveContainers(containersToSave: List<Container>) {
        // In a coroutine-based architecture, this would be wrapped in viewModelScope.launch
        repository.saveContainers(containersToSave)
        _containers.value = containersToSave
    }
}
