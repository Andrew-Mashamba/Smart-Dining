package com.seacliff.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.seacliff.pos.data.local.converter.DateConverter
import com.seacliff.pos.data.local.dao.*
import com.seacliff.pos.data.local.entity.*

@Database(
    entities = [
        GuestEntity::class,
        TableEntity::class,
        StaffEntity::class,
        MenuItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guestDao(): GuestDao
    abstract fun tableDao(): TableDao
    abstract fun staffDao(): StaffDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        const val DATABASE_NAME = "seacliff_pos_db"
    }
}
