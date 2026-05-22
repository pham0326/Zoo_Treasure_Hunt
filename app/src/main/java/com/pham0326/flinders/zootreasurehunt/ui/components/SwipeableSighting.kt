package com.pham0326.flinders.zootreasurehunt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pham0326.flinders.zootreasurehunt.model.Sighting

@Composable
fun SwipeableSighting(
    sighting: Sighting,
    isNocturnalMode: Boolean,
    onEditClick: () -> Unit,
    onSwipe: () -> Unit,
    onCaptureClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSwipe()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)

        }
    }

    val cardShape = RoundedCornerShape(16.dp)

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape
        ) {
            AnimalCard(
                sighting = sighting,
                isNocturnalMode = isNocturnalMode,
                onClick = onEditClick,
                onCaptureClick = onCaptureClick
            )
        }
    }
}