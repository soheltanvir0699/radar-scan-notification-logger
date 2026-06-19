package io.shareit.transfer.location

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LocationCaptureWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val point = LocationCapture.capture(applicationContext) ?: return Result.retry()
        LocationStore.append(applicationContext, point)
        return Result.success()
    }
}
