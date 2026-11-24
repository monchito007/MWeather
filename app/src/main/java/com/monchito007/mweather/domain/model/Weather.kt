package com.monchito007.mweather.domain.model

data class Weather(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** When this forecast was downloaded (epoch millis), or null if unknown (previews, tests). */
    val fetchedAtMillis: Long? = null,
    /** Null when the air quality couldn't be downloaded; the rest of the forecast still shows. */
    val airQuality: AirQuality? = null,
)
