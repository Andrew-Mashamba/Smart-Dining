package com.seacliff.pos.data.local.entity

import androidx.room.Entity
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

    @SerializedName("category")
    val category: String, // appetizer/main/dessert/drink

    @SerializedName("price")
    val price: Double,

    @SerializedName("prep_area")
    val prepArea: String, // kitchen/bar

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("is_available")
    val isAvailable: Boolean = true,

    @SerializedName("preparation_time")
    val preparationTime: Int, // minutes

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
