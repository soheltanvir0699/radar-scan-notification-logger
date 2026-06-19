package io.shareit.transfer.location

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val capturedAt: Long,
    val address: String,
) {
    fun displayTime(): String =
        SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(capturedAt))

    fun displayCoordinates(): String =
        "%.5f, %.5f".format(Locale.US, latitude, longitude)

    fun displayAddress(): String =
        address.trim().ifBlank { displayCoordinates() }

    fun toJsonLine(): String = JSONObject().apply {
        put("lat", latitude)
        put("lng", longitude)
        put("acc", accuracyMeters.toDouble())
        put("ts", capturedAt)
        put("addr", address)
    }.toString()

    companion object {
        fun fromJsonLine(line: String): LocationPoint? = try {
            val obj = JSONObject(line)
            LocationPoint(
                latitude = obj.getDouble("lat"),
                longitude = obj.getDouble("lng"),
                accuracyMeters = obj.optDouble("acc", 0.0).toFloat(),
                capturedAt = obj.getLong("ts"),
                address = obj.optString("addr", ""),
            )
        } catch (_: Exception) {
            null
        }
    }
}
