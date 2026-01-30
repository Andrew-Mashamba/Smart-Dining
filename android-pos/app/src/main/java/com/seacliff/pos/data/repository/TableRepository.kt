package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.TableDao
import com.seacliff.pos.data.local.entity.TableEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.UpdateStatusRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepository @Inject constructor(
    private val apiService: ApiService,
    private val tableDao: TableDao
) {

    fun getTables(forceRefresh: Boolean = false): Flow<Resource<List<TableEntity>>> = flow {
        try {
            emit(Resource.Loading())

            // Load from local database first
            if (!forceRefresh) {
                val localTables = tableDao.getAllTables()
                localTables.collect { tables ->
                    if (tables.isNotEmpty()) {
                        emit(Resource.Success(tables))
                    }
                }
            }

            // Fetch from API
            val response = apiService.getTables()

            if (response.isSuccessful && response.body() != null) {
                val tables = response.body()!!

                // Save to local database
                tableDao.insertTables(tables)

                emit(Resource.Success(tables))
            } else {
                // Return cached data on API failure
                val localTables = tableDao.getAllTables()
                localTables.collect { tables ->
                    emit(Resource.Success(tables))
                }
            }
        } catch (e: Exception) {
            // On error, try to return cached data
            try {
                val localTables = tableDao.getAllTables()
                localTables.collect { tables ->
                    if (tables.isNotEmpty()) {
                        emit(Resource.Success(tables))
                    } else {
                        emit(Resource.Error("Failed to load tables: ${e.localizedMessage}"))
                    }
                }
            } catch (cacheException: Exception) {
                emit(Resource.Error("Failed to load tables: ${e.localizedMessage}"))
            }
        }
    }

    fun getTablesByStatus(status: String): Flow<List<TableEntity>> {
        return tableDao.getTablesByStatus(status)
    }

    suspend fun updateTableStatus(tableId: Long, status: String): Resource<Unit> {
        return try {
            val response = apiService.updateTableStatus(
                tableId,
                UpdateStatusRequest(status)
            )

            if (response.isSuccessful) {
                // Update local database
                tableDao.updateTableStatus(tableId, status)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update table status")
            }
        } catch (e: Exception) {
            // Update locally anyway for offline support
            tableDao.updateTableStatus(tableId, status)
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }
}
