package com.monchito007.mweather.domain.repository

import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather

interface WeatherRepository {
    /** [languageCode] is an ISO 639-1 code ("es", "en") for the returned place names. */
    suspend fun searchLocations(query: String, languageCode: String = "en"): Result<List<LocationSearchResult>>
    suspend fun getWeather(latitude: Double, longitude: Double, locationName: String = ""): Result<Weather>
    suspend fun getDefaultWeather(): Result<Weather>
}
