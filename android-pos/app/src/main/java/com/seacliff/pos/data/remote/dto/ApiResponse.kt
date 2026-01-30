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

    @SerializedName("staff")
    val staff: StaffDto
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
    val phoneNumber: String? = null
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

    @SerializedName("notes")
    val notes: String? = null
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

    @SerializedName("method")
    val method: String
)
