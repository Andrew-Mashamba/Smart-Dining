package com.seacliff.pos.di

import android.content.Context
import androidx.room.Room
import com.seacliff.pos.data.local.dao.*
import com.seacliff.pos.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGuestDao(database: AppDatabase): GuestDao {
        return database.guestDao()
    }

    @Provides
    fun provideTableDao(database: AppDatabase): TableDao {
        return database.tableDao()
    }

    @Provides
    fun provideStaffDao(database: AppDatabase): StaffDao {
        return database.staffDao()
    }

    @Provides
    fun provideMenuItemDao(database: AppDatabase): MenuItemDao {
        return database.menuItemDao()
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideOrderItemDao(database: AppDatabase): OrderItemDao {
        return database.orderItemDao()
    }

    @Provides
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }
}
