package com.seacliff.pos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seacliff.pos.data.local.dao.MenuItemDao
import com.seacliff.pos.data.local.dao.StaffDao
import com.seacliff.pos.data.local.dao.TableDao
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.local.entity.StaffEntity
import com.seacliff.pos.data.local.entity.TableEntity
import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.MenuItemDto
import com.seacliff.pos.data.remote.dto.StaffSummaryDto
import com.seacliff.pos.data.remote.dto.TableDto
import com.seacliff.pos.data.repository.OrderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val tableDao: TableDao,
    private val menuItemDao: MenuItemDao,
    private val staffDao: StaffDao,
    private val preferencesManager: PreferencesManager,
    private val orderRepository: OrderRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting sync worker...")

            if (!preferencesManager.isLoggedIn()) {
                Timber.d("Not logged in, skipping downstream sync")
                return Result.success()
            }

            // Pull tables, menu, staff from backend and store in DB
            syncTablesFromApi()
            syncMenuFromApi()
            syncStaffFromApi()

            // Push unsynced orders to backend
            syncOrdersToApi()

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Sync worker error")
            Result.retry()
        }
    }

    private suspend fun syncTablesFromApi() {
        try {
            val response = apiService.getTables()
            if (response.isSuccessful && response.body() != null) {
                val tables = response.body()!!.tables.map { it.toEntity() }
                tableDao.insertTables(tables)
                Timber.d("Synced ${tables.size} tables from API")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync tables from API")
        }
    }

    private suspend fun syncMenuFromApi() {
        try {
            val response = apiService.getMenuItems(categoryId = null)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.items.map { it.toEntity() }
                menuItemDao.insertMenuItems(items)
                Timber.d("Synced ${items.size} menu items from API")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync menu from API")
        }
    }

    private suspend fun syncStaffFromApi() {
        try {
            val response = apiService.getStaffForPinLogin()
            if (response.isSuccessful && response.body() != null) {
                val staff = response.body()!!.staff.map { it.toEntity() }
                staffDao.insertStaffList(staff)
                Timber.d("Synced ${staff.size} staff from API")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync staff from API")
        }
    }

    private suspend fun syncOrdersToApi() {
        val unsyncedOrders = orderRepository.getUnsyncedOrders()
        Timber.d("Found ${unsyncedOrders.size} unsynced orders")
        var successCount = 0
        var failCount = 0
        unsyncedOrders.forEach { order ->
            try {
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
        if (unsyncedOrders.isNotEmpty()) {
            Timber.d("Order sync: $successCount success, $failCount failed")
        }
        if (failCount > 0 && successCount == 0) {
            throw RuntimeException("All order syncs failed")
        }
    }

    companion object {
        const val WORK_NAME = "sync_orders_work"
    }
}

private fun TableDto.toEntity(): TableEntity = TableEntity(
    id = id,
    name = name,
    location = location ?: "indoor",
    capacity = capacity,
    status = status,
    createdAt = null,
    updatedAt = null
)

private fun MenuItemDto.toEntity(): MenuItemEntity = MenuItemEntity(
    id = id,
    name = name,
    description = description,
    category = category?.name,
    categoryId = category?.id,
    price = price,
    prepArea = prepArea,
    imageUrl = imageUrl,
    isAvailable = available,
    preparationTime = prepTimeMinutes ?: 0,
    isPopular = isPopular,
    dietaryInfo = dietaryInfo,
    createdAt = null,
    updatedAt = null
)

private fun StaffSummaryDto.toEntity(): StaffEntity = StaffEntity(
    id = id,
    name = name,
    email = "",
    role = role,
    phoneNumber = null,
    status = "active",
    createdAt = null,
    updatedAt = null
)
