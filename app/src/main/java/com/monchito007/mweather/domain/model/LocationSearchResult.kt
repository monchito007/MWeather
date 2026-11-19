package com.monchito007.mweather.domain.model

data class LocationSearchResult(
    val id: Long,
    val name: String,
    val country: String? = null,
    val state: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null
) {
    val displayLocation: String
        get() {
            val parts = listOfNotNull(name, state, country).filter { it.isNotBlank() }
            return if (parts.isNotEmpty()) parts.joinToString(", ") else name
        }
}
