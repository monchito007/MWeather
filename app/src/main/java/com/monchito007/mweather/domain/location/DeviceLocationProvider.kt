package com.monchito007.mweather.domain.location

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)

interface DeviceLocationProvider {
    suspend fun getCurrentLocation(): DeviceLocation?
    fun hasLocationPermission(): Boolean
}
