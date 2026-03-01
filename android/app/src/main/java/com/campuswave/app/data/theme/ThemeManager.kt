package com.campuswave.app.data.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Theme mode options for the app
 */
enum class ThemeMode {
    SYSTEM,  // Follow system theme
    LIGHT,   // Always light mode
    DARK     // Always dark mode
}

/**
 * Manages theme preferences with persistent storage
 */
class ThemeManager(private val context: Context) {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Flow of the current theme mode preference
     */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Save the theme mode preference
     */
    suspend fun saveThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    /**
     * Get the current theme mode synchronously
     */
    suspend fun getThemeMode(): ThemeMode {
        return themeMode.first()
    }
}
