package com.pham0326.flinders.zootreasurehunt.ui.components

import com.pham0326.flinders.zootreasurehunt.R
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.ui.theme.LocalZooSpacing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun AnimalCard(
    sighting: Sighting,
    isNocturnalMode: Boolean,
    onClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    val spacing = LocalZooSpacing.current
    val foundTint = if (isNocturnalMode) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val defaultSurface = MaterialTheme.colorScheme.surface

    val animatedCardColor by animateColorAsState(
        targetValue = if (sighting.isFound) foundTint else defaultSurface,
        animationSpec = if (isNocturnalMode) snap() else spring(),
        label = "animalCardBackground"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (sighting.isFound) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = if (isNocturnalMode) snap() else spring(),
        label = "animalCardText"
    )

    val imageModel = sighting.photoPath ?: sighting.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = if (isNocturnalMode) snap() else spring())
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = animatedCardColor)
    ) {
        Row(
            modifier = Modifier.padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = spacing.small)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sighting.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedTextColor
                )

                AnimatedVisibility(
                    visible = sighting.isFound && sighting.notes.isNotEmpty(),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Text(
                        text = sighting.notes,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.small))
            Button(onClick = onCaptureClick) {
                Text("Capture")
            }
            Spacer(modifier = Modifier.width(spacing.small))

            AnimatedVisibility(
                visible = sighting.isFound,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = stringResource(id = R.string.found_label),
                    fontWeight = FontWeight.Bold,
                    color = animatedTextColor
                )
            }
        }
    }
}