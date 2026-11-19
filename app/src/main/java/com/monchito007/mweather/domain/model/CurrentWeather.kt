package com.monchito007.mweather.domain.model

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val weatherCode: Int,
    val weatherDescription: String,
    val isDay: Boolean,
    val precipitation: Double,
    val pressure: Double,
    val cloudCover: Int,
    val uvIndex: Double = 0.0
)
