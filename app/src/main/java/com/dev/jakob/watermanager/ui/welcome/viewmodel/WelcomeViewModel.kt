package com.dev.jakob.watermanager.ui.welcome.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water
import com.dev.jakob.watermanager.data.repository.WaterRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel for the Welcome screen.
 * It manages UI-related data and business logic for displaying the total water intake
 * and the available water containers.
 *
 * **Note for future improvement:** This ViewModel currently uses synchronous calls to the repository.
 * These should be replaced with asynchronous calls using `viewModelScope.launch` once the repository
 * methods are converted to `suspend` functions.
 *
 * @property repository The [WaterRepository] used for data access.
 */
class WelcomeViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _waterIntakeList = MutableLiveData<List<Water>>()
    /**
     * LiveData holding a list of all [Water] intake entries.
     * Observers can subscribe to be notified of changes.
     */
    val waterIntakeList: LiveData<List<Water>> = _waterIntakeList

    /**
     * LiveData holding the total water consumed today in milliliters.
     * This value is calculated from [waterIntakeList] for the current day.
     */
    val totalWaterToday: MediatorLiveData<Int> = MediatorLiveData()

    private val _containers = MutableLiveData<List<Container>>()
    /**
     * LiveData holding a list of available water containers.
     * Observers can subscribe to be notified of changes.
     */
    val containers: LiveData<List<Container>> = _containers

    init {
        // Observe _waterIntakeList and update totalWaterToday whenever it changes.
        totalWaterToday.addSource(_waterIntakeList) { waterList ->
            // Den heutigen Tag in der Standard-Zeitzone des Geräts ermitteln
            val today = LocalDate.now(ZoneId.systemDefault())

            val sum = waterList.filter { water ->
                // Den gespeicherten UTC-Zeitstempel in ein Datum in der lokalen Zeitzone umwandeln
                val entryDate = Instant.ofEpochMilli(water.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                // Vergleichen, ob die Daten übereinstimmen
                entryDate.isEqual(today)
            }.sumOf { it.amount }

            totalWaterToday.value = sum
        }
        loadInitialData()
    }

    /**
     * Loads the initial data for containers and water intake from the [WaterRepository].
     */
    private fun loadInitialData() {
        // In a coroutine-based architecture, this would be wrapped in viewModelScope.launch
        _containers.value = repository.loadContainers()
        _waterIntakeList.value = repository.loadWaterIntake()
    }

    /**
     * Adds the water amount from a specific container to the intake list
     * and saves the updated list via the [WaterRepository].
     *
     * @param container The [Container] whose water amount should be added.
     */
    fun addWater(container: Container) {
        val currentList = _waterIntakeList.value.orEmpty().toMutableList()
        val newWaterEntry = Water(container.name, container.size, System.currentTimeMillis())
        currentList.add(newWaterEntry)
        _waterIntakeList.value = currentList

        // In a coroutine-based architecture, this would be wrapped in viewModelScope.launch
        repository.saveWaterIntake(currentList)
    }

    /**
     * Refreshes the data by reloading it from the repository.
     * This is useful when data might have been changed externally (e.g., in the Settings screen).
     */
    fun refreshData() {
        loadInitialData()
    }
}
