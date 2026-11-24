package com.monchito007.mweather.domain.model

/**
 * Current air quality. [europeanAqi] goes from 0 (best) upwards (100+ is extremely poor).
 * Pollutants are in µg/m³; null values weren't provided by the API.
 */
data class AirQuality(
    val europeanAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val nitrogenDioxide: Double?,
    val ozone: Double?,
    /** Only pollen types present right now (at least 1 grain/m³), highest first. Empty outside Europe. */
    val pollen: List<Pollen> = emptyList()
)

data class Pollen(val type: PollenType, val grainsPerCubicMetre: Double)

enum class PollenType { ALDER, BIRCH, GRASS, MUGWORT, OLIVE, RAGWEED }
