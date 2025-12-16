package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val containers: List<Container> = emptyList(),
    val bodyWeight: Int = 0,
    val calculationFactor: Int = 30,
    val dailyGoal: Int = 0
)

class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.getAllContainers(),
            repository.getBodyWeight(),
            repository.getDailyGoal()
        ) { containers, bodyWeight, dailyGoal ->
            val calculatedFactor = if (bodyWeight > 0 && dailyGoal > 0) {
                (dailyGoal.toFloat() / bodyWeight.toFloat()).toInt().coerceIn(30, 40)
            } else {
                30
            }
            SettingsUiState(
                containers = containers,
                bodyWeight = bodyWeight,
                dailyGoal = dailyGoal,
                calculationFactor = calculatedFactor
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun onContainerUpdated(updatedContainer: Container) {
        _uiState.update { currentState ->
            val newList = currentState.containers.map { container ->
                if (container.id == updatedContainer.id) updatedContainer else container
            }
            currentState.copy(containers = newList)
        }
    }

    fun addContainer() {
        _uiState.update { currentState ->
            val newContainer = Container(name = "", size = 0)
            currentState.copy(containers = currentState.containers + newContainer)
        }
    }

    fun removeContainer(container: Container) {
        _uiState.update { currentState ->
            currentState.copy(containers = currentState.containers - container)
        }
        if (container.id != 0L) {
            viewModelScope.launch {
                repository.deleteContainer(container)
            }
        }
    }

    fun onBodyWeightChanged(weight: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(weight, currentState.calculationFactor)
            currentState.copy(bodyWeight = weight, dailyGoal = newDailyGoal)
        }
    }

    fun onCalculationFactorChanged(factor: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(currentState.bodyWeight, factor)
            currentState.copy(calculationFactor = factor, dailyGoal = newDailyGoal)
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val validContainers = currentState.containers.filter { it.name.isNotBlank() && it.size > 0 }

            validContainers.forEach { container ->
                repository.insertContainer(container)
            }

            repository.saveBodyWeight(currentState.bodyWeight)
            repository.saveDailyGoal(currentState.dailyGoal)
        }
    }

    private fun calculateDailyGoal(bodyWeight: Int, factor: Int): Int {
        return bodyWeight * factor
    }
}
