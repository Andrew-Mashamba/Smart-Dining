package com.seacliff.pos.data.repository

import com.google.gson.Gson
import com.seacliff.pos.data.local.dao.GuestDao
import com.seacliff.pos.data.local.dao.OrderDao
import com.seacliff.pos.data.local.dao.OrderItemDao
import com.seacliff.pos.data.local.entity.GuestEntity
import com.seacliff.pos.data.local.entity.OrderEntity
import com.seacliff.pos.data.local.entity.OrderItemEntity
import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.ApiErrorBody
import com.seacliff.pos.data.remote.dto.CancelOrderRequest
import com.seacliff.pos.data.remote.dto.CreateOrderRequest
import com.seacliff.pos.data.remote.dto.OrderDto
import com.seacliff.pos.data.remote.dto.OrderItemRequest
import com.seacliff.pos.data.remote.dto.OrderSummaryDetailDto
import com.seacliff.pos.data.remote.dto.UpdateStatusRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** Used to persist order line items locally when creating an order (API or offline). */
data class OrderItemDetail(
    val menuItemId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String? = null
)

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val guestDao: GuestDao,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {

    private fun parseCreatedAt(s: String?): Date? {
        if (s.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(s)
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(s)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Convert OrderDto from API to OrderEntity for local storage
     */
    private fun OrderDto.toEntity(): OrderEntity {
        return OrderEntity(
            id = this.id,
            guestId = this.guestId,
            tableId = this.tableId,
            waiterId = this.waiterId,
            sessionId = this.sessionId,
            status = this.status,
            orderSource = this.orderSource,
            subtotal = this.subtotal,
            tax = this.tax,
            serviceCharge = 0.0,
            totalAmount = this.total,
            notes = this.notes ?: this.specialInstructions,
            createdAt = parseCreatedAt(this.createdAt) ?: Date(),
            updatedAt = null,
            servedAt = null,
            isSynced = true,
            localId = null,
            tableName = this.table?.name,
            waiterName = this.waiter?.name
        )
    }

    /**
     * Convert OrderSummaryDetailDto to OrderEntity
     */
    private fun OrderSummaryDetailDto.toEntity(guestId: Long, tableId: Long, waiterId: Long): OrderEntity {
        val totalAmount = this.totals.totalAmount?.takeIf { it > 0 }
            ?: (this.totals.subtotal + this.totals.tax)
        return OrderEntity(
            id = this.orderId,
            guestId = guestId,
            tableId = tableId,
            waiterId = waiterId,
            sessionId = null,
            status = this.status,
            orderSource = "pos",
            subtotal = this.totals.subtotal,
            tax = this.totals.tax,
            serviceCharge = this.totals.serviceCharge ?: 0.0,
            totalAmount = totalAmount,
            notes = null,
            createdAt = parseCreatedAt(this.createdAt) ?: Date(),
            updatedAt = null,
            servedAt = null,
            isSynced = true,
            localId = null,
            tableName = this.table,
            waiterName = this.waiter
        )
    }

    fun createOrder(
        guestId: Long,
        tableId: Long,
        items: List<OrderItemRequest>,
        notes: String? = null,
        localOrderItems: List<OrderItemDetail>
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

            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!
                val waiterId = preferencesManager.getStaffId()

                val order = orderResponse.order.toEntity(guestId, tableId, waiterId)

                orderDao.insertOrder(order)

                saveOrderItemsLocally(order.id, localOrderItems)

                emit(Resource.Success(order))
            } else {
                val errorMessage = if (response.code() == 401) {
                    "Session expired. Please log in again."
                } else {
                    parseErrorResponse(response.errorBody())
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            try {
                ensureDefaultGuestExists()
                val localId = UUID.randomUUID().toString()
                val waiterId = preferencesManager.getStaffId()
                val effectiveGuestId = if (guestDao.getGuestById(guestId) != null) guestId else 1L
                val offlineOrder = OrderEntity(
                    guestId = effectiveGuestId,
                    tableId = tableId,
                    waiterId = waiterId,
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
                saveOrderItemsLocally(orderId, localOrderItems)

                emit(Resource.Success(offlineOrder.copy(id = orderId)))
            } catch (localEx: Exception) {
                val networkHint = when {
                    e is java.net.SocketTimeoutException -> "Request timed out. "
                    e is java.net.UnknownHostException -> "No connection. "
                    else -> ""
                }
                emit(Resource.Error("${networkHint}Could not save order. Please try again."))
            }
        }
    }

    private fun parseErrorResponse(errorBody: ResponseBody?): String {
        if (errorBody == null) return "Order failed"
        return try {
            val error = gson.fromJson(errorBody.charStream(), ApiErrorBody::class.java)
            error.message ?: "Order failed"
        } catch (_: Exception) {
            "Order failed"
        }
    }

    private suspend fun ensureDefaultGuestExists() {
        if (guestDao.getGuestById(1L) == null) {
            val now = Date()
            guestDao.insertGuest(
                GuestEntity(
                    id = 1L,
                    phoneNumber = "",
                    name = "Walk-in Guest",
                    firstVisitAt = now,
                    lastVisitAt = now
                )
            )
        }
    }

    private suspend fun saveOrderItemsLocally(orderId: Long, details: List<OrderItemDetail>) {
        val now = Date()
        val entities = details.map { d ->
            OrderItemEntity(
                orderId = orderId,
                menuItemId = d.menuItemId,
                quantity = d.quantity,
                unitPrice = d.unitPrice,
                subtotal = d.unitPrice * d.quantity,
                status = "pending",
                notes = d.notes,
                createdAt = now,
                updatedAt = now
            )
        }
        orderItemDao.insertOrderItems(entities)
    }

    fun getOrders(): Flow<List<OrderEntity>> {
        return orderDao.getAllOrders()
    }

    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }

    fun getActiveOrdersByTable(tableId: Long): Flow<List<OrderEntity>> {
        return orderDao.getActiveOrdersByTable(tableId)
    }

    fun getTodayOrders(): Flow<List<OrderEntity>> {
        return orderDao.getTodayOrders()
    }

    /**
     * Fetch orders directly from API without local storage.
     * Returns orders as entities for display.
     */
    suspend fun fetchOrdersFromApi(): Resource<List<OrderEntity>> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful && response.body() != null) {
                val paginatedResponse = response.body()!!
                val orders = paginatedResponse.data.map { it.toEntity() }
                Resource.Success(orders)
            } else {
                Resource.Error("Failed to fetch orders: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error fetching orders")
        }
    }

    /**
     * Fetch orders by status directly from API.
     */
    suspend fun fetchOrdersByStatusFromApi(status: String): Resource<List<OrderEntity>> {
        return try {
            val response = apiService.getOrders(status = status)
            if (response.isSuccessful && response.body() != null) {
                val paginatedResponse = response.body()!!
                val orders = paginatedResponse.data.map { it.toEntity() }
                Resource.Success(orders)
            } else {
                Resource.Error("Failed to fetch orders: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error fetching orders")
        }
    }

    suspend fun getOrderById(orderId: Long): Resource<OrderEntity> {
        return try {
            // Try API first
            val response = apiService.getOrder(orderId)
            if (response.isSuccessful && response.body() != null) {
                val orderDetail = response.body()!!
                val order = orderDetail.toEntity(
                    guestId = 1L, // Default guest
                    tableId = 0L, // Will be resolved from table name
                    waiterId = preferencesManager.getStaffId()
                )
                Resource.Success(order)
            } else {
                // Fallback to local
                val order = orderDao.getOrderById(orderId)
                if (order != null) {
                    Resource.Success(order)
                } else {
                    Resource.Error("Order not found")
                }
            }
        } catch (e: Exception) {
            // Fallback to local on network error
            val order = orderDao.getOrderById(orderId)
            if (order != null) {
                Resource.Success(order)
            } else {
                Resource.Error(e.message ?: "Failed to fetch order")
            }
        }
    }

    suspend fun markAsServed(orderId: Long): Resource<Unit> {
        return try {
            // Try to update via API first
            try {
                val response = apiService.markOrderAsServed(orderId)
                if (response.isSuccessful) {
                    val servedAt = System.currentTimeMillis()
                    orderDao.updateOrderStatus(orderId, "served")
                    orderDao.updateServedAt(orderId, servedAt)
                    return Resource.Success(Unit)
                }
            } catch (apiError: Exception) {
                // API not available, continue with local update
            }

            // Update locally
            val servedAt = System.currentTimeMillis()
            orderDao.updateOrderStatus(orderId, "served")
            orderDao.updateServedAt(orderId, servedAt)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as served")
        }
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
                // Update locally for offline support
                orderDao.updateOrderStatus(orderId, status)
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            // Update locally for offline support
            orderDao.updateOrderStatus(orderId, status)
            Resource.Success(Unit)
        }
    }

    suspend fun cancelOrder(orderId: Long, reason: String): Resource<Unit> {
        return try {
            val response = apiService.cancelOrder(orderId, CancelOrderRequest(reason))

            if (response.isSuccessful) {
                orderDao.updateOrderStatus(orderId, "cancelled")
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to cancel order")
            }
        } catch (e: Exception) {
            // Update locally for offline support
            orderDao.updateOrderStatus(orderId, "cancelled")
            Resource.Success(Unit)
        }
    }

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> {
        return orderItemDao.getOrderItems(orderId)
    }

    /**
     * Fetch order items directly from API for display.
     */
    suspend fun fetchOrderItemsFromApi(orderId: Long): Resource<List<OrderItemEntity>> {
        return try {
            val response = apiService.getOrder(orderId)
            if (response.isSuccessful && response.body() != null) {
                val orderDetail = response.body()!!
                val items = orderDetail.items.mapIndexed { index, item ->
                    OrderItemEntity(
                        id = index.toLong() + 1,
                        orderId = orderId,
                        menuItemId = 0L, // Not provided in summary
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        subtotal = item.subtotal,
                        status = item.status ?: "pending",
                        notes = item.specialInstructions,
                        itemName = item.name
                    )
                }
                Resource.Success(items)
            } else {
                Resource.Error("Failed to fetch order items")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error fetching order items")
        }
    }

    suspend fun getUnsyncedOrders(): List<OrderEntity> {
        return orderDao.getUnsyncedOrders()
    }

    suspend fun markOrderAsSynced(orderId: Long) {
        orderDao.markOrderAsSynced(orderId)
    }
}
