package com.monchito007.mweather.data.preferences

import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.ThemeMode
import kotlinx.coroutines.flow.Flow

/** A city the user picked from search, restored on the next app start. */
data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String
)

data class UserPreferences(
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val language: AppLanguage = AppLanguage.SPANISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isHappyMode: Boolean = false,
    /** Null means "use the device location". */
    val lastLocation: SavedLocation? = null
)

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun saveDisplaySettings(
        unit: TemperatureUnit,
        language: AppLanguage,
        themeMode: ThemeMode,
        isHappyMode: Boolean
    )

    /** Pass null to go back to using the device location on start. */
    suspend fun saveLastLocation(location: SavedLocation?)
}
