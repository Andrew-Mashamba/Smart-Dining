package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE local_id = :localId")
    suspend fun getOrderByLocalId(localId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY created_at DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE waiter_id = :waiterId ORDER BY created_at DESC")
    fun getOrdersByWaiter(waiterId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE table_id = :tableId AND status NOT IN ('completed', 'cancelled') ORDER BY created_at DESC")
    fun getActiveOrdersByTable(tableId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE DATE(created_at / 1000, 'unixepoch') = DATE('now') ORDER BY created_at DESC")
    fun getTodayOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET served_at = :servedAt WHERE id = :orderId")
    suspend fun updateServedAt(orderId: Long, servedAt: Long)

    @Query("UPDATE orders SET isSynced = 1 WHERE id = :orderId")
    suspend fun markOrderAsSynced(orderId: Long)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE DATE(created_at / 1000, 'unixepoch') < DATE('now', '-30 days')")
    suspend fun deleteOldOrders()
}
