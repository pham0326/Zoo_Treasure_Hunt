package com.pham0326.flinders.zootreasurehunt

import java.util.UUID

data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isFound: Boolean = false,
    val notes: String = ""
)