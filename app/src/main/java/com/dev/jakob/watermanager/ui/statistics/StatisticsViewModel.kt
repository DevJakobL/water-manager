package com.dev.jakob.watermanager.ui.statistics

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
import java.util.*

data class CalendarData(val date: Date, val amount: Int)

data class StatisticsUiState(
    val calendarData: List<CalendarData> = emptyList(),
    val selectedDayWaterIntake: List<Water> = emptyList(),
    val containers: List<Container> = emptyList(),
    val weeklySummary: WeeklySummary = WeeklySummary(0, 0, 0),
    val dailyGoal: Int = 0
)

class StatisticsViewModel(private val waterRepository: WaterRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        combine(
            waterRepository.getAllWaterIntake(),
            waterRepository.getAllContainers(),
            waterRepository.getDailyGoal(),
            _selectedDate
        ) { allWaterIntake, containers, dailyGoal, selectedDate ->
            val groupedByDay = allWaterIntake.groupBy { water ->
                Instant.ofEpochMilli(water.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }

            val calendarData = groupedByDay.map { (date, entries) ->
                val totalAmount = entries.sumOf { it.amount }
                CalendarData(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()), totalAmount)
            }

            val selectedDayIntake = groupedByDay[selectedDate] ?: emptyList()

            // Calculate Weekly Summary
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val weekEntries = allWaterIntake.filter {
                val entryDate = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                !entryDate.isBefore(startOfWeek) && !entryDate.isAfter(today)
            }
            val weekIntakeByDay = weekEntries.groupBy {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }.mapValues { it.value.sumOf { entry -> entry.amount } }

            val averageIntake = if (weekIntakeByDay.isNotEmpty()) weekIntakeByDay.values.average().toInt() else 0
            val goalMetDays = weekIntakeByDay.count { it.value >= dailyGoal }
            val extraHydratedDays = weekIntakeByDay.count { it.value >= dailyGoal * 1.2 }

            StatisticsUiState(
                calendarData = calendarData,
                containers = containers,
                selectedDayWaterIntake = selectedDayIntake,
                weeklySummary = WeeklySummary(averageIntake, goalMetDays, extraHydratedDays),
                dailyGoal = dailyGoal
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun onDaySelected(selectedDate: Date) {
        _selectedDate.value = Instant.ofEpochMilli(selectedDate.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun addWaterIntake(container: Container, date: Date) {
        viewModelScope.launch {
            val selectedDateCalendar = Calendar.getInstance().apply { time = date }
            val now = Calendar.getInstance()

            val newTimestamp = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDateCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedDateCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                set(Calendar.SECOND, now.get(Calendar.SECOND))
            }.timeInMillis

            val newWater = Water(name = container.name, amount = container.size, timestamp = newTimestamp)
            waterRepository.insertWater(newWater)
        }
    }

    fun deleteWaterIntake(water: Water) {
        viewModelScope.launch {
            waterRepository.deleteWater(water)
        }
    }
}
