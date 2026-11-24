package com.monchito007.mweather.domain.model

/** Measured values are null when the API didn't provide them; the UI shows "—". */
data class HourlyForecast(
    val time: String,
    val temperature: Double?,
    val weatherCode: Int,
    val weatherDescription: String,
    val precipitationProbability: Int?,
    val precipitation: Double?,
    val humidity: Int?,
    val windSpeed: Double?,
    val isDay: Boolean,
    /** Snow in cm (precipitation is its water equivalent in mm). */
    val snowfall: Double? = null
)
