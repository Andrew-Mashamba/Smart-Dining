package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    // Category can be either a string (from local) or category_id from API
    @ColumnInfo(name = "category")
    @SerializedName("category")
    val category: String? = null, // appetizer/main/dessert/drink

    @ColumnInfo(name = "category_id")
    @SerializedName("category_id")
    val categoryId: Long? = null,

    @SerializedName("price")
    val price: Double,

    @ColumnInfo(name = "prep_area")
    @SerializedName("prep_area")
    val prepArea: String, // kitchen/bar

    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    val imageUrl: String? = null,

    // API returns "available", local DB uses "is_available"
    @ColumnInfo(name = "is_available")
    @SerializedName("available")
    val isAvailable: Boolean = true,

    // API returns "prep_time_minutes", local DB uses "preparation_time"
    @ColumnInfo(name = "preparation_time")
    @SerializedName("prep_time_minutes")
    val preparationTime: Int = 0, // minutes

    @ColumnInfo(name = "is_popular")
    @SerializedName("is_popular")
    val isPopular: Boolean = false,

    @ColumnInfo(name = "dietary_info")
    @SerializedName("dietary_info")
    val dietaryInfo: String? = null,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
