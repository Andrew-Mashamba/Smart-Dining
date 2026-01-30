package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :menuItemId")
    suspend fun getMenuItemById(menuItemId: Long): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE is_available = 1 ORDER BY category, name")
    fun getAvailableMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE category = :category AND is_available = 1 ORDER BY name")
    fun getMenuItemsByCategory(category: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE prep_area = :prepArea AND is_available = 1 ORDER BY name")
    fun getMenuItemsByPrepArea(prepArea: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(menuItem: MenuItemEntity)

    @Query("UPDATE menu_items SET is_available = :isAvailable WHERE id = :menuItemId")
    suspend fun updateMenuItemAvailability(menuItemId: Long, isAvailable: Boolean)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItemEntity)

    @Query("DELETE FROM menu_items")
    suspend fun deleteAllMenuItems()
}
