package com.monchito007.mweather.data.repository

import com.monchito007.mweather.data.api.OpenMeteoApi
import com.monchito007.mweather.data.cache.ForecastCache
import com.monchito007.mweather.data.dto.CachedForecastDto
import com.monchito007.mweather.data.mapper.localTimeAt
import com.monchito007.mweather.data.mapper.toDomain
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException

/**
 * Network failures are reported as [Result.failure] so the UI can tell the user,
 * instead of silently showing made-up data. The ViewModel keeps the last
 * successfully loaded weather on screen while showing the error.
 *
 * Every successful download is saved to [cache] (when given), so [getCachedWeather] can show it
 * on the next start, before the network answers or without a connection at all.
 */
class WeatherRepositoryImpl(
    private val api: OpenMeteoApi,
    private val cache: ForecastCache? = null,
    private val clock: () -> Long = System::currentTimeMillis,
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
        val forecast = api.getForecast(latitude = latitude, longitude = longitude)
        val fetchedAt = clock()
        // A cache that can't be written (e.g. disk full) must not hide the fresh forecast.
        runCatchingCancellable { cache?.write(CachedForecastDto(fetchedAt, locationName, forecast)) }
        forecast.toDomain(fallbackLocationName = locationName, fetchedAtMillis = fetchedAt)
    }

    override suspend fun getCachedWeather(): Weather? {
        val entry = runCatchingCancellable { cache?.read() }.getOrNull() ?: return null
        val now = clock()
        if (now - entry.fetchedAtMillis > MAX_CACHE_AGE_MILLIS) return null // whole forecast is past
        val forecast = entry.forecast
        return forecast.toDomain(
            fallbackLocationName = entry.locationName,
            fetchedAtMillis = entry.fetchedAtMillis,
            // Not the cached "current" time: hours and days passed since the download are dropped.
            localNow = localTimeAt(now, forecast.utcOffsetSeconds, forecast.timezone)
        )
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

        /** The API returns 7 days, so an older forecast has nothing left to show. */
        const val MAX_CACHE_AGE_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
