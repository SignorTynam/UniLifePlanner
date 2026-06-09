package com.example.unilifeplanner.university.refresh

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unilifeplanner.data.datastore.UniboImportDataStore
import kotlinx.coroutines.flow.first

class UniboAutoRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val dataStore = UniboImportDataStore(applicationContext)
        val selection = dataStore.savedSelectionFlow.first() ?: return Result.success()
        val lastAutoRefreshAt = selection.lastAutoRefreshAtMillis ?: 0L
        if (System.currentTimeMillis() - lastAutoRefreshAt < AUTO_REFRESH_COOLDOWN_MILLIS) {
            return Result.success()
        }

        return runCatching {
            UniboRefreshManager(applicationContext).refreshImportedUniboData(
                source = UniboRefreshSource.APP_OPEN,
                force = true
            )
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    private companion object {
        const val AUTO_REFRESH_COOLDOWN_MILLIS = 10 * 60 * 1000L
    }
}
