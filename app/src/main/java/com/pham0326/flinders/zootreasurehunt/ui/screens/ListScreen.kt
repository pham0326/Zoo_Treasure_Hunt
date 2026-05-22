package com.pham0326.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pham0326.flinders.zootreasurehunt.R
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.ui.components.SwipeableSighting

@Composable
fun ListScreen(
    sightings: List<Sighting>,
    searchQuery: String,
    stepCount: Int,
    currentLux: Float,
    isNocturnalMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEditClick: (Sighting) -> Unit,
    onDelete: (Sighting) -> Unit,
    onCaptureClick: (Sighting) -> Unit
) {
    val filteredSightings = sightings.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val listState = rememberLazyListState()
    val estimatedDistance = (stepCount * 0.8).toInt()

    val badge = when {
        stepCount >= 1000 -> "Safari Champion 🏆"
        stepCount >= 500 -> "Trail Explorer 🦁"
        stepCount >= 100 -> "Zoo Walker 🐾"
        else -> "New Explorer 🌱"
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SafariProgressCard(
                stepCount = stepCount,
                estimatedDistance = estimatedDistance,
                badge = badge,
                currentLux = currentLux,
                isNocturnalMode = isNocturnalMode
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Filter animals") },
                placeholder = { Text("Search by animal name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
            )
        }

        if (filteredSightings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillParentMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No matching animals 🐾",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Shake the device to reset the filter.",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            items(
                items = filteredSightings,
                key = { it.id }
            ) { animal ->
                SwipeableSighting(
                    sighting = animal,
                    onEditClick = { onEditClick(animal) },
                    onSwipe = { onDelete(animal) },
                    onCaptureClick = { onCaptureClick(animal) }
                )
            }
        }
    }
}

@Composable
private fun SafariProgressCard(
    stepCount: Int,
    estimatedDistance: Int,
    badge: String,
    currentLux: Float,
    isNocturnalMode: Boolean
) {
    val backgroundColor = if (isNocturnalMode) {
        Color(0xFF1B1B2F)
    } else {
        Color(0xFFE8F5E9)
    }

    val textColor = if (isNocturnalMode) {
        Color.White
    } else {
        Color.Black
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isNocturnalMode) "🌙 Nocturnal House Mode" else "☀️ Safari Fitness Mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = "Steps: $stepCount",
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Estimated distance: ${estimatedDistance} m",
                color = textColor
            )

            Text(
                text = "Badge: $badge",
                color = textColor
            )

            Text(
                text = "Light level: ${currentLux.toInt()} Lux",
                color = textColor
            )

            if (isNocturnalMode) {
                Text(
                    text = "Rewards paused to minimise disruption around nocturnal animals.",
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

