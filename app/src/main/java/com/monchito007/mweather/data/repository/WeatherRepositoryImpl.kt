package com.monchito007.mweather.data.repository

import com.monchito007.mweather.data.api.ApiClient
import com.monchito007.mweather.data.api.OpenMeteoApi
import com.monchito007.mweather.data.mapper.toDomain
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException

/**
 * Network failures are reported as [Result.failure] so the UI can tell the user,
 * instead of silently showing made-up data. The ViewModel keeps the last
 * successfully loaded weather on screen while showing the error.
 */
class WeatherRepositoryImpl(
    private val api: OpenMeteoApi = ApiClient.openMeteoApi,
) : WeatherRepository {

    override suspend fun searchLocations(
        query: String,
        languageCode: String
    ): Result<List<LocationSearchResult>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return runCatchingCancellable {
            api.searchLocations(name = query, language = languageCode).results?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Result<Weather> = runCatchingCancellable {
        api.getForecast(latitude = latitude, longitude = longitude)
            .toDomain(fallbackLocationName = locationName)
    }

    override suspend fun getDefaultWeather(): Result<Weather> =
        getWeather(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_LOCATION_NAME)

    /** Like [runCatching], but never swallows coroutine cancellation. */
    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private companion object {
        const val DEFAULT_LATITUDE = 40.7128
        const val DEFAULT_LONGITUDE = -74.0060
        const val DEFAULT_LOCATION_NAME = "New York"
    }
}
