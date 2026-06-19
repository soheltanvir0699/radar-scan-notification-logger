package io.shareit.transfer.location

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LocationScheduler {

    private const val WORK_NAME = "location_capture_periodic"
    private const val LEGACY_WORK_NAME = "location_capture_every_15m"

    fun schedule(context: Context) {
        if (!LocationAccess.hasAnyLocation(context)) return

        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)

        val request = PeriodicWorkRequestBuilder<LocationCaptureWorker>(
            30,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
