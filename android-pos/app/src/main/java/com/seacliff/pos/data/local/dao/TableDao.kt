package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.TableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM tables ORDER BY name")
    fun getAllTables(): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE id = :tableId")
    suspend fun getTableById(tableId: Long): TableEntity?

    @Query("SELECT * FROM tables WHERE status = :status ORDER BY name")
    fun getTablesByStatus(status: String): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE location = :location ORDER BY name")
    fun getTablesByLocation(location: String): Flow<List<TableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Update
    suspend fun updateTable(table: TableEntity)

    @Query("UPDATE tables SET status = :status WHERE id = :tableId")
    suspend fun updateTableStatus(tableId: Long, status: String)

    @Delete
    suspend fun deleteTable(table: TableEntity)

    @Query("DELETE FROM tables")
    suspend fun deleteAllTables()
}
