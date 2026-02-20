package com.seacliff.pos.data.remote.api

import com.seacliff.pos.data.local.entity.*
import com.seacliff.pos.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== Authentication ====================
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/login-pin")
    suspend fun loginWithPin(@Body request: PinLoginRequest): Response<LoginResponse>

    @GET("auth/staff-list")
    suspend fun getStaffForPinLogin(): Response<StaffListResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("auth/me")
    suspend fun getCurrentStaff(): Response<MeResponse>

    @POST("auth/set-pin")
    suspend fun setPin(@Body request: SetPinRequest): Response<ApiResponse<Unit>>

    // ==================== Device Tokens (FCM) ====================
    @POST("device-tokens")
    suspend fun registerDeviceToken(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @HTTP(method = "DELETE", path = "device-tokens", hasBody = true)
    suspend fun removeDeviceToken(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // ==================== Menu ====================
    @GET("menu")
    suspend fun getMenu(): Response<MenuListResponse>

    @GET("menu/items")
    suspend fun getMenuItems(@Query("category_id") categoryId: Long? = null): Response<MenuListResponse>

    @GET("menu/{id}")
    suspend fun getMenuItem(@Path("id") id: Long): Response<MenuItemDto>

    @GET("menu/categories")
    suspend fun getMenuCategories(): Response<MenuCategoriesResponse>

    @GET("menu/popular")
    suspend fun getPopularMenuItems(@Query("limit") limit: Int = 10): Response<MenuListResponse>

    @GET("menu/search")
    suspend fun searchMenu(@Query("query") query: String): Response<MenuSearchResponse>

    @PUT("menu/{id}/availability")
    suspend fun updateMenuItemAvailability(
        @Path("id") id: Long,
        @Body request: UpdateAvailabilityRequest
    ): Response<MenuAvailabilityResponse>

    // ==================== Tables ====================
    @GET("tables")
    suspend fun getTables(
        @Query("status") status: String? = null,
        @Query("location") location: String? = null
    ): Response<TableListResponse>

    @GET("tables/{id}")
    suspend fun getTable(@Path("id") id: Long): Response<TableDto>

    @PATCH("tables/{id}/status")
    suspend fun updateTableStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<TableStatusResponse>

    // ==================== Guests ====================
    @GET("guests/phone/{phone}")
    suspend fun getGuestByPhone(@Path("phone") phone: String): Response<GuestDto>

    @POST("guests")
    suspend fun createGuest(@Body request: CreateGuestRequest): Response<GuestCreateResponse>

    // ==================== Orders ====================
    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("table_id") tableId: Long? = null,
        @Query("waiter_id") waiterId: Long? = null,
        @Query("date") date: String? = null
    ): Response<PaginatedOrdersResponse>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: Long): Response<OrderSummaryDetailDto>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderActionResponse>

    @POST("orders/{id}/items")
    suspend fun addOrderItems(
        @Path("id") id: Long,
        @Body request: AddOrderItemsRequest
    ): Response<OrderActionResponse>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<OrderActionResponse>

    @POST("orders/{id}/serve")
    suspend fun markOrderAsServed(@Path("id") id: Long): Response<OrderActionResponse>

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: Long,
        @Body request: CancelOrderRequest
    ): Response<ApiResponse<Unit>>

    @GET("orders/{id}/receipt")
    suspend fun getOrderReceipt(@Path("id") id: Long): Response<okhttp3.ResponseBody>

    // ==================== Order Items (Kitchen/Bar) ====================
    @GET("order-items/pending")
    suspend fun getPendingOrderItems(): Response<List<OrderItemDto>>

    @POST("order-items/{id}/received")
    suspend fun markOrderItemReceived(@Path("id") id: Long): Response<OrderItemResponse>

    @POST("order-items/{id}/done")
    suspend fun markOrderItemDone(@Path("id") id: Long): Response<OrderItemResponse>

    // ==================== Payments ====================
    @POST("payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<PaymentResponse>

    @GET("payments")
    suspend fun getPayments(@Query("order_id") orderId: Long? = null): Response<PaymentsListResponse>

    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: Long): Response<PaymentDto>

    @POST("payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: Long): Response<PaymentResponse>

    @GET("orders/{orderId}/bill")
    suspend fun getOrderBill(@Path("orderId") orderId: Long): Response<BillResponse>

    // Stripe payments
    @POST("payments/stripe/create-intent")
    suspend fun createStripeIntent(@Body request: StripeIntentRequest): Response<StripeIntentResponse>

    @POST("payments/stripe/confirm")
    suspend fun confirmStripePayment(@Body request: StripeConfirmRequest): Response<PaymentResponse>

    // ==================== Tips ====================
    @POST("tips")
    suspend fun createTip(@Body request: CreateTipRequest): Response<TipResponse>

    @GET("orders/{orderId}/tip-suggestions")
    suspend fun getTipSuggestions(@Path("orderId") orderId: Long): Response<TipSuggestionsResponse>
}
