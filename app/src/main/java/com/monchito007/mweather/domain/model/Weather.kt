package com.monchito007.mweather.domain.model

data class Weather(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
)
