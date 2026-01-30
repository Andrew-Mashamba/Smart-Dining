package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.OrderDao
import com.seacliff.pos.data.local.dao.OrderItemDao
import com.seacliff.pos.data.local.entity.OrderEntity
import com.seacliff.pos.data.local.entity.OrderItemEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.CreateOrderRequest
import com.seacliff.pos.data.remote.dto.OrderItemRequest
import com.seacliff.pos.data.remote.dto.UpdateStatusRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) {

    fun createOrder(
        guestId: Long,
        tableId: Long,
        items: List<OrderItemRequest>,
        notes: String? = null
    ): Flow<Resource<OrderEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreateOrderRequest(
                guestId = guestId,
                tableId = tableId,
                orderSource = "pos",
                items = items,
                notes = notes
            )

            val response = apiService.createOrder(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val order = response.body()!!.data!!

                // Save to local database
                orderDao.insertOrder(order)

                emit(Resource.Success(order))
            } else {
                // Create offline order
                val localId = UUID.randomUUID().toString()
                val offlineOrder = OrderEntity(
                    guestId = guestId,
                    tableId = tableId,
                    waiterId = 1, // Use logged-in staff ID
                    status = "pending",
                    orderSource = "pos",
                    subtotal = 0.0,
                    tax = 0.0,
                    serviceCharge = 0.0,
                    totalAmount = 0.0,
                    notes = notes,
                    isSynced = false,
                    localId = localId,
                    createdAt = Date()
                )

                val orderId = orderDao.insertOrder(offlineOrder)

                emit(Resource.Success(offlineOrder.copy(id = orderId)))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to create order: ${e.localizedMessage}"))
        }
    }

    fun getOrders(): Flow<List<OrderEntity>> {
        return orderDao.getAllOrders()
    }

    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }

    fun getTodayOrders(): Flow<List<OrderEntity>> {
        return orderDao.getTodayOrders()
    }

    suspend fun updateOrderStatus(orderId: Long, status: String): Resource<Unit> {
        return try {
            val response = apiService.updateOrderStatus(
                orderId,
                UpdateStatusRequest(status)
            )

            if (response.isSuccessful) {
                orderDao.updateOrderStatus(orderId, status)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update order status")
            }
        } catch (e: Exception) {
            // Update locally for offline support
            orderDao.updateOrderStatus(orderId, status)
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> {
        return orderItemDao.getOrderItems(orderId)
    }

    suspend fun getUnsyncedOrders(): List<OrderEntity> {
        return orderDao.getUnsyncedOrders()
    }

    suspend fun markOrderAsSynced(orderId: Long) {
        orderDao.markOrderAsSynced(orderId)
    }
}
