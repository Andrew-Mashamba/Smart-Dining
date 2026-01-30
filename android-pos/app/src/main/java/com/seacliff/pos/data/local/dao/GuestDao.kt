package com.seacliff.pos.data.local.dao

import androidx.room.*
import com.seacliff.pos.data.local.entity.GuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests")
    fun getAllGuests(): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE id = :guestId")
    suspend fun getGuestById(guestId: Long): GuestEntity?

    @Query("SELECT * FROM guests WHERE phone_number = :phoneNumber")
    suspend fun getGuestByPhone(phoneNumber: String): GuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: GuestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuests(guests: List<GuestEntity>)

    @Update
    suspend fun updateGuest(guest: GuestEntity)

    @Delete
    suspend fun deleteGuest(guest: GuestEntity)

    @Query("DELETE FROM guests")
    suspend fun deleteAllGuests()

    @Query("SELECT * FROM guests ORDER BY last_visit_at DESC LIMIT :limit")
    fun getRecentGuests(limit: Int = 10): Flow<List<GuestEntity>>
}
