package com.monchito007.mweather.domain.repository

import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather

interface WeatherRepository {
    /** [languageCode] is an ISO 639-1 code ("es", "en") for the returned place names. */
    suspend fun searchLocations(query: String, languageCode: String = "en"): Result<List<LocationSearchResult>>
    /**
     * [saveToCache] false is for favorite cities: the offline cache (and the widget) keep showing
     * the main city instead of whichever favorite was looked at last.
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationName: String = "",
        saveToCache: Boolean = true
    ): Result<Weather>
    suspend fun getDefaultWeather(): Result<Weather>

    /**
     * The last forecast successfully downloaded (for any location), read from disk, or null if
     * there is none or it is too old to be useful. Lets the app show something instantly and offline.
     */
    suspend fun getCachedWeather(): Weather? = null
}
