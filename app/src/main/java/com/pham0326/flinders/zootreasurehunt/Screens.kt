package com.pham0326.flinders.zootreasurehunt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Zoo Treasure Hunt\nCreated by\nDora Pham",
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
    }
}

@Composable
fun ListScreen(
    sightings: List<Sighting>,
    onEditClick: (Sighting) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        sightings.forEach { animal ->
            AnimalCard(sighting = animal) {
                onEditClick(animal)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}