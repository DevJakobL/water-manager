package com.dev.jakob.watermanager.ui.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WelcomeUiState(
    val totalWaterToday: Int = 0,
    val containers: List<Container> = emptyList(),
    val dailyGoal: Int = 0
)

class WelcomeViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.getAllWaterIntake(),
            repository.getAllContainers(),
            repository.getDailyGoal()
        ) { waterList, containers, dailyGoal ->
            val today = LocalDate.now(ZoneId.systemDefault())
            val sumToday = waterList.filter { water ->
                Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .isEqual(today)
            }.sumOf { it.amount }

            WelcomeUiState(
                totalWaterToday = sumToday,
                containers = containers,
                dailyGoal = dailyGoal
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun addWater(container: Container) {
        viewModelScope.launch {
            val newWaterEntry = Water(name = container.name, amount = container.size, timestamp = System.currentTimeMillis())
            repository.insertWater(newWaterEntry)
        }
    }
}
