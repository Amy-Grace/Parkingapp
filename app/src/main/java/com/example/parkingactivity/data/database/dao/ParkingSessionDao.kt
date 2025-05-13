package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.PaymentStatus
import com.example.parkingactivity.data.database.entities.ParkingSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ParkingSessionDao {
    @Query("SELECT * FROM parking_session")
    fun getAllParkingSessions(): Flow<List<ParkingSessionEntity>>
    
    @Query("SELECT * FROM parking_session WHERE id = :id")
    suspend fun getParkingSessionById(id: String): ParkingSessionEntity?
    
    @Query("SELECT * FROM parking_session WHERE userId = :userId")
    fun getParkingSessionsByUserId(userId: String): Flow<List<ParkingSessionEntity>>
    
    @Query("SELECT * FROM parking_session WHERE facilityId = :facilityId")
    fun getParkingSessionsByFacilityId(facilityId: String): Flow<List<ParkingSessionEntity>>
    
    @Query("SELECT * FROM parking_session WHERE userId = :userId AND exitTime IS NULL")
    fun getActiveParkingSessionsByUserId(userId: String): Flow<List<ParkingSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingSession(session: ParkingSessionEntity)
    
    @Update
    suspend fun updateParkingSession(session: ParkingSessionEntity)
    
    @Query("UPDATE parking_session SET exitTime = :exitTime WHERE id = :id")
    suspend fun updateExitTime(id: String, exitTime: Date)
    
    @Query("UPDATE parking_session SET paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updatePaymentStatus(id: String, paymentStatus: PaymentStatus)
    
    @Query("UPDATE parking_session SET appliedCouponId = :couponId WHERE id = :id")
    suspend fun applyCoupon(id: String, couponId: String)
    
    @Query("DELETE FROM parking_session WHERE id = :id")
    suspend fun deleteParkingSession(id: String)
} 