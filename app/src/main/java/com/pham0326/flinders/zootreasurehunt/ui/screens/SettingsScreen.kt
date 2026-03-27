package com.pham0326.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = "Settings")
        Text(
            text = "Sort Order",
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            RadioButton(
                selected = isSortByName,
                onClick = { onSortChange(true) }
            )
            Text(text = "Sort by Name")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            RadioButton(
                selected = !isSortByName,
                onClick = { onSortChange(false) }
            )
            Text(text = "Sort by Recency / Found Status")
        }
    }
}