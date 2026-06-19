package io.shareit.transfer.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object LocationCapture {

    suspend fun capture(context: Context): LocationPoint? = withContext(Dispatchers.IO) {
        if (!LocationAccess.hasAnyLocation(context)) return@withContext null

        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val token = CancellationTokenSource()
                Tasks.await(
                    client.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        token.token
                    ),
                    30,
                    TimeUnit.SECONDS
                )
            } else {
                Tasks.await(client.lastLocation, 15, TimeUnit.SECONDS)
            }
        }.getOrNull() ?: return@withContext null

        val address = reverseGeocode(context, location.latitude, location.longitude)
        LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            capturedAt = System.currentTimeMillis(),
            address = address,
        )
    }

    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String {
        if (!Geocoder.isPresent()) return ""
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull()?.getAddressLine(0).orEmpty()) {}
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
                    .orEmpty()
            }
        }.getOrDefault("")
    }
}
