package com.monchito007.mweather

import com.monchito007.mweather.data.api.OpenMeteoApi
import com.monchito007.mweather.data.dto.AirQualityCurrentDto
import com.monchito007.mweather.data.dto.AirQualityDto
import com.monchito007.mweather.data.dto.CurrentWeatherDto
import com.monchito007.mweather.data.dto.DailyWeatherDto
import com.monchito007.mweather.data.dto.GeocodingLocationDto
import com.monchito007.mweather.data.dto.GeocodingSearchResponseDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.monchito007.mweather.data.repository.WeatherRepositoryImpl
import com.monchito007.mweather.domain.model.PollenType
import com.monchito007.mweather.domain.model.WeatherCategory
import com.monchito007.mweather.domain.model.WeatherCodeInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class WeatherRepositoryTest {

    @Test
    fun testGetWeatherMapsApiResponse() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi())
        val weather = repository.getWeather(40.4, -3.7, "Madrid").getOrThrow()

        assertEquals("Madrid", weather.locationName)
        assertEquals(18.5, weather.current.temperature ?: 0.0, 0.001)
        assertEquals(2, weather.daily.size)
    }

    @Test
    fun testDefaultWeatherUsesNewYork() = runTest {
        val api = FakeOpenMeteoApi()
        val weather = WeatherRepositoryImpl(api).getDefaultWeather().getOrThrow()

        assertEquals("New York", weather.locationName)
        assertEquals(40.7128, api.lastLatitude ?: 0.0, 0.001)
    }

    @Test
    fun testGetWeatherNetworkErrorReturnsFailureInsteadOfFakeData() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi(failWith = IOException("offline")))
        val result = repository.getWeather(40.4, -3.7, "Madrid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun testSearchLocationsReturnsMatches() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi())
        val locations = repository.searchLocations("London").getOrThrow()

        assertEquals(1, locations.size)
        assertEquals("London", locations.first().name)
    }

    @Test
    fun testSearchLocationsNoResultsReturnsEmptyList() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi(searchResults = null))
        val locations = repository.searchLocations("zzzz").getOrThrow()

        assertTrue(locations.isEmpty())
    }

    @Test
    fun testSearchLocationsNetworkErrorReturnsFailure() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi(failWith = IOException("offline")))

        assertTrue(repository.searchLocations("London").isFailure)
    }

    @Test
    fun testBlankSearchDoesNotHitApi() = runTest {
        val api = FakeOpenMeteoApi(failWith = IOException("should not be called"))

        assertTrue(WeatherRepositoryImpl(api).searchLocations("  ").getOrThrow().isEmpty())
    }

    @Test
    fun testWeatherCodeInfoMapping() {
        val clearInfo = WeatherCodeInfo.fromCode(0)
        assertEquals("Clear sky", clearInfo.description)
        assertEquals(WeatherCategory.CLEAR, clearInfo.category)

        val rainInfo = WeatherCodeInfo.fromCode(63)
        assertEquals("Moderate rain", rainInfo.description)
        assertEquals(WeatherCategory.RAIN, rainInfo.category)

        val unknownInfo = WeatherCodeInfo.fromCode(999)
        assertEquals("Unknown weather", unknownInfo.description)
        assertEquals(WeatherCategory.UNKNOWN, unknownInfo.category)
    }

    @Test
    fun airQualityIsMappedWithPollenPresentHighestFirst() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi())

        val airQuality = repository.getWeather(40.4, -3.7, "Madrid").getOrThrow().airQuality!!

        assertEquals(42, airQuality.europeanAqi)
        assertEquals(listOf(PollenType.OLIVE, PollenType.GRASS), airQuality.pollen.map { it.type }) // no birch, mugwort is a trace
    }

    @Test
    fun airQualityFailureStillShowsTheForecast() = runTest {
        val repository = WeatherRepositoryImpl(FakeOpenMeteoApi(airQualityFailsWith = IOException("down")))

        val weather = repository.getWeather(40.4, -3.7, "Madrid").getOrThrow()

        assertEquals(18.5, weather.current.temperature ?: 0.0, 0.001)
        assertNull(weather.airQuality)
    }
}

private class FakeOpenMeteoApi(
    private val failWith: Exception? = null,
    private val airQualityFailsWith: Exception? = null,
    private val searchResults: List<GeocodingLocationDto>? = listOf(
        GeocodingLocationDto(id = 1, name = "London", latitude = 51.5, longitude = -0.12, country = "UK")
    )
) : OpenMeteoApi {
    var lastLatitude: Double? = null

    override suspend fun searchLocations(
        url: String,
        name: String,
        count: Int,
        language: String,
        format: String
    ): GeocodingSearchResponseDto {
        failWith?.let { throw it }
        return GeocodingSearchResponseDto(results = searchResults)
    }

    override suspend fun getForecast(
        url: String,
        latitude: Double,
        longitude: Double,
        current: String,
        hourly: String,
        daily: String,
        timezone: String,
        forecastDays: Int
    ): OpenMeteoForecastDto {
        failWith?.let { throw it }
        lastLatitude = latitude
        return OpenMeteoForecastDto(
            latitude = latitude,
            longitude = longitude,
            timezone = "Europe/Madrid",
            current = CurrentWeatherDto(temperature2m = 18.5, weatherCode = 1, isDay = 1),
            daily = DailyWeatherDto(
                time = listOf("2026-11-19", "2026-11-20"),
                temperature2mMax = listOf(20.0, 21.0),
                temperature2mMin = listOf(10.0, 11.0)
            )
        )
    }

    override suspend fun getAirQuality(
        url: String,
        latitude: Double,
        longitude: Double,
        current: String,
        timezone: String
    ): AirQualityDto {
        failWith?.let { throw it }
        airQualityFailsWith?.let { throw it }
        return AirQualityDto(
            AirQualityCurrentDto(europeanAqi = 41.6, pm25 = 9.0, grassPollen = 3.0, olivePollen = 12.0, birchPollen = 0.0, mugwortPollen = 0.3)
        )
    }
}
