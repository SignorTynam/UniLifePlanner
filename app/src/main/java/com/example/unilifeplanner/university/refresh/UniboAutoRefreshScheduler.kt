package com.example.unilifeplanner.university.refresh

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object UniboAutoRefreshScheduler {
    private const val WORK_NAME = "unibo_auto_refresh_on_app_open"

    fun scheduleOnAppOpen(context: Context) {
        val request = OneTimeWorkRequestBuilder<UniboAutoRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
