package com.pham0326.flinders.zootreasurehunt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.data.SettingsRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.worker.CongratulationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.pham0326.flinders.zootreasurehunt.model.ZooUiState

class ZooViewModel(
    application: Application,
    private val repository: SightingRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ZooUiState())
    val uiState: StateFlow<ZooUiState> = _uiState.asStateFlow()
    private val workManager = WorkManager.getInstance(application)

    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())
    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    val sightings: StateFlow<List<Sighting>> = _sightings.asStateFlow()

    val isSortByName = settingsRepository.sortByNameFlow

    init {
        viewModelScope.launch {
            _rawSightings.value = repository.loadSightings()
        }

        viewModelScope.launch {
            combine(_rawSightings, settingsRepository.sortByNameFlow)
            { list, sortByName ->
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

    private fun updateAndSave(newList: List<Sighting>) {
        _rawSightings.value = newList
        viewModelScope.launch {
            repository.saveSightings(newList)
        }
    }

    fun updateSighting(updated: Sighting) {
        viewModelScope.launch {
            repository.updateSighting(updated)
            _rawSightings.value = repository.loadSightings()
        }
    }

    fun deleteSighting(sighting: Sighting) {
        viewModelScope.launch {
            repository.deleteSighting(sighting)
            _rawSightings.value = repository.loadSightings()
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
}

