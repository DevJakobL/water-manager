package com.dev.jakob.watermanager.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.data.repository.WaterRepository
import com.dev.jakob.watermanager.worker.HydrationReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Repräsentiert den UI-Zustand des Einstellungsbildschirms.
 *
 * @property containers Eine Liste der aktuell konfigurierten Trinkgefäße.
 * @property bodyWeight Das Körpergewicht des Benutzers in Kilogramm.
 * @property calculationFactor Der Berechnungsfaktor in ml/kg zur Berechnung des Tagesziels.
 * @property dailyGoal Das berechnete tägliche Wasserziel in Millilitern.
 * @property notificationsEnabled Ob Benachrichtigungen aktiviert sind.
 * @property notificationStartHour Die Startstunde für Benachrichtigungen (0-23).
 * @property notificationEndHour Die Endstunde für Benachrichtigungen (0-23).
 */
data class SettingsUiState(
    val containers: List<Container> = emptyList(),
    val bodyWeight: Int = 0,
    val calculationFactor: Int = 30,
    val dailyGoal: Int = 0,
    val notificationsEnabled: Boolean = false,
    val notificationStartHour: Int = 8,
    val notificationEndHour: Int = 23
)

/**
 * ViewModel für den Einstellungsbildschirm.
 *
 * Verwaltet den Zustand der Einstellungen und interagiert mit dem [WaterRepository],
 * um Benutzerdaten wie Körpergewicht, Berechnungsfaktor und Trinkgefäße zu speichern und abzurufen.
 *
 * @param repository Das [WaterRepository] zur Datenverwaltung.
 * @param workManager Der [WorkManager] zum Planen von Hintergrundaufgaben.
 */
