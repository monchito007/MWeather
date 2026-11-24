package com.monchito007.mweather

import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.widget.toWidgetUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class WidgetUiModelTest {

    private val now = 10_000_000_000L

    private fun context(languageTag: String): Context {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }
        return base.createConfigurationContext(configuration)
    }

    @Test
    fun spanishModelHasLocalizedTextsAndNextFiveDays() {
        val model = weather().toWidgetUiModel(context("es"), TemperatureUnit.CELSIUS, now)

        assertEquals("Madrid", model.locationName)
        assertEquals("12°", model.temperature)
        assertEquals("☀️", model.emoji)
        assertEquals("Cielo despejado", model.description)
        assertEquals("18° / 7°", model.highLow)
        assertEquals("Actualizado hace 5 min", model.updatedAgo)
        // Today (the 23rd) is left out: the next 5 of the remaining 6 days, from Tuesday
        assertEquals(listOf("Mar", "Mié", "Jue", "Vie", "Sáb"), model.days.map { it.label })
        assertEquals("19°", model.days.first().maxTemp)
        assertEquals("8°", model.days.first().minTemp)
        assertEquals("🌧️", model.days.first().emoji)
    }

    @Test
    fun englishFahrenheitAndUnknownPlace() {
        val model = weather(name = "").toWidgetUiModel(context("en"), TemperatureUnit.FAHRENHEIT, now)

        assertEquals("Current Location", model.locationName)
        assertEquals("54°", model.temperature) // 12 °C
        assertEquals("Updated 5 min ago", model.updatedAgo)
    }

    private fun weather(name: String = "Madrid") = Weather(
        locationName = name,
        latitude = 40.4,
        longitude = -3.7,
        timezone = "Europe/Madrid",
        current = CurrentWeather(
            temperature = 12.0,
            apparentTemperature = 11.0,
            humidity = 60,
            windSpeed = 8.0,
            windDirection = 250,
            weatherCode = 0,
            weatherDescription = "Clear sky",
            isDay = true,
            precipitation = 0.0,
            pressure = 1019.0,
            cloudCover = 0
        ),
        hourly = List(8) { i ->
            HourlyForecast(
                time = "2026-11-23T${14 + i}:00",
                temperature = 12.0 - i,
                weatherCode = 1,
                weatherDescription = "Mainly clear",
                precipitationProbability = 0,
                precipitation = 0.0,
                humidity = 60,
                windSpeed = 8.0,
                isDay = true
            )
        },
        daily = List(7) { i ->
            DailyForecast(
                date = "2026-11-${23 + i}", // Monday 23rd to Sunday 29th
                weatherCode = if (i == 0) 0 else 63,
                weatherDescription = if (i == 0) "Clear sky" else "Moderate rain",
                maxTemp = 18.0 + i,
                minTemp = 7.0 + i,
                precipitationSum = 0.0,
                precipitationProbabilityMax = 0,
                windSpeedMax = 10.0,
                uvIndexMax = 2.0,
                sunrise = null,
                sunset = null
            )
        },
        fetchedAtMillis = now - 5 * 60_000L
    )
}
