package com.pham0326.flinders.zootreasurehunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pham0326.flinders.zootreasurehunt.data.SettingsRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.model.ZooUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class ZooUiEvent {
    data class SightingDeleted(val sighting: Sighting) : ZooUiEvent()
    data object SightingUpdated : ZooUiEvent()
}
@HiltViewModel
class ZooViewModel @Inject constructor(
    private val sightingRepository: SightingRepository,
    private val settingsRepository: SettingsRepository
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
                val sortedList = if (sortByName) {
                    list.sortedBy { it.name }
                } else {
                    list.sortedByDescending { it.isFound }
                }

                _uiState.value.copy(
                    sightings = sortedList,
                    isSortByName = sortByName
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
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

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            selectedSighting = null,
            isDialogVisible = false
        )
    }

    fun updateCapturedImage(name: String, uri: String) {
        viewModelScope.launch {
            sightingRepository.updateCapturedImage(name, uri)
        }
    }

}