class SettingsViewModel(
    private val repository: WaterRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())

    /**
     * Der öffentliche [StateFlow], der den aktuellen UI-Zustand des Einstellungsbildschirms bereitstellt.
     */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Hilfsklasse für combine, da combine nur bis zu 5 Argumente direkt unterstützt
    private data class SettingsPartialState(
        val bodyWeight: Int,
        val dailyGoal: Int,
        val notificationsEnabled: Boolean,
        val startHour: Int,
        val endHour: Int
    )

    init {
        // Kombiniert die Flows aus dem Repository, um den UI-Zustand zu aktualisieren.
        // Da combine maximal 5 Flows unterstützt, gruppieren wir die Einstellungen.
        val partialSettingsFlow = combine(
            repository.getBodyWeight(),
            repository.getDailyGoal(),
            repository.getNotificationsEnabled(),
            repository.getNotificationStartHour(),
            repository.getNotificationEndHour()
        ) { bodyWeight, dailyGoal, notificationsEnabled, startHour, endHour ->
            SettingsPartialState(bodyWeight, dailyGoal, notificationsEnabled, startHour, endHour)
        }

        combine(
            repository.getAllContainers(),
            partialSettingsFlow
        ) { containers, partialState ->
            val calculatedFactor = if (partialState.bodyWeight > 0 && partialState.dailyGoal > 0) {
                (partialState.dailyGoal.toFloat() / partialState.bodyWeight.toFloat()).toInt().coerceIn(30, 40)
            } else {
                30
            }
            SettingsUiState(
                containers = containers,
                bodyWeight = partialState.bodyWeight,
                dailyGoal = partialState.dailyGoal,
                calculationFactor = calculatedFactor,
                notificationsEnabled = partialState.notificationsEnabled,
                notificationStartHour = partialState.startHour,
                notificationEndHour = partialState.endHour
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    /**
     * Aktualisiert einen vorhandenen Container im UI-Zustand und speichert ihn persistent.
     * Wenn der Container neu ist (id = 0L), wird eine neue ID von der Datenbank zugewiesen.
     *
     * @param updatedContainer Der zu aktualisierende [Container].
     */
    fun onContainerUpdated(updatedContainer: Container) {
        viewModelScope.launch {
            val newId = repository.insertContainer(updatedContainer)
            _uiState.update { currentState ->
                val updatedList = if (updatedContainer.id == 0L) {
                    // Neuer Container, füge ihn mit der neuen ID hinzu
                    currentState.containers.map {
                        if (it == updatedContainer) updatedContainer.copy(id = newId) else it
                    }
                } else {
                    // Bestehender Container, aktualisiere ihn
                    currentState.containers.map {
                        if (it.id == updatedContainer.id) updatedContainer else it
                    }
                }
                currentState.copy(containers = updatedList)
            }
        }
    }

    /**
     * Fügt einen neuen, leeren Container zum UI-Zustand hinzu.
     * Dieser Container hat zunächst keinen Namen und keine Größe und eine ID von 0L,
     * was anzeigt, dass er noch nicht in der Datenbank gespeichert ist.
     */
    fun addContainer() {
        _uiState.update { currentState ->
            val newContainer = Container(name = "", size = 0)
            currentState.copy(containers = currentState.containers + newContainer)
        }
    }

    /**
     * Entfernt einen Container aus dem UI-Zustand und löscht ihn gegebenenfalls aus der Datenbank.
     *
     * @param container Der zu entfernende [Container].
     */
    fun removeContainer(container: Container) {
        _uiState.update { currentState ->
            currentState.copy(containers = currentState.containers - container)
        }
        if (container.id != 0L) { // Nur löschen, wenn der Container bereits in der DB existiert
            viewModelScope.launch {
                repository.deleteContainer(container)
            }
        }
    }

    /**
     * Aktualisiert das Körpergewicht des Benutzers und berechnet das Tagesziel neu.
     *
     * @param weight Das neue Körpergewicht in Kilogramm.
     */
    fun onBodyWeightChanged(weight: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(weight, currentState.calculationFactor)
            currentState.copy(bodyWeight = weight, dailyGoal = newDailyGoal)
        }
    }

    /**
     * Aktualisiert den Berechnungsfaktor und berechnet das Tagesziel neu.
     *
     * @param factor Der neue Berechnungsfaktor in ml/kg.
     */
    fun onCalculationFactorChanged(factor: Int) {
        _uiState.update { currentState ->
            val newDailyGoal = calculateDailyGoal(currentState.bodyWeight, factor)
            currentState.copy(calculationFactor = factor, dailyGoal = newDailyGoal)
        }
    }

    fun onNotificationsEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            repository.saveNotificationsEnabled(enabled)
            scheduleHydrationReminder(enabled)
        }
    }

    fun onNotificationStartHourChanged(hour: Int) {
        _uiState.update { it.copy(notificationStartHour = hour) }
        viewModelScope.launch {
            repository.saveNotificationStartHour(hour)
        }
    }

    fun onNotificationEndHourChanged(hour: Int) {
        _uiState.update { it.copy(notificationEndHour = hour) }
        viewModelScope.launch {
            repository.saveNotificationEndHour(hour)
        }
    }

    fun sendTestNotification() {
        val workRequest = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setInputData(workDataOf("IS_TEST" to true))
            .build()
        workManager.enqueue(workRequest)
    }

    /**
     * Speichert die aktuellen Einstellungen (Körpergewicht und Tagesziel)
     * persistent über das [WaterRepository].
     * Die Container werden bereits bei jeder Aktualisierung gespeichert.
     */
    fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            repository.saveBodyWeight(currentState.bodyWeight)
            repository.saveDailyGoal(currentState.dailyGoal)
            // Notification settings are saved immediately on change
        }
    }

    /**
     * Berechnet das tägliche Wasserziel basierend auf Körpergewicht und Berechnungsfaktor.
     *
     * @param bodyWeight Das Körpergewicht in Kilogramm.
     * @param factor Der Berechnungsfaktor in ml/kg.
     * @return Das berechnete tägliche Wasserziel in Millilitern.
     */
    private fun calculateDailyGoal(bodyWeight: Int, factor: Int): Int {
        return bodyWeight * factor
    }

    private fun scheduleHydrationReminder(enabled: Boolean) {
        if (enabled) {
            val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(1, TimeUnit.HOURS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } else {
            workManager.cancelUniqueWork("hydration_reminder")
        }
    }
}
