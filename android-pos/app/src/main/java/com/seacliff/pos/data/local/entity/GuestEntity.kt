package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "guests")
data class GuestEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("name")
    val name: String? = null,

    @ColumnInfo(name = "first_visit_at")
    @SerializedName("first_visit_at")
    val firstVisitAt: Date,

    @ColumnInfo(name = "last_visit_at")
    @SerializedName("last_visit_at")
    val lastVisitAt: Date,

    @ColumnInfo(name = "loyalty_points")
    @SerializedName("loyalty_points")
    val loyaltyPoints: Int = 0,

    @SerializedName("preferences")
    val preferences: String? = null, // JSON string

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
