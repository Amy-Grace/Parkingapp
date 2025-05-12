package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.database.entities.ParkingStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingStatusDao {
    @Query("SELECT * FROM parking_status")
    fun getAllParkingStatuses(): Flow<List<ParkingStatusEntity>>
    
    @Query("SELECT * FROM parking_status WHERE facilityId = :facilityId")
    fun getParkingStatusByFacilityId(facilityId: String): Flow<ParkingStatusEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingStatus(status: ParkingStatusEntity)
    
    @Update
    suspend fun updateParkingStatus(status: ParkingStatusEntity)
    
    @Query("UPDATE parking_status SET availableSpots = :availableSpots WHERE facilityId = :facilityId")
    suspend fun updateAvailableSpots(facilityId: String, availableSpots: Int)
    
    @Query("DELETE FROM parking_status WHERE facilityId = :facilityId")
    suspend fun deleteParkingStatus(facilityId: String)
} 