package com.pham0326.flinders.zootreasurehunt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val SORT_BY_NAME = booleanPreferencesKey("sort_by_name")
    }

    val sortByNameFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SORT_BY_NAME] ?: true
        }

    suspend fun setSortByName(isSortByName: Boolean) {
        context.dataStore.edit {
            it[SORT_BY_NAME] = isSortByName
        }
    }
}