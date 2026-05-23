package com.pham0326.flinders.zootreasurehunt.ui.screens
import com.pham0326.flinders.zootreasurehunt.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

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
            text = stringResource(R.string.settings_title),
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
    SectionCard(title = stringResource(R.string.settings_section_sort)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RadioButton(
                selected = isSortByName,
                onClick = { onSortChange(true) }
            )
            Text(
                text = stringResource(R.string.settings_sort_by_name),
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
                text = stringResource(R.string.settings_sort_by_status),
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
    val lightMode = if (isNocturnalMode) {
        stringResource(R.string.sensor_mode_nocturnal)
    } else {
        stringResource(R.string.sensor_mode_safari)
    }

    SectionCard(title = stringResource(R.string.settings_section_sensor_status)) {
        SensorRow(
            name = stringResource(R.string.sensor_light),
            available = isLightSensorAvailable,
            value = if (isLightSensorAvailable) {
                stringResource(R.string.sensor_light_value, currentLux.toInt(), lightMode)
            } else null
        )
        SensorRow(
            name = stringResource(R.string.sensor_step),
            available = isStepCounterAvailable,
            value = if (isStepCounterAvailable) {
                stringResource(R.string.sensor_step_value, stepCount)
            } else stringResource(R.string.sensor_step_unavailable)
        )
        SensorRow(
            name = stringResource(R.string.sensor_location),
            available = isLocationAvailable,
            value = if (isLocationAvailable) {
                stringResource(R.string.sensor_location_granted)
            } else stringResource(R.string.sensor_location_denied)
        )
        SensorRow(
            name = stringResource(R.string.sensor_camera),
            available = true,
            value = stringResource(R.string.sensor_camera_note)
        )
        SensorRow(
            name = stringResource(R.string.sensor_accelerometer),
            available = true,
            value = stringResource(R.string.sensor_accelerometer_note)
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
    SectionCard(title = stringResource(R.string.settings_section_pedometer)) {
        Text(
            text = stringResource(R.string.pedometer_session_label, stepCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Button(onClick = onResetPedometer) {
            Text(stringResource(R.string.pedometer_reset_btn))
        }
    }
}

@Composable
private fun PermissionsCard() {
    SectionCard(title = stringResource(R.string.settings_section_permissions)) {
        PermissionRow(
            permission = stringResource(R.string.perm_camera_name),
            reason = stringResource(R.string.perm_camera_reason)
        )
        PermissionRow(
            permission = stringResource(R.string.perm_notification_name),
            reason = stringResource(R.string.perm_notification_reason)
        )
        PermissionRow(
            permission = stringResource(R.string.perm_location_name),
            reason = stringResource(R.string.perm_location_reason)
        )
        PermissionRow(
            permission = stringResource(R.string.perm_activity_name),
            reason = stringResource(R.string.perm_activity_reason)
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
    SectionCard(title = stringResource(R.string.settings_section_privacy)) {
        PrivacyPoint(
            label = stringResource(R.string.privacy_storage_label),
            body = stringResource(R.string.privacy_storage_body)
        )
        PrivacyPoint(
            label = stringResource(R.string.privacy_access_label),
            body = stringResource(R.string.privacy_access_body)
        )
        PrivacyPoint(
            label = stringResource(R.string.privacy_delete_label),
            body = stringResource(R.string.privacy_delete_body)
        )
        PrivacyPoint(
            label = stringResource(R.string.privacy_gps_label),
            body = stringResource(R.string.privacy_gps_body)
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