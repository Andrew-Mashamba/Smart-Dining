package com.seacliff.pos.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = true,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

/** For parsing API error response body (e.g. 4xx/5xx). */
data class ApiErrorBody(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String? = null
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_name")
    val deviceName: String = "Android POS"
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: StaffDto,

    @SerializedName("message")
    val message: String? = null
)

data class StaffDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("has_pin")
    val hasPin: Boolean = false
)

// PIN Login request
data class PinLoginRequest(
    @SerializedName("staff_id")
    val staffId: Long,

    @SerializedName("pin")
    val pin: String,

    @SerializedName("device_name")
    val deviceName: String = "Android POS"
)

// Staff list for PIN selection
data class StaffListResponse(
    @SerializedName("staff")
    val staff: List<StaffSummaryDto>
)

data class StaffSummaryDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("role")
    val role: String
)

// Set PIN request
data class SetPinRequest(
    @SerializedName("pin")
    val pin: String,

    @SerializedName("current_password")
    val currentPassword: String
)

data class CreateOrderRequest(
    @SerializedName("guest_id")
    val guestId: Long,

    @SerializedName("table_id")
    val tableId: Long,

    @SerializedName("order_source")
    val orderSource: String = "pos",

    @SerializedName("items")
    val items: List<OrderItemRequest>,

    @SerializedName("notes")
    val notes: String? = null
)

data class OrderItemRequest(
    @SerializedName("menu_item_id")
    val menuItemId: Long,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("special_instructions")
    val specialInstructions: String? = null
)

data class UpdateStatusRequest(
    @SerializedName("status")
    val status: String
)

data class CreatePaymentRequest(
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("payment_method")
    val paymentMethod: String,

    // For cash payments
    @SerializedName("tendered")
    val tendered: Double? = null,

    // For mobile money payments
    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("provider")
    val provider: String? = null, // mpesa, tigopesa, airtel

    // For card payments
    @SerializedName("card_last_four")
    val cardLastFour: String? = null,

    @SerializedName("card_type")
    val cardType: String? = null // visa, mastercard, amex
)

// Response wrapper for menu list
data class MenuListResponse(
    @SerializedName("items")
    val items: List<MenuItemDto>,

    @SerializedName("total")
    val total: Int
)

// Menu item DTO matching Laravel MenuItemResource
data class MenuItemDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("price")
    val price: Double,

    @SerializedName("category")
    val category: CategoryDto? = null,

    @SerializedName("prep_area")
    val prepArea: String,

    @SerializedName("prep_time_minutes")
    val prepTimeMinutes: Int? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("available")
    val available: Boolean = true,

    @SerializedName("is_popular")
    val isPopular: Boolean = false,

    @SerializedName("dietary_info")
    val dietaryInfo: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CategoryDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String
)

// Response wrapper for tables list
data class TableListResponse(
    @SerializedName("tables")
    val tables: List<TableDto>,

    @SerializedName("total")
    val total: Int
)

// Table DTO matching Laravel TableResource
data class TableDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("qr_code")
    val qrCode: String? = null,

    @SerializedName("current_orders")
    val currentOrders: List<OrderSummaryDto>? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class OrderSummaryDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("order_number")
    val orderNumber: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("total_amount")
    val totalAmount: Double
)

// Tip request DTO
data class CreateTipRequest(
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("tip_method")
    val tipMethod: String, // cash, card, mobile_money

    @SerializedName("payment_id")
    val paymentId: Long? = null
)

// Tip suggestions response
data class TipSuggestionsResponse(
    @SerializedName("10_percent")
    val tenPercent: Double,

    @SerializedName("15_percent")
    val fifteenPercent: Double,

    @SerializedName("20_percent")
    val twentyPercent: Double,

    @SerializedName("order_total")
    val orderTotal: Double
)

// Menu availability update request
data class UpdateAvailabilityRequest(
    @SerializedName("available")
    val available: Boolean
)

// Paginated response for orders
data class PaginatedOrdersResponse(
    @SerializedName("data")
    val data: List<OrderDto>,

    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("last_page")
    val lastPage: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("total")
    val total: Int
)

// Order DTO matching Laravel response
data class OrderDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("order_number")
    val orderNumber: String? = null,

    @SerializedName("guest_id")
    val guestId: Long,

    @SerializedName("table_id")
    val tableId: Long,

    @SerializedName("waiter_id")
    val waiterId: Long,

    @SerializedName("session_id")
    val sessionId: Long? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("order_source")
    val orderSource: String,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("tax")
    val tax: Double,

    @SerializedName("total")
    val total: Double,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("special_instructions")
    val specialInstructions: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("served_at")
    val servedAt: String? = null,

    // Related entities when loaded
    @SerializedName("order_items")
    val orderItems: List<OrderItemDto>? = null,

    @SerializedName("guest")
    val guest: GuestDto? = null,

    @SerializedName("table")
    val table: TableDto? = null,

    @SerializedName("waiter")
    val waiter: StaffDto? = null,

    @SerializedName("payments")
    val payments: List<PaymentDto>? = null
)

