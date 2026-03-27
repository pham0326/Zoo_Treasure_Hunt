package com.pham0326.flinders.zootreasurehunt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        val SORT_BY_NAME = booleanPreferencesKey("sort_by_name")
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
}