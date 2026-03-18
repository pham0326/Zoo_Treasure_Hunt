package com.pham0326.flinders.zootreasurehunt.data

import android.content.Context
import com.pham0326.flinders.zootreasurehunt.Sighting
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SightingRepository(private val context: Context) {

    private val fileName = "sightings.json"

    suspend fun saveSightings(sightings: List<Sighting>) {
        withContext(Dispatchers.IO) {
            val jsonString = Json.encodeToString(sightings)

            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
        }
    }

    private fun getDefaultSightings(): List<Sighting> {
        return listOf(
            Sighting(
                name = "Lion",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg"
            ),
            Sighting(
                name = "Red Panda",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-panda-ai.jpg"
            ),
            Sighting(
                name = "Giraffe",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/giraffe-ai.jpg"
            ),
            Sighting(
                name = "Kangaroo",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-kangaroo-ai.jpg"
            ),
            Sighting(
                name = "Penguin",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/penguin-ai.jpg"
            )
        )
    }

    suspend fun loadSightings(): List<Sighting> {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)

            if (!file.exists()) return@withContext getDefaultSightings()

            try {
                val jsonString = context.openFileInput(fileName)
                    .bufferedReader()
                    .use { it.readText() }

                Json.decodeFromString(jsonString)
            } catch (e: Exception) {
                getDefaultSightings()
            }
        }
    }
}