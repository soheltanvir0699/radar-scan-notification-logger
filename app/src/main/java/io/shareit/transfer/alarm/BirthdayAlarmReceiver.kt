package io.shareit.transfer.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.shareit.transfer.R
import io.shareit.transfer.SurpriseActivity
import io.shareit.transfer.util.BirthdayConfig

class BirthdayAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        var wakeLock: PowerManager.WakeLock? = null
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SuptiBirthday::ReceiverWakeLock"
            ).apply { acquire(30_000L) }
        } catch (_: Exception) {
        }

        try {
            BirthdaySurpriseService.start(context)
        } catch (_: Exception) {
        }

        try {
            postBackupFullScreenNotification(context)
        } catch (_: Exception) {
        }

        try {
            val activityIntent = Intent(context, SurpriseActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
            context.startActivity(activityIntent)
        } catch (_: Exception) {
        }

        try {
            BirthdayAlarmScheduler.schedule(
                context,
                BirthdayConfig.nextBirthdayMillis(System.currentTimeMillis() + 60_000L)
            )
        } catch (_: Exception) {
        }

        try {
            wakeLock?.takeIf { it.isHeld }?.release()
        } catch (_: Exception) {
        }
    }

    private fun postBackupFullScreenNotification(context: Context) {
        val activityIntent = Intent(context, SurpriseActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            BirthdaySurpriseService.PENDING_INTENT_REQUEST_CODE + 1,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, BirthdaySurpriseService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(
                BirthdaySurpriseService.NOTIFICATION_ID + 1,
                notification
            )
        } catch (_: SecurityException) {
        }
    }
}
