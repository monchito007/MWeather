package com.monchito007.mweather.domain.repository

import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather

interface WeatherRepository {
    suspend fun searchLocations(query: String): Result<List<LocationSearchResult>>
    suspend fun getWeather(latitude: Double, longitude: Double, locationName: String = ""): Result<Weather>
    suspend fun getDefaultWeather(): Result<Weather>
}
