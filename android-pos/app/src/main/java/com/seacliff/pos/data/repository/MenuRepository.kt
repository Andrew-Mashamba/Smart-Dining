package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.MenuItemDao
import com.seacliff.pos.data.local.entity.MenuItemEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.MenuItemDto
import com.seacliff.pos.data.remote.dto.UpdateAvailabilityRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val apiService: ApiService,
    private val menuItemDao: MenuItemDao
) {

    /**
     * Convert MenuItemDto from API to MenuItemEntity for local storage
     */
    private fun MenuItemDto.toEntity(): MenuItemEntity {
        return MenuItemEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category?.name,
            categoryId = this.category?.id,
            price = this.price,
            prepArea = this.prepArea,
            imageUrl = this.imageUrl,
            isAvailable = this.available,
            preparationTime = this.prepTimeMinutes ?: 0,
            isPopular = this.isPopular,
            dietaryInfo = this.dietaryInfo,
            createdAt = null, // Will be parsed if needed
            updatedAt = null
        )
    }

    fun getMenuItems(forceRefresh: Boolean = false): Flow<Resource<List<MenuItemEntity>>> = flow {
        try {
            emit(Resource.Loading())

            // Try to fetch from API and store in DB (sync is also done by SyncWorker)
            try {
                val response = apiService.getMenuItems(categoryId = null)
                if (response.isSuccessful && response.body() != null) {
                    val menuResponse = response.body()!!
                    val menuItems = menuResponse.items.map { it.toEntity() }
                    menuItemDao.insertMenuItems(menuItems)
                }
            } catch (_: Exception) {
                // API not available; use whatever is in DB
            }

            val localItems = menuItemDao.getAvailableMenuItems().first()
            emit(Resource.Success(localItems))
        } catch (e: Exception) {
            try {
                val localItems = menuItemDao.getAllMenuItems().first()
                emit(Resource.Success(localItems))
            } catch (_: Exception) {
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
                UpdateAvailabilityRequest(isAvailable)
            )

            if (response.isSuccessful) {
                // Update local database
                menuItemDao.updateMenuItemAvailability(itemId, isAvailable)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update availability")
            }
        } catch (e: Exception) {
            // Update locally for offline support
            menuItemDao.updateMenuItemAvailability(itemId, isAvailable)
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }
}
