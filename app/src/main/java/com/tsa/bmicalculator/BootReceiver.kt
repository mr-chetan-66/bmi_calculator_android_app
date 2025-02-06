package com.tsa.bmicalculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            // Reschedule notifications here if needed
            val workManager = WorkManager.getInstance(context)
            // Example: Schedule a periodic work request for notifications.
            val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "notification_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }
    }
}
