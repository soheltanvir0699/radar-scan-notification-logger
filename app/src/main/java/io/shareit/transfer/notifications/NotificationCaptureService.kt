package io.shareit.transfer.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

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
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        if (title.isBlank() && text.isBlank() && subText.isBlank() && bigText.isBlank()) {
            return
        }

        val captured = CapturedNotification(
            packageName = sbn.packageName,
            appLabel = AppLabelCache.label(applicationContext, sbn.packageName),
            title = title.trim(),
            text = text.trim(),
            subText = subText.trim(),
            bigText = bigText.trim(),
            postedAt = sbn.postTime,
            key = sbn.key ?: "${sbn.packageName}_${sbn.id}_${sbn.postTime}",
        )
        NotificationStore.append(applicationContext, captured)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
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
