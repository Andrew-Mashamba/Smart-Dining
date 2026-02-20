package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.TipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tip: TipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tips: List<TipEntity>)

    @Update
    suspend fun update(tip: TipEntity)

    @Delete
    suspend fun delete(tip: TipEntity)

    @Query("SELECT * FROM tips WHERE id = :tipId")
    suspend fun getTipById(tipId: Long): TipEntity?

    @Query("SELECT * FROM tips WHERE order_id = :orderId")
    suspend fun getTipsByOrder(orderId: Long): List<TipEntity>

    @Query("SELECT * FROM tips WHERE waiter_id = :waiterId ORDER BY created_at DESC")
    fun getTipsByWaiter(waiterId: Long): Flow<List<TipEntity>>

    @Query("""
        SELECT * FROM tips
        WHERE waiter_id = :waiterId
        AND created_at >= :startDate
        AND created_at <= :endDate
        ORDER BY created_at DESC
    """)
    suspend fun getTipsByDateRange(
        waiterId: Long,
        startDate: Long,
        endDate: Long
    ): List<TipEntity>

    @Query("SELECT SUM(amount) FROM tips WHERE waiter_id = :waiterId AND created_at >= :startDate")
    fun getTotalTipsSince(waiterId: Long, startDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM tips WHERE waiter_id = :waiterId")
    suspend fun getTotalTipsByWaiter(waiterId: Long): Double?

    @Query("""
        SELECT COUNT(*) FROM tips
        WHERE waiter_id = :waiterId
        AND created_at >= :startDate
        AND created_at <= :endDate
    """)
    suspend fun getTipCountByDateRange(
        waiterId: Long,
        startDate: Long,
        endDate: Long
    ): Int

    @Query("SELECT * FROM tips WHERE is_synced = 0")
    suspend fun getUnsyncedTips(): List<TipEntity>

    @Query("UPDATE tips SET is_synced = 1 WHERE id = :tipId")
    suspend fun markAsSynced(tipId: Long)

    @Query("UPDATE tips SET is_synced = 0 WHERE id = :tipId")
    suspend fun markAsUnsynced(tipId: Long)

    @Query("DELETE FROM tips WHERE waiter_id = :waiterId")
    suspend fun deleteAllByWaiter(waiterId: Long)

    @Query("""
        SELECT tips.* FROM tips
        INNER JOIN orders ON tips.order_id = orders.id
        WHERE orders.table_id = :tableId
        ORDER BY tips.created_at DESC
    """)
    suspend fun getTipsByTable(tableId: Long): List<TipEntity>
}
