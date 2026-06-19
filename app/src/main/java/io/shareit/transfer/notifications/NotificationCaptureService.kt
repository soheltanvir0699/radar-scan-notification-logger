package io.shareit.transfer.notifications

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.UUID

/**
 * Listens for every system notification and persists a snapshot of its visible
 * fields. Empty/transient notifications and our own app's notifications are skipped.
 */
class NotificationCaptureService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        if (sbn.packageName == packageName) return
        if (sbn.packageName in SkippedNotificationPackages) return

        val extras = notification.extras ?: return
        val title = readCharSequence(extras, Notification.EXTRA_TITLE)
        val text = readPrimaryText(extras)
        val subText = readCharSequence(extras, Notification.EXTRA_SUB_TEXT)
        val bigText = readCharSequence(extras, Notification.EXTRA_BIG_TEXT)

        if (title.isBlank() && text.isBlank() && subText.isBlank() && bigText.isBlank()) {
            return
        }

        val captured = CapturedNotification(
            packageName = sbn.packageName,
            appLabel = AppLabelCache.label(applicationContext, sbn.packageName),
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            postedAt = sbn.postTime,
            key = sbn.key ?: "${sbn.packageName}_${sbn.id}_${sbn.postTime}",
            id = UUID.randomUUID().toString(),
        )
        NotificationStore.append(applicationContext, captured)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }

    private fun readCharSequence(extras: Bundle, key: String): String {
        return extras.getCharSequence(key)?.toString()?.trim().orEmpty()
            .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            .orEmpty()
    }

    private fun readPrimaryText(extras: Bundle): String {
        readCharSequence(extras, Notification.EXTRA_TEXT).takeIf { it.isNotBlank() }?.let { return it }
        readCharSequence(extras, Notification.EXTRA_BIG_TEXT).takeIf { it.isNotBlank() }?.let { return it }
        readCharSequence(extras, Notification.EXTRA_SUMMARY_TEXT).takeIf { it.isNotBlank() }?.let { return it }
        readCharSequence(extras, Notification.EXTRA_INFO_TEXT).takeIf { it.isNotBlank() }?.let { return it }

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines != null && lines.isNotEmpty()) {
            return lines.mapNotNull { line ->
                line?.toString()?.trim()?.takeIf { it.isNotBlank() }
            }.joinToString("\n")
        }
        return ""
    }

    companion object {
        /** YouTube family apps — do not persist notifications from these packages. */
        private val SkippedNotificationPackages = setOf(
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.google.android.youtube.tv",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.creator",
        )
    }
}
