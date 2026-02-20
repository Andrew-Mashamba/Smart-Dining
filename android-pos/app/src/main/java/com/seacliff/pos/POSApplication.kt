package com.seacliff.pos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.seacliff.pos.service.AppLifecycleObserver
import com.seacliff.pos.service.PosFirebaseMessagingService
import com.seacliff.pos.worker.SyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class POSApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("SeaCliff POS Application started")

        // Initialize app lifecycle observer for FCM foreground/background detection
        AppLifecycleObserver.init()

        // Create notification channels
        PosFirebaseMessagingService.createNotificationChannels(this)

        // Setup periodic sync and trigger immediate sync (pulls tables, menu, staff from backend when logged in)
        syncManager.setupPeriodicSync()
        syncManager.triggerImmediateSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
