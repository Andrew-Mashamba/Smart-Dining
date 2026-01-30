package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.MenuItemDao
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val apiService: ApiService,
    private val menuItemDao: MenuItemDao
) {

    fun getMenuItems(forceRefresh: Boolean = false): Flow<Resource<List<MenuItemEntity>>> = flow {
        try {
            emit(Resource.Loading())

            // Load from local database first
            if (!forceRefresh) {
                val localItems = menuItemDao.getAvailableMenuItems()
                localItems.collect { items ->
                    if (items.isNotEmpty()) {
                        emit(Resource.Success(items))
                    }
                }
            }

            // Fetch from API
            val response = apiService.getMenu()

            if (response.isSuccessful && response.body() != null) {
                val menuItems = response.body()!!

                // Save to local database
                menuItemDao.insertMenuItems(menuItems)

                emit(Resource.Success(menuItems))
            } else {
                // If API fails, return cached data
                val localItems = menuItemDao.getAllMenuItems()
                localItems.collect { items ->
                    emit(Resource.Success(items))
                }
            }
        } catch (e: Exception) {
            // On error, try to return cached data
            try {
                val localItems = menuItemDao.getAllMenuItems()
                localItems.collect { items ->
                    if (items.isNotEmpty()) {
                        emit(Resource.Success(items))
                    } else {
                        emit(Resource.Error("Failed to load menu: ${e.localizedMessage}"))
                    }
                }
            } catch (cacheException: Exception) {
                emit(Resource.Error("Failed to load menu: ${e.localizedMessage}"))
            }
        }
    }

    fun getMenuItemsByCategory(category: String): Flow<List<MenuItemEntity>> {
        return menuItemDao.getMenuItemsByCategory(category)
    }

    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>> {
        return menuItemDao.searchMenuItems(query)
    }

    suspend fun updateMenuItemAvailability(itemId: Long, isAvailable: Boolean): Resource<Unit> {
        return try {
            val response = apiService.updateMenuItemAvailability(
                itemId,
                mapOf("is_available" to isAvailable)
            )

            if (response.isSuccessful) {
                // Update local database
                menuItemDao.updateMenuItemAvailability(itemId, isAvailable)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update availability")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }
}
