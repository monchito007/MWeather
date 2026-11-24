package com.monchito007.mweather.domain.model

/** Measured values are null when the API didn't provide them; the UI shows "—". */
data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    val weatherDescription: String,
    val maxTemp: Double?,
    val minTemp: Double?,
    val precipitationSum: Double?,
    val precipitationProbabilityMax: Int?,
    val windSpeedMax: Double?,
    val uvIndexMax: Double?,
    val sunrise: String?,
    val sunset: String?,
    /** Snow in cm (precipitationSum is its water equivalent in mm). */
    val snowfallSum: Double? = null,
    /** Seconds of sunshine. */
    val sunshineDuration: Double? = null,
    /** Hours with precipitation. */
    val precipitationHours: Double? = null
)
