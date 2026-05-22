package com.pham0326.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val badge = badgeFor(stepCount, isNocturnalMode)

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

            if (isNocturnalMode) {
                NocturnalQuietBanner()
            }

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
                label = { Text(stringResource(R.string.filter_label)) },
                placeholder = { Text(stringResource(R.string.filter_placeholder)) },
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
                        text = stringResource(R.string.empty_no_match),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.empty_shake_hint),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            items(items = filteredSightings, key = { it.id }) { animal ->
                SwipeableSighting(
                    sighting = animal,
                    isNocturnalMode = isNocturnalMode,
                    onEditClick = { onEditClick(animal) },
                    onSwipe = { onDelete(animal) },
                    onCaptureClick = { onCaptureClick(animal) }
                )
            }
        }
    }
}

@Composable
private fun NocturnalQuietBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.nocturnal_banner_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.nocturnal_banner_body),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isNocturnalMode) {
                    stringResource(R.string.mode_title_nocturnal)
                } else {
                    stringResource(R.string.mode_title_safari)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.steps_label, stepCount),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = stringResource(R.string.distance_label, estimatedDistance),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.badge_label, badge),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.lux_label, currentLux.toInt()),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isNocturnalMode) {
                Text(
                    text = stringResource(R.string.nocturnal_rewards_paused),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun badgeFor(stepCount: Int, isNocturnalMode: Boolean): String {
    if (isNocturnalMode) {

        return when {
            stepCount >= 1000 -> "Champion (quiet mode)"
            stepCount >= 500 -> "Explorer (quiet mode)"
            stepCount >= 100 -> "Walker (quiet mode)"
            else -> "Resting"
        }
    }
    return when {
        stepCount >= 1000 -> "Safari Champion 🏆"
        stepCount >= 500 -> "Trail Explorer 🦁"
        stepCount >= 100 -> "Zoo Walker 🚶🏻‍♀️"
        else -> "New Explorer 🌱"
    }
}