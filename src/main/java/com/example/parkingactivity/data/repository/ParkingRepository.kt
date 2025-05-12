package com.example.parkingactivity.data.repository

import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingSession
import com.example.parkingactivity.data.ParkingStatus
import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.PaymentStatus
import com.example.parkingactivity.data.database.dao.ParkingFacilityDao
import com.example.parkingactivity.data.database.dao.ParkingSessionDao
import com.example.parkingactivity.data.database.dao.ParkingStatusDao
import com.example.parkingactivity.data.database.entities.ParkingFacilityEntity
import com.example.parkingactivity.data.database.entities.ParkingSessionEntity
import com.example.parkingactivity.data.database.entities.ParkingStatusEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingRepository @Inject constructor(
    private val parkingFacilityDao: ParkingFacilityDao,
    private val parkingStatusDao: ParkingStatusDao,
    private val parkingSessionDao: ParkingSessionDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    // ParkingFacility operations
    fun getAllParkingFacilities(): Flow<List<ParkingFacility>> {
        return parkingFacilityDao.getAllParkingFacilities().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getParkingFacilitiesByType(type: FacilityType): Flow<List<ParkingFacility>> {
        return parkingFacilityDao.getParkingFacilitiesByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getParkingFacilityById(id: String): ParkingFacility? {
        return parkingFacilityDao.getParkingFacilityById(id)?.toDomainModel()
    }
    
    suspend fun saveParkingFacility(facility: ParkingFacility) {
        parkingFacilityDao.insertParkingFacility(facility.toEntity())
        
        // Also update in Firebase for real-time access
        firebaseDatabase.getReference("parking_facilities")
            .child(facility.id)
            .setValue(facility)
    }
    
    // ParkingStatus operations
    fun getParkingStatusByFacilityId(facilityId: String): Flow<ParkingStatus?> {
        // Start observing real-time status from Firebase
        observeParkingStatusFromFirebase(facilityId)
        
        return parkingStatusDao.getParkingStatusByFacilityId(facilityId).map { entity ->
            entity?.toDomainModel()
        }
    }
    
    suspend fun updateParkingStatus(status: ParkingStatus) {
        parkingStatusDao.insertParkingStatus(status.toEntity())
        
        // Update in Firebase for real-time access
        firebaseDatabase.getReference("parking_statuses")
            .child(status.facilityId)
            .setValue(status)
    }
    
    private fun observeParkingStatusFromFirebase(facilityId: String) {
        firebaseDatabase.getReference("parking_statuses")
            .child(facilityId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseStatus = snapshot.getValue(ParkingStatus::class.java)
                    firebaseStatus?.let {
                        // Update local database with Firebase data
                        parkingStatusDao.insertParkingStatus(it.toEntity())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
    
    // Get real-time updates of parking statuses from Firebase
    fun getRealtimeParkingStatuses(): Flow<Map<String, ParkingStatus>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val statuses = mutableMapOf<String, ParkingStatus>()
                for (childSnapshot in snapshot.children) {
                    val facilityId = childSnapshot.key ?: continue
                    val status = childSnapshot.getValue(ParkingStatus::class.java) ?: continue
                    statuses[facilityId] = status
                }
                trySend(statuses)
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        val reference = firebaseDatabase.getReference("parking_statuses")
        reference.addValueEventListener(listener)
        
        awaitClose {
            reference.removeEventListener(listener)
        }
    }
    
    // ParkingSession operations
    fun getParkingSessionsByUserId(userId: String): Flow<List<ParkingSession>> {
        return parkingSessionDao.getParkingSessionsByUserId(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getActiveParkingSessionsByUserId(userId: String): Flow<List<ParkingSession>> {
        return parkingSessionDao.getActiveParkingSessionsByUserId(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getParkingSessionById(id: String): ParkingSession? {
        return parkingSessionDao.getParkingSessionById(id)?.toDomainModel()
    }
    
    suspend fun startParkingSession(userId: String, facilityId: String, spotNumber: String? = null): ParkingSession {
        val session = ParkingSession(
            userId = userId,
            facilityId = facilityId,
            entryTime = Date(),
            paymentStatus = PaymentStatus.PENDING,
            spotNumber = spotNumber
        )
        
        parkingSessionDao.insertParkingSession(session.toEntity())
        
        // Update available spots in parking status
        val currentStatus = parkingStatusDao.getParkingStatusByFacilityId(facilityId).map { it }.firstOrNull()
        currentStatus?.let {
            val newAvailableSpots = (it.availableSpots - 1).coerceAtLeast(0)
            val updatedStatus = it.copy(availableSpots = newAvailableSpots, lastUpdated = Date())
            parkingStatusDao.updateParkingStatus(updatedStatus)
            
            // Update in Firebase
            firebaseDatabase.getReference("parking_statuses")
                .child(facilityId)
                .setValue(updatedStatus.toDomainModel())
        }
        
        return session
    }
    
    suspend fun endParkingSession(sessionId: String): ParkingSession? {
        val session = parkingSessionDao.getParkingSessionById(sessionId) ?: return null
        
        val updatedSession = session.copy(exitTime = Date())
        parkingSessionDao.updateParkingSession(updatedSession)
        
        // Update available spots in parking status
        val currentStatus = parkingStatusDao.getParkingStatusByFacilityId(session.facilityId).map { it }.firstOrNull()
        currentStatus?.let {
            val newAvailableSpots = it.availableSpots + 1
            val updatedStatus = it.copy(availableSpots = newAvailableSpots, lastUpdated = Date())
            parkingStatusDao.updateParkingStatus(updatedStatus)
            
            // Update in Firebase
            firebaseDatabase.getReference("parking_statuses")
                .child(session.facilityId)
                .setValue(updatedStatus.toDomainModel())
        }
        
        return updatedSession.toDomainModel()
    }
    
    suspend fun calculateParkingFee(sessionId: String, appliedCouponId: String? = null): Double {
        val session = parkingSessionDao.getParkingSessionById(sessionId) ?: return 0.0
        val facility = parkingFacilityDao.getParkingFacilityById(session.facilityId) ?: return 0.0
        
        val entryTime = session.entryTime.time
        val exitTime = session.exitTime?.time ?: System.currentTimeMillis()
        
        // Calculate duration in hours
        val durationMs = exitTime - entryTime
        val durationHours = durationMs / (1000.0 * 60 * 60)
        
        // Calculate base fee
        val baseFee = durationHours * facility.hourlyRate
        
        // TODO: Apply discount if coupon is provided
        // This would involve coupon repository logic
        
        return baseFee
    }
    
    // Extension functions to convert between domain models and entities
    private fun ParkingFacilityEntity.toDomainModel(): ParkingFacility {
        return ParkingFacility(
            id = id,
            name = name,
            type = type,
            address = address,
            totalSpots = totalSpots,
            hourlyRate = hourlyRate,
            latitude = latitude,
            longitude = longitude
        )
    }
    
    private fun ParkingFacility.toEntity(): ParkingFacilityEntity {
        return ParkingFacilityEntity(
            id = id,
            name = name,
            type = type,
            address = address,
            totalSpots = totalSpots,
            hourlyRate = hourlyRate,
            latitude = latitude,
            longitude = longitude
        )
    }
    
    private fun ParkingStatusEntity.toDomainModel(): ParkingStatus {
        return ParkingStatus(
            facilityId = facilityId,
            availableSpots = availableSpots,
            lastUpdated = lastUpdated
        )
    }
    
    private fun ParkingStatus.toEntity(): ParkingStatusEntity {
        return ParkingStatusEntity(
            facilityId = facilityId,
            availableSpots = availableSpots,
            lastUpdated = lastUpdated
        )
    }
    
    private fun ParkingSessionEntity.toDomainModel(): ParkingSession {
        return ParkingSession(
            id = id,
            userId = userId,
            facilityId = facilityId,
            entryTime = entryTime,
            exitTime = exitTime,
            paymentStatus = paymentStatus,
            appliedCouponId = appliedCouponId,
            spotNumber = spotNumber
        )
    }
    
    private fun ParkingSession.toEntity(): ParkingSessionEntity {
        return ParkingSessionEntity(
            id = id,
            userId = userId,
            facilityId = facilityId,
            entryTime = entryTime,
            exitTime = exitTime,
            paymentStatus = paymentStatus,
            appliedCouponId = appliedCouponId,
            spotNumber = spotNumber
        )
    }
}