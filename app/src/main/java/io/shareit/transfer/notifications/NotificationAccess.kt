package io.shareit.transfer.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Helpers around the special "Notification access" permission used by
 * [NotificationCaptureService]. There is no runtime-permission flow for it; users
 * must enable the listener manually from system settings.
 */
object NotificationAccess {

    private const val ENABLED_LISTENERS_KEY = "enabled_notification_listeners"

    fun isGranted(context: Context): Boolean {
        val expected = ComponentName(context, NotificationCaptureService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, ENABLED_LISTENERS_KEY)
            ?: return false
        return flat.split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == expected }
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }
}
