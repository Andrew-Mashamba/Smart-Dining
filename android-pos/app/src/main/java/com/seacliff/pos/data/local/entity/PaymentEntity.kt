package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
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

    @ColumnInfo(name = "order_id")
    @SerializedName("order_id")
    val orderId: Long,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("method")
    val method: String, // cash/card/mpesa/pesapal/bank_transfer

    @SerializedName("status")
    val status: String, // pending/processing/completed/failed/cancelled/refunded

    @ColumnInfo(name = "transaction_id")
    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @ColumnInfo(name = "gateway_response")
    @SerializedName("gateway_response")
    val gatewayResponse: String? = null, // JSON string

    @ColumnInfo(name = "paid_at")
    @SerializedName("paid_at")
    val paidAt: Date? = null,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
