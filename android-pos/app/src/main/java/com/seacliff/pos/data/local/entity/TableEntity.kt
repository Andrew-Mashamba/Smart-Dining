package com.seacliff.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("location")
    val location: String, // indoor/outdoor/bar

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("status")
    val status: String, // available/occupied/reserved

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Date? = null
)
