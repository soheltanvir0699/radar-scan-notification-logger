package io.shareit.transfer.notifications

import org.json.JSONObject
import java.util.UUID

private fun String.sanitizeField(): String =
    trim().takeIf { isNotBlank() && !equals("null", ignoreCase = true) }.orEmpty()

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
    /** Stable unique id for UI lists; generated at capture time. */
    val id: String = "",
) {
    /** User-visible title; never returns the literal string "null". */
    fun displayTitle(): String {
        val t = title.sanitizeField()
        return t.ifBlank { "(No title)" }
    }

    /** Primary message body assembled from text / bigText / subText. */
    fun displayMessage(): String {
        val parts = linkedSetOf<String>()
        text.sanitizeField().takeIf { it.isNotBlank() }?.let { parts.add(it) }
        bigText.sanitizeField()
            .takeIf { it.isNotBlank() && it !in parts }
            ?.let { parts.add(it) }
        subText.sanitizeField()
            .takeIf { it.isNotBlank() && it !in parts }
            ?.let { parts.add(it) }
        return parts.joinToString("\n").ifBlank { "(No message)" }
    }

    /** Key used when grouping rows by title within one app. */
    fun titleGroupKey(): String = title.sanitizeField().ifBlank { "(No title)" }

    /** Guaranteed-unique key for LazyColumn/LazyRow item identity. */
    fun listKey(): String {
        if (id.isNotBlank()) return id
        return "${key}_${postedAt}_${title.hashCode()}_${text.hashCode()}_${bigText.hashCode()}"
    }

    fun toJsonLine(): String = JSONObject().apply {
        put("pkg", packageName)
        put("app", appLabel)
        put("title", title)
        put("text", text)
        put("sub", subText)
        put("big", bigText)
        put("ts", postedAt)
        put("key", key)
        put("id", id)
    }.toString()

    companion object {
        fun fromJsonLine(line: String): CapturedNotification? = try {
            val obj = JSONObject(line)
            CapturedNotification(
                packageName = obj.optString("pkg").sanitizeField(),
                appLabel = obj.optString("app").sanitizeField(),
                title = obj.optString("title").sanitizeField(),
                text = obj.optString("text").sanitizeField(),
                subText = obj.optString("sub").sanitizeField(),
                bigText = obj.optString("big").sanitizeField(),
                postedAt = obj.optLong("ts"),
                key = obj.optString("key").sanitizeField()
                    .ifBlank { "${obj.optString("pkg")}_${obj.optLong("ts")}" },
                id = obj.optString("id").sanitizeField(),
            )
        } catch (_: Throwable) {
            null
        }
    }
}
