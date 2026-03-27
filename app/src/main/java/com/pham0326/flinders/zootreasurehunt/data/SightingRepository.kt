package com.pham0326.flinders.zootreasurehunt.data

import com.pham0326.flinders.zootreasurehunt.model.Sighting

interface SightingRepository {
    suspend fun saveSightings(sightings: List<Sighting>)
    suspend fun loadSightings(): List<Sighting>
    suspend fun addSighting(sighting: Sighting)
    suspend fun updateSighting(sighting: Sighting)
    suspend fun deleteSighting(sighting: Sighting)
}