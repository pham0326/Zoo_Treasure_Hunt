package com.pham0326.flinders.zootreasurehunt.data
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class FileSightingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SightingRepository {

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
                isFound = false,
                notes = "",
                timestamp = System.currentTimeMillis(),
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg",
                photoPath = null,
                latitude = -34.9142,
                longitude = 138.6056
            ),
            Sighting(
                name = "Penguin",
                isFound = false,
                notes = "",
                timestamp = System.currentTimeMillis(),
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/penguin-ai.jpg",
                photoPath = null,
                latitude = -34.9120,
                longitude = 138.6075
            ),
            Sighting(
                name = "Red Panda",
                isFound = false,
                notes = "",
                timestamp = System.currentTimeMillis(),
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-panda-ai.jpg",
                photoPath = null,
                latitude = -34.9176,
                longitude = 138.6068
            ),
            Sighting(
                name = "Kangaroo",
                isFound = false,
                notes = "",
                timestamp = System.currentTimeMillis(),
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-kangaroo-ai.jpg",
                photoPath = null,
                latitude = -34.9159,
                longitude = 138.6020
            ),
            Sighting(
                name = "Giraffe",
                isFound = false,
                notes = "",
                timestamp = System.currentTimeMillis(),
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/giraffe-ai.jpg",
                photoPath = null,
                latitude = -34.9133,
                longitude = 138.6042
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

    override suspend fun updateCapturedImage(name: String, uri: String) {
        val currentList = loadSightings()

        val updatedList = currentList.map { sighting ->
            if (sighting.name == name) {
                sighting.copy(photoPath = uri)
            } else {
                sighting
            }
        }
        saveSightings(updatedList)
    }
}