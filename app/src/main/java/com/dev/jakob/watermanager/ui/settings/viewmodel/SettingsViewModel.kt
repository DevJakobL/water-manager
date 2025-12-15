package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the Settings screen.
 *
 * @param containers The list of available containers.
 * @param bodyWeight The user's body weight in kilograms.
 * @param calculationFactor The factor (ml/kg) used to calculate the daily goal.
 * @param dailyGoal The calculated daily water intake goal in milliliters.
 */
data class SettingsUiState(
    val containers: List<Container> = emptyList(),
    val bodyWeight: Int = 0,
    val calculationFactor: Int = 30, // Default to 30ml/kg
    val dailyGoal: Int = 0
)

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
    private val _uiState = MutableStateFlow(SettingsUiState())
    /**
     * Public immutable [StateFlow] that the UI observes for state updates.
     */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadInitialSettings()
    }

    /**
     * Loads the initial settings data from the repository asynchronously.
     */
    private fun loadInitialSettings() {
        viewModelScope.launch {
            val containers = repository.loadContainers()
            val bodyWeight = repository.loadBodyWeight()
            val dailyGoal = repository.loadDailyGoal()

            // Determine calculationFactor based on loaded dailyGoal and bodyWeight
            val calculatedFactor = if (bodyWeight > 0 && dailyGoal > 0) {
                (dailyGoal / bodyWeight).coerceIn(30, 40)
            } else {
                30 // Default factor
            }

            _uiState.update {
                it.copy(
                    containers = containers,
                    bodyWeight = bodyWeight,
                    calculationFactor = calculatedFactor,
                    dailyGoal = dailyGoal
                )
            }
        }
    }

    /**
     * Handles the event of a container's data being updated in the UI.
     * This function is the central point for processing user edits from the `ContainerAdapter`.
     * It finds the corresponding container in the current state list by its unique `id`
     * and replaces it with the updated version, preserving the list order.
     * Finally, it emits the new, updated list to the [StateFlow], triggering a UI refresh.
     *
     * @param updatedContainer The container with the new data.
     */
    fun onContainerUpdated(updatedContainer: Container) {
        _uiState.update { currentState ->
            val newList = currentState.containers.map { container ->
                if (container.id == updatedContainer.id) {
                    updatedContainer // Replace the old container with the updated one
                } else {
                    container
                }
            }
            currentState.copy(containers = newList)
        }
    }

    /**
     * Adds a new, empty container to the UI state for the user to fill out.
     * The new container gets a automatically generated unique ID from its constructor.
     */
    fun addContainer() {
        _uiState.update { currentState ->
            val newContainer = Container(name = "", size = 0)
            currentState.copy(containers = currentState.containers + newContainer)
        }
    }

    /**
     * Removes a container from the current UI state.
     *
     * @param container The [Container] to remove.
     */
    fun removeContainer(container: Container) {
        _uiState.update { currentState ->
            currentState.copy(containers = currentState.containers - container)
        }
    }

    /**
     * Updates the body weight in the UI state and recalculates the daily goal.
     *
     * @param weight The new body weight in kilograms.
     */
    fun onBodyWeightChanged(weight: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(weight, currentState.calculationFactor)
            currentState.copy(bodyWeight = weight, dailyGoal = newDailyGoal)
        }
    }

    /**
     * Updates the calculation factor in the UI state and recalculates the daily goal.
     *
     * @param factor The new calculation factor (ml/kg).
     */
    fun onCalculationFactorChanged(factor: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(currentState.bodyWeight, factor)
            currentState.copy(calculationFactor = factor, dailyGoal = newDailyGoal)
        }
    }

    /**
     * Saves the current settings (containers, body weight, and daily goal) to the repository.
     * It filters out any incomplete containers before saving.
     */
    fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            // Filter out containers that are not valid before saving
            val validContainers = currentState.containers.filter { it.name.isNotBlank() && it.size > 0 }
            repository.saveContainers(validContainers)
            repository.saveBodyWeight(currentState.bodyWeight)
            repository.saveDailyGoal(currentState.dailyGoal)
        }
    }

    /**
     * Calculates the daily water intake goal based on body weight and a given factor.
     *
     * @param bodyWeight The user's body weight in kilograms.
     * @param factor The factor (ml/kg) to use for calculation (e.g., 30 or 40).
     * @return The calculated daily water intake goal in milliliters.
     */
    private fun calculateDailyGoal(bodyWeight: Int, factor: Int): Int {
        return bodyWeight * factor
    }
}
