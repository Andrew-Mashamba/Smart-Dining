package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["status"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("method")
    val method: String, // cash/card/mpesa/pesapal/bank_transfer

    @SerializedName("status")
    val status: String, // pending/processing/completed/failed/cancelled/refunded

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("gateway_response")
    val gatewayResponse: String? = null, // JSON string

    @SerializedName("paid_at")
    val paidAt: Date? = null,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
