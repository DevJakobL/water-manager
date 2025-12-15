package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen, adhering to modern Android architecture principles.
 *
 * It holds the UI state, specifically the list of water containers, in a [StateFlow]
 * and exposes it to the UI. It acts as the Single Source of Truth for the settings data.
 * All user interactions (events) are handled here to update the state in a unidirectional data flow.
 *
 * @property repository The [WaterRepository] for data operations.
 */
class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    // Private mutable state flow, the single source of truth
    private val _uiState = MutableStateFlow<List<Container>>(emptyList())
    /**
     * Public immutable [StateFlow] that the UI observes for state updates.
     */
    val uiState: StateFlow<List<Container>> = _uiState.asStateFlow()

    init {
        loadContainers()
    }

    /**
     * Loads the initial list of containers from the repository asynchronously.
     */
    private fun loadContainers() {
        viewModelScope.launch {
            _uiState.value = repository.loadContainers()
        }
    }

    /**
     * Handles the event of text changing for a container's name.
     * It creates a new list with the updated container and emits it to the [StateFlow].
     *
     * @param containerId The ID of the container being edited. Can be null for newly added containers before saving.
     * @param newName The new name for the container.
     */
    fun onContainerNameChanged(containerId: String, newName: String) {
        val newList = _uiState.value.map { container ->
            if (container.id == containerId) {
                container.copy(name = newName) // Create a new instance with the change
            } else {
                container
            }
        }
        _uiState.value = newList
    }

    /**
     * Handles the event of text changing for a container's size.
     * It creates a new list with the updated container and emits it to the [StateFlow].
     *
     * @param containerId The ID of the container being edited. Can be null for newly added containers before saving.
     * @param newSize The new size for the container.
     */
    fun onContainerSizeChanged(containerId: String, newSize: Int) {
        val newList = _uiState.value.map { container ->
            if (container.id == containerId) {
                container.copy(size = newSize) // Create a new instance with the change
            } else {
                container
            }
        }
        _uiState.value = newList
    }

    /**
     * Adds a new, empty container to the UI state for the user to fill out.
     * The new container gets a automatically generated unique ID from its constructor.
     */
    fun addContainer() {
        val newContainer = Container(name = "", size = 0)
        _uiState.value = _uiState.value + newContainer
    }

    /**
     * Removes a container from the current UI state.
     *
     * @param container The [Container] to remove.
     */
    fun removeContainer(container: Container) {
        _uiState.value = _uiState.value - container
    }

    /**
     * Saves the current, valid list of containers to the repository.
     * It filters out any incomplete containers before saving.
     */
    fun saveContainers() {
        viewModelScope.launch {
            // Filter out containers that are not valid before saving
            val validContainers = _uiState.value.filter { it.name.isNotBlank() && it.size > 0 }
            repository.saveContainers(validContainers)
        }
    }
}
