package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.database.entities.SavedPaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPaymentMethodDao {
    @Query("SELECT * FROM saved_payment_method")
    fun getAllSavedPaymentMethods(): Flow<List<SavedPaymentMethodEntity>>
    
    @Query("SELECT * FROM saved_payment_method WHERE id = :id")
    suspend fun getSavedPaymentMethodById(id: String): SavedPaymentMethodEntity?
    
    @Query("SELECT * FROM saved_payment_method WHERE userId = :userId")
    fun getSavedPaymentMethodsByUserId(userId: String): Flow<List<SavedPaymentMethodEntity>>
    
    @Query("SELECT * FROM saved_payment_method WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultPaymentMethod(userId: String): SavedPaymentMethodEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPaymentMethod(method: SavedPaymentMethodEntity)
    
    @Update
    suspend fun updateSavedPaymentMethod(method: SavedPaymentMethodEntity)
    
    @Query("UPDATE saved_payment_method SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultPaymentMethod(userId: String)
    
    @Query("UPDATE saved_payment_method SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultPaymentMethod(id: String)
    
    @Query("DELETE FROM saved_payment_method WHERE id = :id")
    suspend fun deleteSavedPaymentMethod(id: String)
    
    @Query("DELETE FROM saved_payment_method WHERE userId = :userId")
    suspend fun deleteAllUserPaymentMethods(userId: String)
} 