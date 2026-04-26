package com.pham0326.flinders.zootreasurehunt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pham0326.flinders.zootreasurehunt.R
import com.pham0326.flinders.zootreasurehunt.model.Sighting

@Composable
fun AnimalCard(
    sighting: Sighting,
    onClick: () -> Unit
) {
    val animatedCardColor by animateColorAsState(
        targetValue = if (sighting.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        animationSpec = spring(),
        label = "animalCardBackground"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (sighting.isFound) Color(0xFF2E7D32) else Color.Black,
        animationSpec = spring(),
        label = "animalCardText"
    )

    val imageModel = sighting.photoPath ?: sighting.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = animatedCardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
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
                        color = Color.Gray
                    )
                }
            }

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