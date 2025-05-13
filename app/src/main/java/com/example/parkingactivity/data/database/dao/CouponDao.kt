package com.example.parkingactivity.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parkingactivity.data.database.entities.CouponEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupon")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupon WHERE id = :id")
    suspend fun getCouponById(id: String): CouponEntity?

    @Query("SELECT * FROM coupon WHERE code = :code")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Query("SELECT * FROM coupon WHERE facilityId = :facilityId OR facilityId IS NULL")
    fun getCouponsByFacilityId(facilityId: String): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupon WHERE expiryDate > :currentDate")
    fun getValidCoupons(currentDate: Date): Flow<List<CouponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Query("DELETE FROM coupon WHERE id = :id")
    suspend fun deleteCoupon(id: String)
}