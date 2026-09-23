package org.dastakvani.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dastakvani.app.domain.model.ComplaintLocation
import java.util.Locale
import kotlin.coroutines.resume

class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): ComplaintLocation {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Explicit honest fallback when GPS hardware is switched off
            return ComplaintLocation(
                latitude = 0.0,
                longitude = 0.0,
                address = "स्थान सेवा बंद है (Location service disabled)",
                district = "Madhubani"
            )
        }

        val location = fetchFusedLocation() ?: fetchLastKnownLocation()

        return if (location != null) {
            val (address, district) = reverseGeocode(location.latitude, location.longitude)
            ComplaintLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                district = district
            )
        } else {
            // Location could not be resolved by satellite/network
            ComplaintLocation(
                latitude = 0.0,
                longitude = 0.0,
                address = "जीपीएस सिग्नल अनुपलब्ध (GPS signal unavailable)",
                district = "Madhubani"
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFusedLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val cts = CancellationTokenSource()
        continuation.invokeOnCancellation { cts.cancel() }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                continuation.resume(loc)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                continuation.resume(loc)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    private fun reverseGeocode(lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("hi", "IN"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val addr = addresses?.firstOrNull()
            if (addr != null) {
                val villageOrLocality = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: "ग्रामीण क्षेत्र"
                val district = addr.subAdminArea ?: addr.adminArea ?: "Madhubani"
                val fullAddress = "${addr.getAddressLine(0) ?: villageOrLocality}, $district"
                Pair(fullAddress, district)
            } else {
                Pair("अक्षांश: ${String.format("%.4f", lat)}, देशांतर: ${String.format("%.4f", lng)}", "Madhubani")
            }
        } catch (e: Exception) {
            Pair("अक्षांश: ${String.format("%.4f", lat)}, देशांतर: ${String.format("%.4f", lng)}", "Madhubani")
        }
    }
}
