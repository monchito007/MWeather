package com.monchito007.mweather.data.preferences

import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.ThemeMode
import kotlinx.coroutines.flow.Flow

/** A city the user picked from search (restored on the next start) or saved as a favorite. */
data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String
) {
    /** Stable id for maps and pager keys. */
    val key: String get() = "%.4f,%.4f".format(java.util.Locale.US, latitude, longitude)

    /**
     * Same place give or take ~10 km: the forecast API snaps coordinates to its grid, so the
     * city on screen never has exactly the coordinates that were searched or saved.
     */
    fun isSamePlaceAs(other: SavedLocation): Boolean =
        kotlin.math.abs(latitude - other.latitude) < SAME_PLACE_DEGREES &&
            kotlin.math.abs(longitude - other.longitude) < SAME_PLACE_DEGREES

    private companion object {
        const val SAME_PLACE_DEGREES = 0.1
    }
}

data class UserPreferences(
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val language: AppLanguage = AppLanguage.SPANISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isHappyMode: Boolean = false,
    /** Null means "use the device location". */
    val lastLocation: SavedLocation? = null,
    /** Favorite cities, in the order they were added (one page each). */
    val favorites: List<SavedLocation> = emptyList()
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

    suspend fun saveFavorites(favorites: List<SavedLocation>)
}
