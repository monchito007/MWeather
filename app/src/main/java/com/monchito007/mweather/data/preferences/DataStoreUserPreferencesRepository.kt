package com.monchito007.mweather.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Must be a single instance per process: top-level delegate guarantees that.
private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            // A corrupted/unreadable file must not crash the app: fall back to defaults.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toUserPreferences() }

    override suspend fun saveDisplaySettings(
        unit: TemperatureUnit,
        language: AppLanguage,
        themeMode: ThemeMode,
        isHappyMode: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.UNIT] = unit.name
            prefs[Keys.LANGUAGE] = language.name
            prefs[Keys.THEME_MODE] = themeMode.name
            prefs[Keys.HAPPY_MODE] = isHappyMode
        }
    }

    override suspend fun saveLastLocation(location: SavedLocation?) {
        dataStore.edit { prefs ->
            if (location == null) {
                prefs.remove(Keys.LAST_LATITUDE)
                prefs.remove(Keys.LAST_LONGITUDE)
                prefs.remove(Keys.LAST_NAME)
            } else {
                prefs[Keys.LAST_LATITUDE] = location.latitude
                prefs[Keys.LAST_LONGITUDE] = location.longitude
                prefs[Keys.LAST_NAME] = location.name
            }
        }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences()
        val latitude = this[Keys.LAST_LATITUDE]
        val longitude = this[Keys.LAST_LONGITUDE]
        return UserPreferences(
            unit = enumOrDefault(this[Keys.UNIT], defaults.unit),
            language = enumOrDefault(this[Keys.LANGUAGE], defaults.language),
            themeMode = enumOrDefault(this[Keys.THEME_MODE], defaults.themeMode),
            isHappyMode = this[Keys.HAPPY_MODE] ?: defaults.isHappyMode,
            lastLocation = if (latitude != null && longitude != null) {
                SavedLocation(latitude, longitude, this[Keys.LAST_NAME].orEmpty())
            } else {
                null
            }
        )
    }

    /** Unknown values (e.g. an enum constant renamed in a later version) fall back to the default. */
    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private object Keys {
        val UNIT = stringPreferencesKey("temperature_unit")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPPY_MODE = booleanPreferencesKey("happy_mode")
        val LAST_LATITUDE = doublePreferencesKey("last_location_latitude")
        val LAST_LONGITUDE = doublePreferencesKey("last_location_longitude")
        val LAST_NAME = stringPreferencesKey("last_location_name")
    }

    companion object {
        fun create(context: Context): DataStoreUserPreferencesRepository =
            DataStoreUserPreferencesRepository(context.applicationContext.userPreferencesDataStore)
    }
}
