package com.monchito007.mweather

import com.monchito007.mweather.data.api.OpenMeteoApi
import com.monchito007.mweather.data.cache.FileForecastCache
import com.monchito007.mweather.data.cache.ForecastCache
import com.monchito007.mweather.data.dto.CachedForecastDto
import com.monchito007.mweather.data.dto.CurrentWeatherDto
import com.monchito007.mweather.data.dto.DailyWeatherDto
import com.monchito007.mweather.data.dto.GeocodingSearchResponseDto
import com.monchito007.mweather.data.dto.HourlyWeatherDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.monchito007.mweather.data.mapper.localTimeAt
import com.monchito007.mweather.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class ForecastCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun fileCache(file: File = File(tempFolder.root, "last_forecast.json")) =
        FileForecastCache(file, ioDispatcher = Dispatchers.Unconfined)

    // ---------- FileForecastCache ----------

    @Test
    fun savedForecastIsReadBack() = runTest {
        val cache = fileCache()
        val entry = CachedForecastDto(1_000L, "Madrid", forecast())

        cache.write(entry)

        assertEquals(entry, cache.read())
    }

    @Test
    fun missingOrCorruptFileReadsAsNull() = runTest {
        val file = File(tempFolder.root, "last_forecast.json")
        assertNull(fileCache(file).read())

        file.writeText("{ not json")
        assertNull(fileCache(file).read())

        file.writeText("""{"unexpected": true}""")
        assertNull(fileCache(file).read())
    }

    // ---------- Repository + cache ----------

    @Test
    fun downloadIsCachedAndReturnedLaterWithItsDownloadTime() = runTest {
        val cache = fileCache()
        var now = MADRID_14_15_UTC_MILLIS
        WeatherRepositoryImpl(ForecastApi(), cache, clock = { now }).getWeather(40.4, -3.7, "Madrid").getOrThrow()

        now += 2 * HOUR // two hours later, on a new run of the app (without network)
        val cached = WeatherRepositoryImpl(ForecastApi(IOException("offline")), cache, clock = { now })
            .getCachedWeather()

        assertNotNull(cached)
        assertEquals("Madrid", cached!!.locationName)
        assertEquals(MADRID_14_15_UTC_MILLIS, cached.fetchedAtMillis)
        // The two hours that passed are no longer shown
        assertEquals("2026-11-23T16:00", cached.hourly.first().time)
    }

    @Test
    fun cachedForecastDropsDaysThatHavePassed() = runTest {
        val cache = fileCache()
        var now = MADRID_14_15_UTC_MILLIS
        val repository = WeatherRepositoryImpl(ForecastApi(), cache, clock = { now })
        repository.getWeather(40.4, -3.7, "Madrid").getOrThrow()

        now += 24 * HOUR
        val cached = repository.getCachedWeather()!!

        assertEquals("2026-11-24", cached.daily.first().date) // "today" is no longer the 23rd
        assertEquals(6.0, cached.current.uvIndex ?: 0.0, 0.001) // today's UV, not the 23rd's
    }

    @Test
    fun forecastOlderThanAWeekIsIgnored() = runTest {
        val cache = fileCache()
        var now = MADRID_14_15_UTC_MILLIS
        val repository = WeatherRepositoryImpl(ForecastApi(), cache, clock = { now })
        repository.getWeather(40.4, -3.7, "Madrid").getOrThrow()

        now += 8 * 24 * HOUR

        assertNull(repository.getCachedWeather())
    }

    @Test
    fun failingCacheNeverBreaksTheDownload() = runTest {
        val brokenCache = object : ForecastCache {
            override suspend fun read(): CachedForecastDto? = throw IOException("disk error")
            override suspend fun write(entry: CachedForecastDto) = throw IOException("disk full")
        }
        val repository = WeatherRepositoryImpl(ForecastApi(), brokenCache)

        assertTrue(repository.getWeather(40.4, -3.7, "Madrid").isSuccess)
        assertNull(repository.getCachedWeather())
    }

    @Test
    fun localTimeUsesTheLocationOffset() {
        assertEquals("2026-11-23T14:15", localTimeAt(MADRID_14_15_UTC_MILLIS, 3600))
        assertEquals("2026-11-23T13:15", localTimeAt(MADRID_14_15_UTC_MILLIS, 0))
        // Without an offset, falls back to the timezone ID
        assertEquals("2026-11-23T08:15", localTimeAt(MADRID_14_15_UTC_MILLIS, null, "America/New_York"))
    }

    private companion object {
        const val HOUR = 60 * 60 * 1000L
        /** 2026-11-23 13:15 UTC = 14:15 in Madrid (UTC+1 in November). */
        const val MADRID_14_15_UTC_MILLIS = 1_795_439_700_000L

        fun forecast() = OpenMeteoForecastDto(
            latitude = 40.4,
            longitude = -3.7,
            timezone = "Europe/Madrid",
            utcOffsetSeconds = 3600,
            current = CurrentWeatherDto(time = "2026-11-23T14:15", temperature2m = 12.0),
            hourly = HourlyWeatherDto(
                time = List(48) { h -> "2026-11-${23 + h / 24}T${(h % 24).toString().padStart(2, '0')}:00" }
            ),
            daily = DailyWeatherDto(
                time = listOf("2026-11-23", "2026-11-24"),
                uvIndexMax = listOf(2.0, 6.0)
            )
        )
    }

    private class ForecastApi(private val failWith: Exception? = null) : OpenMeteoApi {
        override suspend fun searchLocations(
            url: String, name: String, count: Int, language: String, format: String
        ) = GeocodingSearchResponseDto(results = null)

        override suspend fun getForecast(
            url: String, latitude: Double, longitude: Double, current: String,
            hourly: String, daily: String, timezone: String, forecastDays: Int
        ): OpenMeteoForecastDto {
            failWith?.let { throw it }
            return forecast()
        }
    }
}
