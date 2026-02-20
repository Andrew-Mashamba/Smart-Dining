package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
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

    @ColumnInfo(name = "order_id")
    @SerializedName("order_id")
    val orderId: Long,

    @ColumnInfo(name = "menu_item_id")
    @SerializedName("menu_item_id")
    val menuItemId: Long,

    @SerializedName("quantity")
    val quantity: Int,

    @ColumnInfo(name = "unit_price")
    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("status")
    val status: String, // pending/received/preparing/done

    @SerializedName("notes")
    val notes: String? = null,

    @ColumnInfo(name = "prepared_by")
    @SerializedName("prepared_by")
    val preparedBy: Long? = null,

    @ColumnInfo(name = "prepared_at")
    @SerializedName("prepared_at")
    val preparedAt: Date? = null,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null,

    // Display field for item name (from API, not a FK lookup)
    @ColumnInfo(name = "item_name")
    val itemName: String? = null
)
