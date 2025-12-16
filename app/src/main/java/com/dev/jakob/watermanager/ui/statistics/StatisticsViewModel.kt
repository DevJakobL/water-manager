package com.dev.jakob.watermanager.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.*

/**
 * Data class to hold the water intake for a specific date.
 * @param date The date of the water intake.
 * @param amount The total amount of water consumed on that date.
 */
data class CalendarData(val date: Date, val amount: Float)

/**
 * [ViewModel] for the [StatisticsScreen].
 * It provides the data for the calendar by fetching it from the [WaterRepository] asynchronously.
 * @param waterRepository The repository for accessing water data.
 */
class StatisticsViewModel(private val waterRepository: WaterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<CalendarData>>(emptyList())
    val uiState: StateFlow<List<CalendarData>> = _uiState.asStateFlow()

    /**
     * Holds the full list of all water intake entries loaded from the repository.
     * This list is used to filter entries for specific dates.
     */
    private var allWaterIntake: List<Water> = emptyList()

    private val _selectedDayWaterIntake = MutableStateFlow<List<Water>>(emptyList())
    /**
     * [StateFlow] that exposes the list of [Water] entries for the currently selected day.
     * UI components can collect this flow to display the detailed water intake for a specific date.
     */
    val selectedDayWaterIntake: StateFlow<List<Water>> = _selectedDayWaterIntake.asStateFlow()

    private val _containers = MutableStateFlow<List<Container>>(emptyList())
    /**
     * [StateFlow] that exposes the list of available [Container]s.
     */
    val containers: StateFlow<List<Container>> = _containers.asStateFlow()

    init {
        viewModelScope.launch {
            loadCalendarData()
            loadContainers()
        }
    }

    /**
     * Loads the list of available containers from the repository.
     */
    private suspend fun loadContainers() {
        _containers.value = waterRepository.loadContainers()
    }

    /**
     * Loads the water intake data from the repository and processes it for the calendar.
     * It groups the water intake by day and sums the amounts.
     * This function also populates [allWaterIntake] with the raw data.
     */
    private suspend fun loadCalendarData() {
        allWaterIntake = waterRepository.loadWaterIntake() // Store the full list

        val entries = allWaterIntake
            .groupBy { water ->
                // Group entries by their date in the system's default timezone
                Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (date, dailyWaterEntries) ->
                // For each day, sum the total amount of water
                val totalAmount = dailyWaterEntries.sumOf { it.amount }.toFloat()
                // Convert the LocalDate back to a Date object for the UI
                val dateAsJavaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                CalendarData(dateAsJavaUtilDate, totalAmount)
            }

        _uiState.value = entries
    }

    /**
     * Loads the water intake data from the repository and processes it for the calendar.
     * It groups the water intake by day and sums the amounts.
     */
    suspend fun refreshData() {
        loadCalendarData()
    }

    /**
     * Filters the water intake entries for a specific selected date and updates the
     * [selectedDayWaterIntake] StateFlow.
     * @param selectedDate The date for which to filter the water intake entries.
     */
    fun onDaySelected(selectedDate: Date) {
        viewModelScope.launch {
            val selectedLocalDate = Instant.ofEpochMilli(selectedDate.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val filteredList = allWaterIntake.filter { water ->
                Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() == selectedLocalDate
            }
            _selectedDayWaterIntake.value = filteredList
        }
    }

    /**
     * Adds a new water intake entry to the repository and refreshes the data.
     * @param container The [Container] to add.
     * @param date The date for which to add the water intake.
     */
    fun addWaterIntake(container: Container, date: Date) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val selectedDateCalendar = Calendar.getInstance().apply { time = date }

            calendar.set(Calendar.YEAR, selectedDateCalendar.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, selectedDateCalendar.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
            // Time is set to the current time

            val newWater = Water(container.name, container.size, calendar.timeInMillis)
            val updatedList = allWaterIntake.toMutableList().apply { add(newWater) }
            waterRepository.saveWaterIntake(updatedList)
            refreshData()
            onDaySelected(date)
        }
    }

    /**
     * Deletes a specific water intake entry from the repository and refreshes the data.
     * @param water The [Water] object to be deleted.
     */
    fun deleteWaterIntake(water: Water) {
        viewModelScope.launch {
            // Preserve the currently selected date before data is refreshed
            val currentSelectedDate = _selectedDayWaterIntake.value.firstOrNull()?.let {
                Date(it.timestamp)
            } ?: Date() // Fallback to current date

            waterRepository.deleteWaterIntake(water)
            refreshData() // Refresh all data after deletion

            // Re-filter for the preserved date to update the list
            onDaySelected(currentSelectedDate)
        }
    }
}
