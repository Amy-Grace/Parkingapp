package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.database.entities.ParkingFacilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingFacilityDao {
    @Query("SELECT * FROM parking_facility")
    fun getAllParkingFacilities(): Flow<List<ParkingFacilityEntity>>
    
    @Query("SELECT * FROM parking_facility WHERE type = :type")
    fun getParkingFacilitiesByType(type: FacilityType): Flow<List<ParkingFacilityEntity>>
    
    @Query("SELECT * FROM parking_facility WHERE id = :id")
    suspend fun getParkingFacilityById(id: String): ParkingFacilityEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingFacility(facility: ParkingFacilityEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingFacilities(facilities: List<ParkingFacilityEntity>)
    
    @Update
    suspend fun updateParkingFacility(facility: ParkingFacilityEntity)
    
    @Delete
    suspend fun deleteParkingFacility(facility: ParkingFacilityEntity)
    
    @Query("DELETE FROM parking_facility")
    suspend fun deleteAllParkingFacilities()
} 