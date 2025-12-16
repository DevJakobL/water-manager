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

data class CalendarData(val date: Date, val amount: Float)

data class StatisticsUiState(
    val calendarData: List<CalendarData> = emptyList(),
    val selectedDayWaterIntake: List<Water> = emptyList(),
    val containers: List<Container> = emptyList()
)

class StatisticsViewModel(private val waterRepository: WaterRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        combine(
            waterRepository.getAllWaterIntake(),
            waterRepository.getAllContainers(),
            _selectedDate
        ) { allWaterIntake, containers, selectedDate ->
            val calendarData = allWaterIntake
                .groupBy { water ->
                    Instant.ofEpochMilli(water.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                .map { (date, dailyWaterEntries) ->
                    val totalAmount = dailyWaterEntries.sumOf { it.amount }.toFloat()
                    val dateAsJavaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    CalendarData(dateAsJavaUtilDate, totalAmount)
                }

            val selectedDayIntake = allWaterIntake.filter { water ->
                Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() == selectedDate
            }

            StatisticsUiState(
                calendarData = calendarData,
                containers = containers,
                selectedDayWaterIntake = selectedDayIntake
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
