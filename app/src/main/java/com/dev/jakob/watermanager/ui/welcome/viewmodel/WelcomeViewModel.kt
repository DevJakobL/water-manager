package com.dev.jakob.watermanager.ui.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Represents the UI state for the Welcome screen.
 *
 * @param totalWaterToday The total amount of water consumed today.
 * @param containers The list of available containers.
 * @param dailyGoal The daily water intake goal.
 */
data class WelcomeUiState(
    val totalWaterToday: Int = 0,
    val containers: List<Container> = emptyList(),
    val dailyGoal: Int = 0
)

/**
 * ViewModel for the Welcome screen, following modern Android architecture principles.
 * It manages the UI state and handles business logic for the welcome screen.
 *
 * @property repository The [WaterRepository] for data access.
 */
class WelcomeViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    // Keep a private copy of the full water intake list to avoid re-loading it constantly.
    private var fullWaterIntakeList: List<Water> = emptyList()

    init {
        loadInitialData()
    }

    /**
     * Loads the initial data for containers and water intake from the repository asynchronously.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            fullWaterIntakeList = repository.loadWaterIntake()
            val containers = repository.loadContainers()
            val dailyGoal = repository.loadDailyGoal() // Load daily goal
            updateUiState(fullWaterIntakeList, containers, dailyGoal)
        }
    }

    /**
     * Adds a new water intake entry and saves it.
     *
     * @param container The [Container] that was used.
     */
    fun addWater(container: Container) {
        viewModelScope.launch {
            val newWaterEntry = Water(container.name, container.size, System.currentTimeMillis())
            val updatedList = fullWaterIntakeList + newWaterEntry
            repository.saveWaterIntake(updatedList)

            // Update the local list and UI state
            fullWaterIntakeList = updatedList
            updateUiState(fullWaterIntakeList, uiState.value.containers, uiState.value.dailyGoal)
        }
    }

    /**
     * Recalculates the UI state based on the latest data.
     */
    private fun updateUiState(waterList: List<Water>, containers: List<Container>, dailyGoal: Int) {
        val today = LocalDate.now(ZoneId.systemDefault())
        val sumToday = waterList.filter { water ->
            Instant.ofEpochMilli(water.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .isEqual(today)
        }.sumOf { it.amount }

        _uiState.update {
            it.copy(
                totalWaterToday = sumToday,
                containers = containers,
                dailyGoal = dailyGoal // Update daily goal in UI state
            )
        }
    }

    /**
     * Refreshes the data by reloading it from the repository.
     */
    fun refreshData() {
        loadInitialData()
    }
}
