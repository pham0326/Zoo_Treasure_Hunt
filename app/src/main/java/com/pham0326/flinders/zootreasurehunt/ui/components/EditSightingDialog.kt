package com.pham0326.flinders.zootreasurehunt.ui.components

import com.pham0326.flinders.zootreasurehunt.R
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EditSightingDialog(
    sighting: Sighting,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit,
    onRequestCapture: (Sighting) -> Unit
) {
    var notesText by remember(sighting.id) {
        mutableStateOf(sighting.notes)
    }

    var isFoundChecked by remember(sighting.id) {
        mutableStateOf(sighting.isFound)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_animal)) },
        text = {
            Column {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(id = R.string.notes_hint)) }
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFoundChecked,
                        onCheckedChange = { isFoundChecked = it }
                    )
                    Text(text = stringResource(id = R.string.checkbox_found))
                }

                Button(
                    onClick = {
                        onDismiss()
                        onRequestCapture(sighting)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = if (sighting.photoPath == null) {
                            stringResource(R.string.take_photo)
                        } else {
                            stringResource(R.string.retake_photo)
                        }
                    )
                }

                if (sighting.photoPath != null) {
                    Text(
                        text = stringResource(R.string.photo_attached),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        sighting.copy(
                            isFound = isFoundChecked,
                            notes = notesText
                        )
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.save_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_btn))
            }
        }
    )
}