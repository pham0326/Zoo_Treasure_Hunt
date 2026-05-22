package com.pham0326.flinders.zootreasurehunt.model

data class ZooUiState(
    val sightings: List<Sighting> = emptyList(),
    val isSortByName: Boolean = true,
    val selectedSighting: Sighting? = null,
    val isDialogVisible: Boolean = false,
    val searchQuery: String = "",

    val stepCount: Int = 0,
    val currentLux: Float = 100f,
    val isNocturnalMode: Boolean = false
)