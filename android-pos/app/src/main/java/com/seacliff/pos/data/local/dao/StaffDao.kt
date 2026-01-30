package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :staffId")
    suspend fun getStaffById(staffId: Long): StaffEntity?

    @Query("SELECT * FROM staff WHERE email = :email")
    suspend fun getStaffByEmail(email: String): StaffEntity?

    @Query("SELECT * FROM staff WHERE role = :role AND status = 'active'")
    fun getStaffByRole(role: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE status = 'active'")
    fun getActiveStaff(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staff: List<StaffEntity>)

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)

    @Query("DELETE FROM staff")
    suspend fun deleteAllStaff()
}
