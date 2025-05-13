package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.PaymentStatus
import com.example.parkingactivity.data.database.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment")
    fun getAllPayments(): Flow<List<PaymentEntity>>
    
    @Query("SELECT * FROM payment WHERE id = :id")
    suspend fun getPaymentById(id: String): PaymentEntity?
    
    @Query("SELECT * FROM payment WHERE sessionId = :sessionId")
    fun getPaymentsBySessionId(sessionId: String): Flow<List<PaymentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
    
    @Update
    suspend fun updatePayment(payment: PaymentEntity)
    
    @Query("UPDATE payment SET status = :status WHERE id = :id")
    suspend fun updatePaymentStatus(id: String, status: PaymentStatus)
    
    @Query("DELETE FROM payment WHERE id = :id")
    suspend fun deletePayment(id: String)
} 