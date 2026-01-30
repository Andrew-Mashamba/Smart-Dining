package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = GuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["guest_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["table_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["waiter_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["guest_id"]),
        Index(value = ["table_id"]),
        Index(value = ["waiter_id"]),
        Index(value = ["status"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("guest_id")
    val guestId: Long,

    @SerializedName("table_id")
    val tableId: Long,

    @SerializedName("waiter_id")
    val waiterId: Long,

    @SerializedName("session_id")
    val sessionId: Long? = null,

    @SerializedName("status")
    val status: String, // pending/confirmed/preparing/ready/served/completed/cancelled

    @SerializedName("order_source")
    val orderSource: String, // whatsapp/pos/web

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("tax")
    val tax: Double,

    @SerializedName("service_charge")
    val serviceCharge: Double,

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null,

    // Local-only fields for offline support
    val isSynced: Boolean = false,
    val localId: String? = null // UUID for offline orders
)
