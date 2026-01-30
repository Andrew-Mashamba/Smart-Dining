package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE id = :orderItemId")
    suspend fun getOrderItemById(orderItemId: Long): OrderItemEntity?

    @Query("SELECT * FROM order_items WHERE status = :status ORDER BY created_at")
    fun getOrderItemsByStatus(status: String): Flow<List<OrderItemEntity>>

    @Query("""
        SELECT oi.* FROM order_items oi
        INNER JOIN menu_items mi ON oi.menu_item_id = mi.id
        WHERE mi.prep_area = :prepArea AND oi.status IN ('pending', 'received', 'preparing')
        ORDER BY oi.created_at
    """)
    fun getPendingOrderItemsByPrepArea(prepArea: String): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(orderItems: List<OrderItemEntity>)

    @Update
    suspend fun updateOrderItem(orderItem: OrderItemEntity)

    @Query("UPDATE order_items SET status = :status WHERE id = :orderItemId")
    suspend fun updateOrderItemStatus(orderItemId: Long, status: String)

    @Delete
    suspend fun deleteOrderItem(orderItem: OrderItemEntity)

    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)
}
