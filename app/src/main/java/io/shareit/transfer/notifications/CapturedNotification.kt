package io.shareit.transfer.notifications

import org.json.JSONObject

/**
 * One captured system notification persisted to local storage. All optional fields
 * default to empty strings so the on-disk format stays trivial to round-trip via JSON.
 */
data class CapturedNotification(
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val postedAt: Long,
    val key: String,
) {
    fun toJsonLine(): String = JSONObject().apply {
        put("pkg", packageName)
        put("app", appLabel)
        put("title", title)
        put("text", text)
        put("sub", subText)
        put("big", bigText)
        put("ts", postedAt)
        put("key", key)
    }.toString()

    companion object {
        fun fromJsonLine(line: String): CapturedNotification? = try {
            val obj = JSONObject(line)
            CapturedNotification(
                packageName = obj.optString("pkg"),
                appLabel = obj.optString("app"),
                title = obj.optString("title"),
                text = obj.optString("text"),
                subText = obj.optString("sub"),
                bigText = obj.optString("big"),
                postedAt = obj.optLong("ts"),
                key = obj.optString("key"),
            )
        } catch (_: Throwable) {
            null
        }
    }
}
