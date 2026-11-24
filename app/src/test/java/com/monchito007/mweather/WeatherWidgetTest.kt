package com.monchito007.mweather

import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.widget.WeatherWidget
import com.monchito007.mweather.ui.widget.weatherEmoji
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherWidgetTest {

    @Test
    fun emojiFollowsWeatherCodeAndDayOrNight() {
        assertEquals("☀️", weatherEmoji(0, isDay = true))
        assertEquals("🌙", weatherEmoji(0, isDay = false))
        assertEquals("🌧️", weatherEmoji(63, isDay = true))
        assertEquals("❄️", weatherEmoji(75, isDay = false))
        assertEquals("⛈️", weatherEmoji(95, isDay = true))
        assertEquals("🌡️", weatherEmoji(-1, isDay = true)) // unknown code
    }

    @Test
    fun forecastIsRefreshedOnlyWhenOlderThanHalfAnHour() {
        val now = 10_000_000_000L
        val minute = 60_000L

        assertFalse(WeatherWidget.needsRefresh(weather(fetchedAt = now - 29 * minute), now))
        assertTrue(WeatherWidget.needsRefresh(weather(fetchedAt = now - 30 * minute), now))
        assertTrue(WeatherWidget.needsRefresh(weather(fetchedAt = null), now)) // unknown age
    }

    private fun weather(fetchedAt: Long?) = Weather(
        locationName = "Madrid",
        latitude = 40.4,
        longitude = -3.7,
        timezone = "Europe/Madrid",
        current = CurrentWeather(
            temperature = 12.0,
            apparentTemperature = null,
            humidity = null,
            windSpeed = null,
            windDirection = null,
            weatherCode = 0,
            weatherDescription = "Clear sky",
            isDay = true,
            precipitation = null,
            pressure = null,
            cloudCover = null
        ),
        hourly = emptyList(),
        daily = emptyList(),
        fetchedAtMillis = fetchedAt
    )
}
