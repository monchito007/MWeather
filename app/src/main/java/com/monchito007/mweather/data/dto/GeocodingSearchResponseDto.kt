package com.monchito007.mweather.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponseDto(
    @Json(name = "results") val results: List<GeocodingLocationDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingLocationDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)
