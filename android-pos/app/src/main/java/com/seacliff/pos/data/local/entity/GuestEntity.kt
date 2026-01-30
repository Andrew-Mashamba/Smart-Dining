package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "guests")
data class GuestEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("first_visit_at")
    val firstVisitAt: Date,

    @SerializedName("last_visit_at")
    val lastVisitAt: Date,

    @SerializedName("loyalty_points")
    val loyaltyPoints: Int = 0,

    @SerializedName("preferences")
    val preferences: String? = null, // JSON string

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
