package com.pham0326.flinders.zootreasurehunt.data

import android.content.Context
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class FileSightingRepository(private val context: Context) : SightingRepository {
    private val fileName = "sightings.json"

    override suspend fun saveSightings(sightings: List<Sighting>) {
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

    override suspend fun loadSightings(): List<Sighting> {
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

    override suspend fun addSighting(sighting: Sighting) {
        val currentList = loadSightings().toMutableList()
        currentList.add(sighting)
        saveSightings(currentList)
    }

    override suspend fun updateSighting(sighting: Sighting) {
        val currentList = loadSightings().map {
            if (it.id == sighting.id) sighting else it
        }
        saveSightings(currentList)
    }

    override suspend fun deleteSighting(sighting: Sighting) {
        val currentList = loadSightings().filter { it.id != sighting.id }
        saveSightings(currentList)
    }
}