package com.pham0326.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isSortByName: Boolean,
    stepCount: Int,
    currentLux: Float,
    isNocturnalMode: Boolean,
    isLightSensorAvailable: Boolean,
    isStepCounterAvailable: Boolean,
    isLocationAvailable: Boolean,
    onSortChange: (Boolean) -> Unit,
    onResetPedometer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SortOrderCard(
            isSortByName = isSortByName,
            onSortChange = onSortChange
        )

        SensorStatusCard(
            stepCount = stepCount,
            currentLux = currentLux,
            isNocturnalMode = isNocturnalMode,
            isLightSensorAvailable = isLightSensorAvailable,
            isStepCounterAvailable = isStepCounterAvailable,
            isLocationAvailable = isLocationAvailable
        )

        PedometerControlsCard(
            stepCount = stepCount,
            onResetPedometer = onResetPedometer
        )
        PermissionsCard()
        PrivacyCard()
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun SortOrderCard(
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    SectionCard(title = "Sort Order") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RadioButton(
                selected = isSortByName,
                onClick = { onSortChange(true) }
            )
            Text(
                text = "Sort by Name",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RadioButton(
                selected = !isSortByName,
                onClick = { onSortChange(false) }
            )
            Text(
                text = "Sort by Recency / Found Status",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SensorStatusCard(
    stepCount: Int,
    currentLux: Float,
    isNocturnalMode: Boolean,
    isLightSensorAvailable: Boolean,
    isStepCounterAvailable: Boolean,
    isLocationAvailable: Boolean
) {
    SectionCard(title = "Sensor Status") {
        SensorRow(
            name = "Light sensor",
            available = isLightSensorAvailable,
            value = if (isLightSensorAvailable) {
                "${currentLux.toInt()} lux  •  ${if (isNocturnalMode) "Nocturnal" else "Safari"}"
            } else null
        )
        SensorRow(
            name = "Step counter",
            available = isStepCounterAvailable,
            value = if (isStepCounterAvailable) {
                "$stepCount steps this session only"
            } else "Not present on this device"
        )
        SensorRow(
            name = "Location (GPS)",
            available = isLocationAvailable,
            value = if (isLocationAvailable) {
                "Permission granted, used for proximity check"
            } else "Permission not granted"
        )
        SensorRow(
            name = "Camera",
            available = true,
            value = "Permission requested on capture"
        )
        SensorRow(
            name = "Accelerometer",
            available = true,
            value = "Shake to clear filter"
        )
    }
}

@Composable
private fun SensorRow(
    name: String,
    available: Boolean,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (available) "✓" else "✗",
            color = if (available) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PedometerControlsCard(
    stepCount: Int,
    onResetPedometer: () -> Unit
) {
    SectionCard(title = "Pedometer Controls") {
        Text(
            text = "Current session: $stepCount steps",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Button(onClick = onResetPedometer) {
            Text("Reset Pedometer to 0")
        }
    }
}

@Composable
private fun PermissionsCard() {
    SectionCard(title = "Permissions Used") {
        PermissionRow(
            permission = "Camera",
            reason = "Take photos of animals you find at the zoo."
        )
        PermissionRow(
            permission = "Notifications",
            reason = "Celebrate when you capture an animal."
        )
        PermissionRow(
            permission = "Location (Precise)",
            reason = "Verify you are physically near the exhibit before allowing a sighting."
        )
        PermissionRow(
            permission = "Physical Activity",
            reason = "Count steps for the gamified Safari Pedometer."
        )
    }
}

@Composable
private fun PermissionRow(permission: String, reason: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = permission,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = reason,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyCard() {
    SectionCard(title = "Data Privacy") {
        PrivacyPoint(
            "Where it is stored?",
            "All sightings, photos, and step baselines are device-only. " +
                    "No data is sent to the server."
        )
        PrivacyPoint(
            "Who can read it?",
            "Only this app."
        )
        PrivacyPoint(
            "How to delete it?",
            "Uninstalling the app removes all data, including photos."
        )
        PrivacyPoint(
            "GPS readings",
            "Location is checked only when you tap Capture, not over time."
        )
    }
}

@Composable
private fun PrivacyPoint(label: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = body,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}