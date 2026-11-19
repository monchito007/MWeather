package com.monchito007.mweather.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.monchito007.mweather.domain.location.DeviceLocation
import com.monchito007.mweather.domain.location.DeviceLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class DeviceLocationProviderImpl(
    private val context: Context
) : DeviceLocationProvider {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    override suspend fun getCurrentLocation(): DeviceLocation? {
        if (!hasLocationPermission()) return null

        val location = lastKnownLocation() ?: freshLocation() ?: return null
        return DeviceLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            name = getCityName(location.latitude, location.longitude) ?: "Ubicación actual"
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnownLocation(): Location? =
        try {
            fusedLocationClient.lastLocation.awaitOrNull()
        } catch (_: SecurityException) {
            null
        }

    @SuppressLint("MissingPermission")
    private suspend fun freshLocation(): Location? {
        val cancellation = CancellationTokenSource()
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellation.token
            ).awaitOrNull { cancellation.cancel() }
        } catch (_: SecurityException) {
            null
        }
    }

    /**
     * Reverse geocoding does network I/O. It must never run on the main thread:
     * on API 33+ we use the asynchronous listener API, on older versions the
     * blocking call is moved to [Dispatchers.IO].
     */
    private suspend fun getCityName(lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())

        val address: Address? = withTimeoutOrNull(GEOCODER_TIMEOUT_MS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }
        return address?.locality ?: address?.subAdminArea ?: address?.adminArea
    }

    private companion object {
        const val GEOCODER_TIMEOUT_MS = 5_000L
    }
}

/** Awaits a Play Services [Task], resuming with null on failure or cancellation. */
private suspend fun <T> Task<T>.awaitOrNull(onCancel: () -> Unit = {}): T? =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> if (continuation.isActive) continuation.resume(result) }
        addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
        addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
        continuation.invokeOnCancellation { onCancel() }
    }
