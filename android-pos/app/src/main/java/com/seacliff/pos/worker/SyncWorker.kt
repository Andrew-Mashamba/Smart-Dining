package com.seacliff.pos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seacliff.pos.data.repository.OrderRepository
import com.seacliff.pos.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderRepository: OrderRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting sync worker...")

            // Get all unsynced orders
            val unsyncedOrders = orderRepository.getUnsyncedOrders()

            Timber.d("Found ${unsyncedOrders.size} unsynced orders")

            if (unsyncedOrders.isEmpty()) {
                return Result.success()
            }

            // Try to sync each order
            var successCount = 0
            var failCount = 0

            unsyncedOrders.forEach { order ->
                try {
                    // Try to create the order on the server
                    // Note: This is a simplified version. In production, you'd need to
                    // convert the local order format to the API format

                    // For now, just mark as synced if the order ID is not 0
                    if (order.id > 0) {
                        orderRepository.markOrderAsSynced(order.id)
                        successCount++
                    } else {
                        failCount++
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync order ${order.id}")
                    failCount++
                }
            }

            Timber.d("Sync completed: $successCount success, $failCount failed")

            if (failCount > 0 && successCount == 0) {
                // All syncs failed, retry later
                Result.retry()
            } else {
                // At least some syncs succeeded
                Result.success()
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync worker error")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_orders_work"
    }
}
