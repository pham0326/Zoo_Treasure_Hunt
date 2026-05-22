package com.pham0326.flinders.zootreasurehunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.pham0326.flinders.zootreasurehunt.data.SettingsRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.model.ZooUiState
import com.pham0326.flinders.zootreasurehunt.sensors.ShakeDetector
import com.pham0326.flinders.zootreasurehunt.sensors.LightSensorManager
import com.pham0326.flinders.zootreasurehunt.sensors.LocationProvider
import com.pham0326.flinders.zootreasurehunt.sensors.StepCounterManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ZooUiEvent {
    data class SightingDeleted(val sighting: Sighting) : ZooUiEvent()
    data object SightingUpdated : ZooUiEvent()
    data object FilterClearedByShake : ZooUiEvent()
    data class NocturnalModeChanged(val isNocturnal: Boolean) : ZooUiEvent()
    data object PedometerReset : ZooUiEvent()
}

sealed class ProximityResult {
    data class Allowed(val distanceMetres: Float) : ProximityResult()
    data class TooFar(val animalName: String, val distanceMetres: Float) : ProximityResult()
    data object PermissionDenied : ProximityResult()
    data object NoLocationYet : ProximityResult()
    data class NoCoordinates(val animalName: String) : ProximityResult()
}

@HiltViewModel
class ZooViewModel @Inject constructor(
    private val sightingRepository: SightingRepository,
    private val settingsRepository: SettingsRepository,
    private val shakeDetector: ShakeDetector,
    private val lightSensorManager: LightSensorManager,
    private val stepCounterManager: StepCounterManager,
    private val locationProvider: LocationProvider
) : ViewModel() {

    companion object {
        const val PROXIMITY_THRESHOLD_M = 50f
    }
    private val _uiState = MutableStateFlow(ZooUiState())
    val uiState: StateFlow<ZooUiState> = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<ZooUiEvent>()
    val uiEvent: SharedFlow<ZooUiEvent> = _uiEvent.asSharedFlow()

    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())
    val currentLocation = locationProvider.currentLocation

    init {
        viewModelScope.launch {
            _rawSightings.value = sightingRepository.loadSightings()
        }

        viewModelScope.launch {
            combine(_rawSightings, settingsRepository.sortByNameFlow) { list, sortByName ->
                val sorted = if (sortByName) {
                    list.sortedBy { it.name }
                } else {
                    list.sortedByDescending { it.isFound }
                }
                _uiState.value.copy(sightings = sorted, isSortByName = sortByName)
            }.collect { newState ->
                _uiState.value = newState
            }
        }

        viewModelScope.launch {
            val saved = settingsRepository.stepBaselineFlow.first()
            stepCounterManager.setBaseline(saved)
            stepCounterManager.stepBaseline.collect { current ->
                if (current != null && current != saved) {
                    settingsRepository.setStepBaseline(current)
                }
            }
        }

        observeShakeEvents()
        observeLightSensor()
        observeStepCounter()

        shakeDetector.start()
        lightSensorManager.start()
        stepCounterManager.start()
    }

    fun startLocationUpdates() {
        locationProvider.start()
    }

    fun checkProximity(animal: Sighting): ProximityResult {
        val lat = animal.latitude
        val lon = animal.longitude
        if (lat == null || lon == null) {
            return ProximityResult.NoCoordinates(animal.name)
        }
        if (!locationProvider.hasPermission()) {
            return ProximityResult.PermissionDenied
        }
        val distance = locationProvider.distanceTo(lat, lon)
            ?: return ProximityResult.NoLocationYet

        return if (distance <= PROXIMITY_THRESHOLD_M) {
            ProximityResult.Allowed(distance)
        } else {
            ProximityResult.TooFar(animal.name, distance)
        }
    }

    private fun observeShakeEvents() {
        viewModelScope.launch {
            shakeDetector.shakeEvents.collect {
                if (_uiState.value.searchQuery.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(searchQuery = "")
                    _uiEvent.emit(ZooUiEvent.FilterClearedByShake)
                }
            }
        }
    }

    private fun observeLightSensor() {
        viewModelScope.launch {
            lightSensorManager.currentLux.collect { lux ->
                _uiState.value = _uiState.value.copy(currentLux = lux)
            }
        }
        viewModelScope.launch {
            lightSensorManager.isNocturnal.collect { nocturnal ->
                val previous = _uiState.value.isNocturnalMode
                if (previous != nocturnal) {
                    _uiState.value = _uiState.value.copy(isNocturnalMode = nocturnal)
                    _uiEvent.emit(ZooUiEvent.NocturnalModeChanged(nocturnal))
                }
            }
        }
    }

    private fun observeStepCounter() {
        viewModelScope.launch {
            combine(
                stepCounterManager.sessionSteps,
                lightSensorManager.isNocturnal
            ) { steps, nocturnal ->
                if (nocturnal) _uiState.value.stepCount else steps
            }.collect { displayed ->
                _uiState.value = _uiState.value.copy(stepCount = displayed)
            }
        }
    }
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateSighting(updated: Sighting) {
        viewModelScope.launch {
            sightingRepository.updateSighting(updated)
            _rawSightings.value = sightingRepository.loadSightings()
            _uiEvent.emit(ZooUiEvent.SightingUpdated)
        }
    }

    fun deleteSighting(sighting: Sighting) {
        viewModelScope.launch {
            sightingRepository.deleteSighting(sighting)
            _rawSightings.value = sightingRepository.loadSightings()
            _uiEvent.emit(ZooUiEvent.SightingDeleted(sighting))
        }
    }

    fun undoDelete(sighting: Sighting) {
        viewModelScope.launch {
            sightingRepository.addSighting(sighting)
            _rawSightings.value = sightingRepository.loadSightings()
        }
    }

    fun toggleSortOrder(sortByName: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSortByName(sortByName)
        }
    }

    fun selectSightingForEdit(sighting: Sighting?) {
        _uiState.value = _uiState.value.copy(
            selectedSighting = sighting,
            isDialogVisible = sighting != null
        )
    }
    fun updateCapturedImage(name: String, uri: String) {
        viewModelScope.launch {
            sightingRepository.updateCapturedImage(name, uri)
            _rawSightings.value = sightingRepository.loadSightings()
        }
    }
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            selectedSighting = null,
            isDialogVisible = false
        )
    }

    fun resetPedometer() {
        viewModelScope.launch {
            val newBaseline = stepCounterManager.currentTotalSteps()
            if (newBaseline != null) {
                stepCounterManager.setBaseline(newBaseline)
                settingsRepository.setStepBaseline(newBaseline)
                _uiEvent.emit(ZooUiEvent.PedometerReset)
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
        lightSensorManager.stop()
        stepCounterManager.stop()
        locationProvider.stop()
    }
}