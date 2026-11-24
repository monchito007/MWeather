package com.monchito007.mweather.ui.weather

import androidx.annotation.StringRes
import com.monchito007.mweather.data.preferences.SavedLocation
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import kotlin.math.roundToInt

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT;

    fun convertTemperature(celsius: Double): Double {
        return when (this) {
            CELSIUS -> celsius
            FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
        }
    }

    fun formatTemperature(celsius: Double?): String {
        if (celsius == null) return WeatherUtils.NO_DATA
        val converted = convertTemperature(celsius)
        return "${converted.roundToInt()}°"
    }

    val symbol: String
        get() = when (this) {
            CELSIUS -> "°C"
            FAHRENHEIT -> "°F"
        }
}

enum class AppLanguage(
    val code: String,
    val displayName: String
) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English")
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class WeatherUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val weather: Weather? = null,
    /** Error to show, as a string resource so it follows the active app language. */
    @StringRes val errorMessageRes: Int? = null,
    val searchQuery: String = "",
    val searchResults: List<LocationSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false,
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val language: AppLanguage = AppLanguage.SPANISH,
    val selectedLocation: LocationSearchResult? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isHappyMode: Boolean = false,
    /** False until the saved settings have been read, to avoid flashing the default theme. */
    val preferencesLoaded: Boolean = false,
    /** Favorite cities, one page each after the main page. */
    val favorites: List<SavedLocation> = emptyList(),
    /** Weather of each favorite by [SavedLocation.key], downloaded when its page is shown. */
    val favoriteWeather: Map<String, Weather> = emptyMap(),
    val loadingFavorites: Set<String> = emptySet(),
    val failedFavorites: Set<String> = emptySet()
) {
    /** The city on the main page, as it would be saved as a favorite, or null if none is shown. */
    val mainCity: SavedLocation?
        get() = selectedLocation?.let { SavedLocation(it.latitude, it.longitude, it.displayLocation) }
            ?: weather?.let { SavedLocation(it.latitude, it.longitude, it.locationName) }

    fun isFavorite(city: SavedLocation): Boolean = favorites.any { it.isSamePlaceAs(city) }

    val isDarkTheme: Boolean?
        get() = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> null
        }
    val currentWeather: CurrentWeather?
        get() = weather?.current

    val dailyForecast: List<DailyForecast>
        get() = weather?.daily ?: emptyList()

    val hourlyForecast: List<HourlyForecast>
        get() = weather?.hourly ?: emptyList()

    val locationName: String
        get() = weather?.locationName ?: selectedLocation?.displayLocation ?: ""

    fun formatTemp(celsius: Double?): String {
        return unit.formatTemperature(celsius)
    }
}
