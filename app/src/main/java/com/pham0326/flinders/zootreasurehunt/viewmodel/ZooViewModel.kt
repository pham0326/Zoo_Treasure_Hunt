package com.pham0326.flinders.zootreasurehunt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pham0326.flinders.zootreasurehunt.Sighting
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.worker.CongratulationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ZooViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SightingRepository(application)
    private val workManager = WorkManager.getInstance(application)

    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    val sightings: StateFlow<List<Sighting>> = _sightings.asStateFlow()

    init {
        viewModelScope.launch {
            _sightings.value = repository.loadSightings()
        }
    }

    private fun updateAndSave(newList: List<Sighting>) {
        _sightings.value = newList
        viewModelScope.launch {
            repository.saveSightings(newList)
        }
    }

    fun updateSighting(updated: Sighting) {
        val oldSighting = _sightings.value.find { it.id == updated.id }

        if (updated.isFound && oldSighting?.isFound == false) {
            val workRequest = OneTimeWorkRequestBuilder<CongratulationWorker>()
                .setInputData(workDataOf("ANIMAL_NAME" to updated.name))
                .build()

            workManager.enqueue(workRequest)
        }

        val newList = _sightings.value.map {
            if (it.id == updated.id) updated else it
        }

        updateAndSave(newList)
    }

    fun deleteSighting(sighting: Sighting) {
        val newList = _sightings.value.filter { it.id != sighting.id }
        updateAndSave(newList)
    }
}