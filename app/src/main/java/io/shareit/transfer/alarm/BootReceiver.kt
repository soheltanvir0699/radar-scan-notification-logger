package io.shareit.transfer.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.shareit.transfer.location.LocationScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        BirthdayAlarmScheduler.schedule(context)
        LocationScheduler.schedule(context)
    }
}
