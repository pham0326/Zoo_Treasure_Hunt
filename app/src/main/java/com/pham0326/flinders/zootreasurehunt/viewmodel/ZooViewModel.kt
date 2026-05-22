package com.pham0326.flinders.zootreasurehunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pham0326.flinders.zootreasurehunt.data.SettingsRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.model.ZooUiState
import com.pham0326.flinders.zootreasurehunt.sensors.LightSensorManager
import com.pham0326.flinders.zootreasurehunt.sensors.ShakeDetector
import com.pham0326.flinders.zootreasurehunt.sensors.StepCounterManager
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class ZooUiEvent {
    data class SightingDeleted(val sighting: Sighting) : ZooUiEvent()
    data object SightingUpdated : ZooUiEvent()

    /** The user shook the device while a filter was active. */
    data object FilterClearedByShake : ZooUiEvent()

    /** The user entered or left a nocturnal area (light sensor crossed threshold). */
    data class NocturnalModeChanged(val isNocturnal: Boolean) : ZooUiEvent()
}

@HiltViewModel
class ZooViewModel @Inject constructor(
    private val sightingRepository: SightingRepository,
    private val settingsRepository: SettingsRepository,
    private val shakeDetector: ShakeDetector,
    private val lightSensorManager: LightSensorManager,
    private val stepCounterManager: StepCounterManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZooUiState())
    val uiState: StateFlow<ZooUiState> = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<ZooUiEvent>()
    val uiEvent: SharedFlow<ZooUiEvent> = _uiEvent.asSharedFlow()
    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())
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

        observeShakeEvents()
        observeLightSensor()
        observeStepCounter()

        shakeDetector.start()
        lightSensorManager.start()
        stepCounterManager.start()
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

    fun selectSightingForEdit(sighting: Sighting?) {
        _uiState.value = _uiState.value.copy(
            selectedSighting = sighting,
            isDialogVisible = sighting != null
        )
    }
    fun toggleSortOrder(sortByName: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSortByName(sortByName)
        }
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

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
        lightSensorManager.stop()
        stepCounterManager.stop()
    }
}