package com.seacliff.pos.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create tips table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    order_id INTEGER NOT NULL,
                    payment_id INTEGER,
                    waiter_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    method TEXT NOT NULL,
                    notes TEXT,
                    is_synced INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE,
                    FOREIGN KEY(payment_id) REFERENCES payments(id) ON DELETE SET NULL,
                    FOREIGN KEY(waiter_id) REFERENCES staff(id) ON DELETE CASCADE
                )
            """)

            // Create indices for tips table
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tips_order_id ON tips(order_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tips_payment_id ON tips(payment_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tips_waiter_id ON tips(waiter_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tips_created_at ON tips(created_at)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add served_at column to orders table
            database.execSQL("ALTER TABLE orders ADD COLUMN served_at INTEGER")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE orders ADD COLUMN table_name TEXT")
            database.execSQL("ALTER TABLE orders ADD COLUMN waiter_name TEXT")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recreate orders table without guest_id foreign key constraint
            // SQLite doesn't support DROP CONSTRAINT, so we recreate the table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS orders_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    guest_id INTEGER NOT NULL,
                    table_id INTEGER NOT NULL,
                    waiter_id INTEGER NOT NULL,
                    session_id INTEGER,
                    status TEXT NOT NULL,
                    order_source TEXT NOT NULL,
                    subtotal REAL NOT NULL,
                    tax REAL NOT NULL,
                    service_charge REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    notes TEXT,
                    created_at INTEGER,
                    updated_at INTEGER,
                    served_at INTEGER,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    local_id TEXT,
                    table_name TEXT,
                    waiter_name TEXT,
                    FOREIGN KEY(table_id) REFERENCES tables(id) ON DELETE CASCADE,
                    FOREIGN KEY(waiter_id) REFERENCES staff(id) ON DELETE CASCADE
                )
            """)

            // Copy data from old table
            database.execSQL("""
                INSERT INTO orders_new
                SELECT id, guest_id, table_id, waiter_id, session_id, status, order_source,
                       subtotal, tax, service_charge, total_amount, notes, created_at, updated_at,
                       served_at, isSynced, local_id, table_name, waiter_name
                FROM orders
            """)

            // Drop old table
            database.execSQL("DROP TABLE orders")

            // Rename new table
            database.execSQL("ALTER TABLE orders_new RENAME TO orders")

            // Recreate indices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_orders_guest_id ON orders(guest_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_orders_table_id ON orders(table_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_orders_waiter_id ON orders(waiter_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_orders_status ON orders(status)")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add item_name column to order_items table for display purposes
            database.execSQL("ALTER TABLE order_items ADD COLUMN item_name TEXT")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Update table names to new nomenclature: T0021 (indoor), BT03 (bar), OT008 (outside)
            database.execSQL("UPDATE tables SET name = 'T0001' WHERE id = 1")
            database.execSQL("UPDATE tables SET name = 'T0002' WHERE id = 2")
            database.execSQL("UPDATE tables SET name = 'T0003' WHERE id = 3")
            database.execSQL("UPDATE tables SET name = 'T0004' WHERE id = 4")
            database.execSQL("UPDATE tables SET name = 'T0005' WHERE id = 5")
            database.execSQL("UPDATE tables SET name = 'OT001' WHERE id = 6")
            database.execSQL("UPDATE tables SET name = 'OT002' WHERE id = 7")
            database.execSQL("UPDATE tables SET name = 'OT008' WHERE id = 8")
            database.execSQL("UPDATE tables SET name = 'BT01' WHERE id = 9")
            database.execSQL("UPDATE tables SET name = 'BT02' WHERE id = 10")
            database.execSQL("UPDATE tables SET name = 'BT03' WHERE id = 11")
            database.execSQL("UPDATE tables SET name = 'T0012' WHERE id = 12")
        }
    }

    // No seed data: all data is loaded from the backend and stored in the database via SyncWorker.
    private val seedDatabaseCallback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Tables, menu, staff, guests come from API sync only.
        }
    }

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .addCallback(seedDatabaseCallback)
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

    @Provides
    fun provideTipDao(database: AppDatabase): TipDao {
        return database.tipDao()
    }
}
