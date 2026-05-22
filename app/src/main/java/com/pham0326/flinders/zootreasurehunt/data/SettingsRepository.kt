package com.pham0326.flinders.zootreasurehunt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val SORT_BY_NAME = booleanPreferencesKey("sort_by_name")
        val STEP_BASELINE = floatPreferencesKey("step_baseline")
    }

    val sortByNameFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SORT_BY_NAME] ?: true
        }

    suspend fun setSortByName(isSortByName: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SORT_BY_NAME] = isSortByName
        }
    }

    val stepBaselineFlow: Flow<Float?> = context.dataStore.data
        .map { preferences ->
            preferences[STEP_BASELINE]
        }

    suspend fun setStepBaseline(value: Float?) {
        context.dataStore.edit { preferences ->
            if (value == null) {
                preferences.remove(STEP_BASELINE)
            } else {
                preferences[STEP_BASELINE] = value
            }
        }
    }
}