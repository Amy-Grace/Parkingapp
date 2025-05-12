package com.example.parkingactivity.data.repository

import com.example.parkingactivity.data.Coupon
import com.example.parkingactivity.data.database.dao.CouponDao
import com.example.parkingactivity.data.database.entities.CouponEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val couponDao: CouponDao
) {
    fun getAllCoupons(): Flow<List<Coupon>> {
        return couponDao.getAllCoupons().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getValidCoupons(): Flow<List<Coupon>> {
        return couponDao.getValidCoupons(Date()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getCouponsByFacilityId(facilityId: String): Flow<List<Coupon>> {
        return couponDao.getCouponsByFacilityId(facilityId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getCouponByCode(code: String): Coupon? {
        return couponDao.getCouponByCode(code)?.toDomainModel()
    }
    
    suspend fun validateCoupon(code: String, facilityId: String? = null): CouponValidationResult {
        val coupon = couponDao.getCouponByCode(code) ?: return CouponValidationResult.Invalid("Coupon not found")
        
        // Check if coupon is expired
        if (coupon.expiryDate.before(Date())) {
            return CouponValidationResult.Invalid("Coupon has expired")
        }
        
        // Check if coupon is applicable for this facility
        if (coupon.facilityId != null && facilityId != null && coupon.facilityId != facilityId) {
            return CouponValidationResult.Invalid("Coupon not applicable for this facility")
        }
        
        return CouponValidationResult.Valid(coupon.toDomainModel())
    }
    
    suspend fun applyCouponDiscount(amount: Double, couponId: String): Double {
        val coupon = couponDao.getCouponById(couponId) ?: return amount
        
        // Check if coupon is still valid
        if (coupon.expiryDate.before(Date())) {
            return amount
        }
        
        // Apply discount
        val discountAmount = amount * (coupon.discountPercentage / 100.0)
        return amount - discountAmount
    }
    
    suspend fun createCoupon(
        code: String,
        discountPercentage: Int,
        expiryDate: Date,
        facilityId: String? = null
    ): Coupon {
        val coupon = Coupon(
            code = code,
            discountPercentage = discountPercentage,
            expiryDate = expiryDate,
            facilityId = facilityId
        )
        
        couponDao.insertCoupon(coupon.toEntity())
        
        return coupon
    }
    
    suspend fun deleteCoupon(couponId: String) {
        couponDao.deleteCoupon(couponId)
    }
    
    private fun CouponEntity.toDomainModel(): Coupon {
        return Coupon(
            id = id,
            code = code,
            discountPercentage = discountPercentage,
            expiryDate = expiryDate,
            facilityId = facilityId
        )
    }
    
    private fun Coupon.toEntity(): CouponEntity {
        return CouponEntity(
            id = id,
            code = code,
            discountPercentage = discountPercentage,
            expiryDate = expiryDate,
            facilityId = facilityId
        )
    }
    
    sealed class CouponValidationResult {
        data class Valid(val coupon: Coupon) : CouponValidationResult()
        data class Invalid(val reason: String) : CouponValidationResult()
    }
} 