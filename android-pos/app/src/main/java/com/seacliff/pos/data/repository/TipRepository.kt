package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.TipDao
import com.seacliff.pos.data.local.entity.TipEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.CreateTipRequest
import com.seacliff.pos.data.remote.dto.TipDto
import com.seacliff.pos.data.remote.dto.TipSuggestionsResponse
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipRepository @Inject constructor(
    private val tipDao: TipDao,
    private val apiService: ApiService
) {

    /**
     * Convert TipDto from API to TipEntity for local storage
     */
    private fun TipDto.toEntity(): TipEntity {
        return TipEntity(
            id = this.id,
            orderId = this.orderId,
            waiterId = this.waiterId,
            amount = this.amount,
            method = this.tipMethod,
            isSynced = true,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Create a tip via the API
     */
    suspend fun createTip(
        orderId: Long,
        amount: Double,
        tipMethod: String,
        paymentId: Long? = null
    ): Resource<TipEntity> {
        return try {
            val request = CreateTipRequest(
                orderId = orderId,
                amount = amount,
                tipMethod = tipMethod,
                paymentId = paymentId
            )

            val response = apiService.createTip(request)

            if (response.isSuccessful && response.body() != null) {
                val tipResponse = response.body()!!
                val tip = tipResponse.tip.toEntity()

                // Save to local database
                tipDao.insert(tip)

                Resource.Success(tip)
            } else {
                Resource.Error("Failed to create tip")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Get tip suggestions for an order
     */
    suspend fun getTipSuggestions(orderId: Long): Resource<TipSuggestionsResponse> {
        return try {
            val response = apiService.getTipSuggestions(orderId)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to get tip suggestions")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Insert a new tip into the local database
     */
    suspend fun insertTip(tip: TipEntity): Resource<Long> {
        return try {
            val tipId = tipDao.insert(tip)
            Resource.Success(tipId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save tip")
        }
    }

    /**
     * Get all tips for a specific waiter
     */
    fun getTipsByWaiter(waiterId: Long): Flow<List<TipEntity>> {
        return tipDao.getTipsByWaiter(waiterId)
    }

    /**
     * Get tips for a waiter within a date range
     */
    suspend fun getTipsByDateRange(
        waiterId: Long,
        startDate: Long,
        endDate: Long
    ): Resource<List<TipEntity>> {
        return try {
            val tips = tipDao.getTipsByDateRange(waiterId, startDate, endDate)
            Resource.Success(tips)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch tips")
        }
    }

    /**
     * Get total tips for a waiter since a specific date
     */
    fun getTotalTipsSince(waiterId: Long, startDate: Long): Flow<Double?> {
        return tipDao.getTotalTipsSince(waiterId, startDate)
    }

    /**
     * Get total tips for a waiter (all time)
     */
    suspend fun getTotalTipsByWaiter(waiterId: Long): Resource<Double> {
        return try {
            val total = tipDao.getTotalTipsByWaiter(waiterId) ?: 0.0
            Resource.Success(total)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to calculate total tips")
        }
    }

    /**
     * Get tip count for a waiter within a date range
     */
    suspend fun getTipCountByDateRange(
        waiterId: Long,
        startDate: Long,
        endDate: Long
    ): Resource<Int> {
        return try {
            val count = tipDao.getTipCountByDateRange(waiterId, startDate, endDate)
            Resource.Success(count)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to count tips")
        }
    }

    /**
     * Get tips for a specific order
     */
    suspend fun getTipsByOrder(orderId: Long): Resource<List<TipEntity>> {
        return try {
            val tips = tipDao.getTipsByOrder(orderId)
            Resource.Success(tips)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch order tips")
        }
    }

    /**
     * Get all unsynced tips
     */
    suspend fun getUnsyncedTips(): Resource<List<TipEntity>> {
        return try {
            val tips = tipDao.getUnsyncedTips()
            Resource.Success(tips)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch unsynced tips")
        }
    }

    /**
     * Sync tips to backend
     */
    suspend fun syncTips(): Resource<Boolean> {
        return try {
            val unsyncedTips = tipDao.getUnsyncedTips()

            if (unsyncedTips.isEmpty()) {
                return Resource.Success(true)
            }

            // Sync each tip to the backend
            unsyncedTips.forEach { tip ->
                try {
                    val request = CreateTipRequest(
                        orderId = tip.orderId,
                        amount = tip.amount,
                        tipMethod = tip.method
                    )
                    val response = apiService.createTip(request)
                    if (response.isSuccessful) {
                        tipDao.markAsSynced(tip.id)
                    }
                } catch (e: Exception) {
                    // Continue with next tip if one fails
                }
            }

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync tips")
        }
    }

    /**
     * Mark a tip as synced
     */
    suspend fun markTipAsSynced(tipId: Long): Resource<Unit> {
        return try {
            tipDao.markAsSynced(tipId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark tip as synced")
        }
    }
}
