package com.seacliff.pos.data.local.entity

import androidx.room.ColumnInfo
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

    @ColumnInfo(name = "guest_id")
    @SerializedName("guest_id")
    val guestId: Long,

    @ColumnInfo(name = "table_id")
    @SerializedName("table_id")
    val tableId: Long,

    @ColumnInfo(name = "waiter_id")
    @SerializedName("waiter_id")
    val waiterId: Long,
    @ColumnInfo(name = "session_id")

    @SerializedName("session_id")
    val sessionId: Long? = null,

    @SerializedName("status")
    val status: String, // pending/confirmed/preparing/ready/served/completed/cancelled
    @ColumnInfo(name = "order_source")

    @SerializedName("order_source")
    val orderSource: String, // whatsapp/pos/web

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("tax")
    val tax: Double,
    @ColumnInfo(name = "service_charge")

    @SerializedName("service_charge")
    val serviceCharge: Double,
    @ColumnInfo(name = "total_amount")

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("notes")
    val notes: String? = null,
    @ColumnInfo(name = "created_at")

    @SerializedName("created_at")
    val createdAt: Date? = null,
    @ColumnInfo(name = "updated_at")

    @SerializedName("updated_at")
    val updatedAt: Date? = null,

    @ColumnInfo(name = "served_at")
    @SerializedName("served_at")
    val servedAt: Date? = null, // Timestamp when order was served to customer

    // Local-only fields for offline support
    val isSynced: Boolean = false,
    @ColumnInfo(name = "local_id")
    val localId: String? = null, // UUID for offline orders

    // Display names (from API or resolved); used in order list
    @ColumnInfo(name = "table_name")
    val tableName: String? = null,
    @ColumnInfo(name = "waiter_name")
    val waiterName: String? = null
)
