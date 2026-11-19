package com.monchito007.mweather.domain.model

data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    val weatherDescription: String,
    val maxTemp: Double,
    val minTemp: Double,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val windSpeedMax: Double,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String
)
