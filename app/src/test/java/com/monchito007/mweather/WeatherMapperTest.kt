package com.monchito007.mweather

import com.monchito007.mweather.data.dto.CurrentWeatherDto
import com.monchito007.mweather.data.dto.DailyWeatherDto
import com.monchito007.mweather.data.dto.HourlyWeatherDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.monchito007.mweather.data.mapper.toDomain
import com.monchito007.mweather.domain.model.WeatherCodeInfo
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.WeatherUtils
import com.monchito007.mweather.ui.weather.orNoData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMapperTest {

    @Test
    fun missingValuesStayNullInsteadOfBecomingZero() {
        val dto = OpenMeteoForecastDto(
            latitude = 40.4,
            longitude = -3.7,
            current = CurrentWeatherDto(temperature2m = 14.0), // everything else missing
            hourly = HourlyWeatherDto(
                time = listOf("2026-11-19T10:00", "2026-11-19T11:00"),
                temperature2m = listOf(12.0, null) // second hour missing
            ),
            daily = DailyWeatherDto(time = listOf("2026-11-19"), temperature2mMax = listOf(18.0))
        )

        val weather = dto.toDomain("Madrid")

        with(weather.current) {
            assertEquals(14.0, temperature ?: 0.0, 0.001)
            assertNull(apparentTemperature)
            assertNull(humidity)
            assertNull(windSpeed)
            assertNull(windDirection)
            assertNull(precipitation)
            assertNull(pressure) // was a made-up 1013.25 before
            assertNull(cloudCover)
            assertNull(uvIndex)
            assertEquals(WeatherCodeInfo.UNKNOWN_CODE, weatherCode) // was 0 = "Clear sky" before
        }

        assertEquals(12.0, weather.hourly[0].temperature ?: 0.0, 0.001)
        assertNull(weather.hourly[1].temperature)
        assertNull(weather.hourly[0].precipitationProbability)

        with(weather.daily.single()) {
            assertEquals(18.0, maxTemp ?: 0.0, 0.001)
            assertNull(minTemp)
            assertNull(precipitationProbabilityMax)
            assertNull(sunrise)
            assertNull(sunset)
        }
    }

    @Test
    fun presentValuesAreMapped() {
        val dto = OpenMeteoForecastDto(
            latitude = 0.0,
            longitude = 0.0,
            current = CurrentWeatherDto(
                temperature2m = 20.5,
                relativeHumidity2m = 61.0,
                weatherCode = 63,
                pressureMsl = 1019.4,
                isDay = 0
            )
        )

        val current = dto.toDomain().current

        assertEquals(61, current.humidity)
        assertEquals(63, current.weatherCode)
        assertEquals(1019.4, current.pressure ?: 0.0, 0.001)
        assertEquals(false, current.isDay)
    }

    @Test
    fun windDegreesMapToTheNearestCompassPoint() {
        val expected = mapOf(
            0 to 0, 22 to 0, 23 to 1, 45 to 1, 90 to 2, 135 to 3, 180 to 4,
            210 to 5, 250 to 6, 315 to 7, 337 to 7, 338 to 0, 360 to 0, -45 to 7
        )
        expected.forEach { (degrees, index) ->
            assertEquals("$degrees°", index, WeatherUtils.compassPointIndex(degrees))
        }
    }

    @Test
    fun missingValuesAreFormattedAsDash() {
        assertEquals("—", WeatherUtils.NO_DATA)
        assertEquals("—", TemperatureUnit.CELSIUS.formatTemperature(null))
        assertEquals("—", TemperatureUnit.FAHRENHEIT.formatTemperature(null))
        assertEquals("21°", TemperatureUnit.CELSIUS.formatTemperature(20.6))

        val missing: Int? = null
        assertEquals("—", missing.orNoData { "$it%" })
        assertEquals("0%", 0.orNoData { "$it%" }) // a real 0 is still shown as 0
    }

    @Test
    fun unknownWeatherCodeIsNotClearSky() {
        assertEquals(R.string.weather_unknown, WeatherUtils.weatherDescriptionRes(WeatherCodeInfo.UNKNOWN_CODE))
    }
}
