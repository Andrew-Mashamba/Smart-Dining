package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY created_at DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE order_id = :orderId")
    fun getPaymentsByOrder(orderId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY created_at DESC")
    fun getPaymentsByStatus(status: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE DATE(created_at / 1000, 'unixepoch') = DATE('now')")
    fun getTodayPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET status = :status WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: Long, status: String)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE order_id = :orderId")
    suspend fun deletePaymentsByOrderId(orderId: Long)
}
