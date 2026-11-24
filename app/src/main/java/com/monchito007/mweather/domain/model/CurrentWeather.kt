package com.monchito007.mweather.domain.model

/** Measured values are null when the API didn't provide them; the UI shows "—". */
data class CurrentWeather(
    val temperature: Double?,
    val apparentTemperature: Double?,
    val humidity: Int?,
    val windSpeed: Double?,
    val windDirection: Int?,
    val windGusts: Double? = null,
    val weatherCode: Int,
    val weatherDescription: String,
    val isDay: Boolean,
    val precipitation: Double?,
    val pressure: Double?,
    val cloudCover: Int?,
    val uvIndex: Double? = null
)
