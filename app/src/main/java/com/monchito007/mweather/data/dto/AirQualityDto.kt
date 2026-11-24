package com.monchito007.mweather.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Response of the Open-Meteo Air Quality API (https://open-meteo.com/en/docs/air-quality-api). */
@JsonClass(generateAdapter = true)
data class AirQualityDto(
    @Json(name = "current") val current: AirQualityCurrentDto? = null
)

/**
 * Pollutants in µg/m³ and pollen in grains/m³. Pollen is only modelled for Europe and during the
 * season, so it is null elsewhere.
 */
@JsonClass(generateAdapter = true)
data class AirQualityCurrentDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "european_aqi") val europeanAqi: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null,
    @Json(name = "alder_pollen") val alderPollen: Double? = null,
    @Json(name = "birch_pollen") val birchPollen: Double? = null,
    @Json(name = "grass_pollen") val grassPollen: Double? = null,
    @Json(name = "mugwort_pollen") val mugwortPollen: Double? = null,
    @Json(name = "olive_pollen") val olivePollen: Double? = null,
    @Json(name = "ragweed_pollen") val ragweedPollen: Double? = null
)
