package com.seacliff.pos.data.remote.api

import com.seacliff.pos.data.local.entity.*
import com.seacliff.pos.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("auth/me")
    suspend fun getCurrentStaff(): Response<ApiResponse<StaffEntity>>

    // Menu
    @GET("menu")
    suspend fun getMenu(): Response<List<MenuItemEntity>>

    @GET("menu/{id}")
    suspend fun getMenuItem(@Path("id") id: Long): Response<ApiResponse<MenuItemEntity>>

    @GET("menu/categories")
    suspend fun getMenuCategories(): Response<List<String>>

    @PUT("menu/{id}/availability")
    suspend fun updateMenuItemAvailability(
        @Path("id") id: Long,
        @Body availability: Map<String, Boolean>
    ): Response<ApiResponse<MenuItemEntity>>

    // Tables
    @GET("tables")
    suspend fun getTables(): Response<List<TableEntity>>

    @GET("tables/{id}")
    suspend fun getTable(@Path("id") id: Long): Response<ApiResponse<TableEntity>>

    @PUT("tables/{id}/status")
    suspend fun updateTableStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<TableEntity>>

    // Guests
    @GET("guests/phone/{phone}")
    suspend fun getGuestByPhone(@Path("phone") phone: String): Response<ApiResponse<GuestEntity>>

    @POST("guests")
    suspend fun createGuest(@Body guest: GuestEntity): Response<ApiResponse<GuestEntity>>

    // Orders
    @GET("orders")
    suspend fun getOrders(): Response<List<OrderEntity>>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: Long): Response<ApiResponse<OrderEntity>>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderEntity>>

    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<OrderEntity>>

    @POST("orders/{id}/serve")
    suspend fun markOrderAsServed(@Path("id") id: Long): Response<ApiResponse<OrderEntity>>

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Long): Response<ApiResponse<OrderEntity>>

    // Order Items
    @GET("order-items/pending")
    suspend fun getPendingOrderItems(): Response<List<OrderItemEntity>>

    @POST("order-items/{id}/received")
    suspend fun markOrderItemReceived(@Path("id") id: Long): Response<ApiResponse<OrderItemEntity>>

    @POST("order-items/{id}/done")
    suspend fun markOrderItemDone(@Path("id") id: Long): Response<ApiResponse<OrderItemEntity>>

    // Payments
    @POST("payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<ApiResponse<PaymentEntity>>

    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: Long): Response<ApiResponse<PaymentEntity>>

    @POST("payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: Long): Response<ApiResponse<PaymentEntity>>

    @GET("orders/{orderId}/bill")
    suspend fun getOrderBill(@Path("orderId") orderId: Long): Response<ApiResponse<Map<String, Any>>>

    // Tips
    @POST("tips")
    suspend fun createTip(@Body tip: Map<String, Any>): Response<ApiResponse<Any>>

    @GET("orders/{orderId}/tip-suggestions")
    suspend fun getTipSuggestions(@Path("orderId") orderId: Long): Response<ApiResponse<Map<String, Any>>>
}
