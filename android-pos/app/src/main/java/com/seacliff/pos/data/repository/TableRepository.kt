package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.TableDao
import com.seacliff.pos.data.local.entity.TableEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.TableDto
import com.seacliff.pos.data.remote.dto.UpdateStatusRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepository @Inject constructor(
    private val apiService: ApiService,
    private val tableDao: TableDao
) {

    /**
     * Convert TableDto from API to TableEntity for local storage.
     * Table names come from the backend; for local-only use, seed/migration use nomenclature T0021, BT03, OT008.
     */
    private fun TableDto.toEntity(): TableEntity {
        return TableEntity(
            id = this.id,
            name = this.name,
            location = this.location ?: "indoor",
            capacity = this.capacity,
            status = this.status,
            createdAt = null,
            updatedAt = null
        )
    }

    fun getTables(forceRefresh: Boolean = false): Flow<Resource<List<TableEntity>>> = flow {
        try {
            emit(Resource.Loading())

            // Try to fetch from API and store in DB (sync is also done by SyncWorker)
            try {
                val response = apiService.getTables()
                if (response.isSuccessful && response.body() != null) {
                    val tableResponse = response.body()!!
                    val tables = tableResponse.tables.map { it.toEntity() }
                    tableDao.insertTables(tables)
                }
            } catch (_: Exception) {
                // API not available; use whatever is in DB
            }

            val localTables = tableDao.getAllTables().first()
            emit(Resource.Success(localTables))
        } catch (e: Exception) {
            try {
                val localTables = tableDao.getAllTables().first()
                emit(Resource.Success(localTables))
            } catch (_: Exception) {
                emit(Resource.Error("Failed to load tables: ${e.localizedMessage}"))
            }
        }
    }

    fun getTablesByStatus(status: String): Flow<List<TableEntity>> {
        return tableDao.getTablesByStatus(status)
    }

    fun getTablesByLocation(location: String): Flow<List<TableEntity>> {
        return tableDao.getTablesByLocation(location)
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
                // Update locally for offline support
                tableDao.updateTableStatus(tableId, status)
                Resource.Error("Failed to update table status")
            }
        } catch (e: Exception) {
            // Update locally anyway for offline support
            tableDao.updateTableStatus(tableId, status)
            Resource.Success(Unit) // Return success since local update worked
        }
    }
}
