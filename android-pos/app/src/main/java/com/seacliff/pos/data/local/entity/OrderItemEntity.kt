package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["menu_item_id"]),
        Index(value = ["status"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

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

    @SerializedName("status")
    val status: String, // pending/received/preparing/done

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("prepared_by")
    val preparedBy: Long? = null,

    @SerializedName("prepared_at")
    val preparedAt: Date? = null,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
