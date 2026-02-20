package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String, // waiter/chef/bartender/manager/admin

    @ColumnInfo(name = "phone_number")
    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("status")
    val status: String, // active/inactive

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
