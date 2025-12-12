package com.dev.jakob.watermanager.ui.welcome.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MediatorLiveData // Import for MediatorLiveData
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.model.Water // Import for Water
import com.dev.jakob.watermanager.data.repository.WaterRepository
import java.util.* // Import for Calendar

/**
 * [WelcomeViewModel] ist das ViewModel.
 * Es verwaltet die UI-bezogenen Daten und die Geschäftslogik für die Anzeige
 * der insgesamt getrunkenen Wassermenge und der verfügbaren Wasserbehälter.
 *
 * @param repository Das [WaterRepository], das für den Datenzugriff verwendet wird.
 */
class WelcomeViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _waterIntakeList = MutableLiveData<List<Water>>()
    /**
     * LiveData, die eine Liste aller getrunkenen Wassermengen ([Water]-Objekte) enthält.
     * Beobachter können sich hier anmelden, um über Änderungen informiert zu werden.
     */
    val waterIntakeList: LiveData<List<Water>> = _waterIntakeList

    /**
     * LiveData, die die insgesamt an diesem Tag getrunkene Wassermenge in Millilitern enthält.
     * Dieser Wert wird aus der [waterIntakeList] für den aktuellen Tag berechnet.
     */
    val totalWaterToday: MediatorLiveData<Int> = MediatorLiveData()

    private val _containers = MutableLiveData<List<Container>>()
    /**
     * LiveData, die eine Liste der verfügbaren Wasserbehälter enthält.
     * Beobachter können sich hier anmelden, um über Änderungen informiert zu werden.
     */
    val containers: LiveData<List<Container>> = _containers

    init {
        // Beobachte _waterIntakeList und aktualisiere totalWaterToday, wenn sich _waterIntakeList ändert
        totalWaterToday.addSource(_waterIntakeList) { waterList ->
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            val sum = waterList.filter { water: Water ->
                val waterCalendar = Calendar.getInstance().apply { timeInMillis = water.timestamp }
                waterCalendar.get(Calendar.DAY_OF_YEAR) == currentDay && waterCalendar.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
            totalWaterToday.value = sum
        }
        loadInitialData()
    }

    /**
     * Lädt die initialen Daten für die Wasserbehälter und die insgesamt getrunkene Wassermenge
     * aus dem [WaterRepository].
     */
    private fun loadInitialData() {
        _containers.value = repository.loadContainers()
        _waterIntakeList.value = repository.loadWaterIntake()
    }

    /**
     * Fügt die Wassermenge eines bestimmten Behälters zur Liste der getrunkenen Mengen hinzu
     * und speichert die aktualisierte Liste über das [WaterRepository].
     *
     * @param container Der [Container], dessen Wassermenge hinzugefügt werden soll.
     */
    fun addWater(container: Container) {
        val currentList = _waterIntakeList.value.orEmpty().toMutableList()
        val newWaterEntry = Water(container.name, container.size, System.currentTimeMillis())
        currentList.add(newWaterEntry)
        _waterIntakeList.value = currentList
        repository.saveWaterIntake(currentList)
    }

    /**
     * Aktualisiert die Daten, indem die initialen Daten erneut geladen werden.
     * Dies ist nützlich, wenn die Daten außerhalb des ViewModels geändert wurden (z.B. in den Einstellungen).
     */
    fun refreshData() {
        loadInitialData()
    }
}
