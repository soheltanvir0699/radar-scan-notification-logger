package io.shareit.transfer.location

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LocationScheduler {

    private const val WORK_NAME = "location_capture_every_15m"

    fun schedule(context: Context) {
        if (!LocationAccess.hasAnyLocation(context)) return

        val request = PeriodicWorkRequestBuilder<LocationCaptureWorker>(
            15,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
