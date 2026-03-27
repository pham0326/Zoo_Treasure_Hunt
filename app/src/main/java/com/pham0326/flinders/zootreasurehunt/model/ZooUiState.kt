package com.pham0326.flinders.zootreasurehunt.model

data class ZooUiState(
    val sightings: List<Sighting> = emptyList(),
    val isSortByName: Boolean = true,
    val selectedSighting: Sighting? = null,
    val isDialogVisible: Boolean = false
)