data class OrderItemDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("menu_item_id")
    val menuItemId: Long,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("prep_status")
    val prepStatus: String,

    @SerializedName("special_instructions")
    val specialInstructions: String? = null,

    @SerializedName("menu_item")
    val menuItem: MenuItemDto? = null
)

data class GuestDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("first_visit_at")
    val firstVisitAt: String? = null,

    @SerializedName("last_visit_at")
    val lastVisitAt: String? = null,

    @SerializedName("loyalty_points")
    val loyaltyPoints: Int = 0,

    @SerializedName("preferences")
    val preferences: String? = null  // API returns JSON as string
)

data class PaymentDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("gateway_response")
    val gatewayResponse: Map<String, Any>? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

// Order create/update response wrapper
data class OrderActionResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("order")
    val order: OrderSummaryDetailDto
)

data class OrderSummaryDetailDto(
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("guest")
    val guest: GuestSummaryDto,

    @SerializedName("table")
    val table: String,

    @SerializedName("waiter")
    val waiter: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("items")
    val items: List<OrderItemSummaryDto>,

    @SerializedName("totals")
    val totals: OrderTotalsDto,

    @SerializedName("created_at")
    val createdAt: String? = null
)

data class GuestSummaryDto(
    @SerializedName("name")
    val name: String?,

    @SerializedName("phone")
    val phone: String
)

data class OrderItemSummaryDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("status")
    val status: String?,

    @SerializedName("special_instructions")
    val specialInstructions: String? = null
)

data class OrderTotalsDto(
    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("tax")
    val tax: Double,

    @SerializedName("service_charge")
    val serviceCharge: Double? = 0.0,

    @SerializedName("total_amount")
    val totalAmount: Double? = null
)

// Bill response
data class BillResponse(
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("order_number")
    val orderNumber: String?,

    @SerializedName("items")
    val items: List<BillItemDto>,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("tax")
    val tax: Double,

    @SerializedName("total")
    val total: Double,

    @SerializedName("amount_paid")
    val amountPaid: Double,

    @SerializedName("balance_due")
    val balanceDue: Double,

    @SerializedName("payments")
    val payments: List<PaymentDto>? = null
)

data class BillItemDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("subtotal")
    val subtotal: Double
)

// Auth me response
data class MeResponse(
    @SerializedName("user")
    val user: StaffDto
)

// Menu categories response
data class MenuCategoriesResponse(
    @SerializedName("categories")
    val categories: List<MenuCategoryDto>
)

data class MenuCategoryDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("display_order")
    val displayOrder: Int = 0,

    @SerializedName("items")
    val items: List<MenuItemDto>? = null
)

// Menu search response
data class MenuSearchResponse(
    @SerializedName("results")
    val results: List<MenuItemDto>,

    @SerializedName("count")
    val count: Int
)

// Menu availability update response
data class MenuAvailabilityResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("item")
    val item: MenuItemDto
)

// Table status update response
data class TableStatusResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("table")
    val table: TableDto
)

// Guest creation request
data class CreateGuestRequest(
    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("preferences")
    val preferences: Map<String, Any>? = null
)

// Guest creation response
data class GuestCreateResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("guest")
    val guest: GuestDto
)

// Add order items request
data class AddOrderItemsRequest(
    @SerializedName("items")
    val items: List<OrderItemRequest>
)

// Cancel order request
data class CancelOrderRequest(
    @SerializedName("reason")
    val reason: String
)

// Order item response
data class OrderItemResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("order_item")
    val orderItem: OrderItemDto? = null
)

// Payment response
data class PaymentResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("payment")
    val payment: PaymentDto
)

// Payments list response
data class PaymentsListResponse(
    @SerializedName("payments")
    val payments: List<PaymentDto>,

    @SerializedName("total")
    val total: Int
)

// Tip response
data class TipResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("tip")
    val tip: TipDto
)

data class TipDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("waiter_id")
    val waiterId: Long,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("tip_method")
    val tipMethod: String,

    @SerializedName("payment_id")
    val paymentId: Long? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

// Stripe payment intent request
data class StripeIntentRequest(
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("amount")
    val amount: Double
)

// Stripe payment intent response
data class StripeIntentResponse(
    @SerializedName("client_secret")
    val clientSecret: String,

    @SerializedName("payment_intent_id")
    val paymentIntentId: String
)

// Stripe confirm request
data class StripeConfirmRequest(
    @SerializedName("payment_intent_id")
    val paymentIntentId: String
)
