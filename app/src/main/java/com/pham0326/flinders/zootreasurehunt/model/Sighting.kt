package com.pham0326.flinders.zootreasurehunt.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isFound: Boolean = false,
    val notes: String = "",
    val imageUrl: String = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg",
    val photoPath: String? = null,
    var timestamp: Long,
    var capturedImageUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)