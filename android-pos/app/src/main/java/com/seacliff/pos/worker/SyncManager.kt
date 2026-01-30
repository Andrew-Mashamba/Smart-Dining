package com.seacliff.pos.worker

import android.content.Context
import androidx.work.*
import com.seacliff.pos.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            BuildConfig.SYNC_INTERVAL.toLong(),
            TimeUnit.SECONDS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Timber.d("Periodic sync scheduled every ${BuildConfig.SYNC_INTERVAL} seconds")
    }

    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncRequest)

        Timber.d("Immediate sync triggered")
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        Timber.d("Sync cancelled")
    }

    fun observeSyncStatus() = workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
}
