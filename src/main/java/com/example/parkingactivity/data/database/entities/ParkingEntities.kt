package com.example.parkingactivity.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.PaymentStatus
import java.util.Date

@Entity(tableName = "parking_facility")
data class ParkingFacilityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: FacilityType,
    val address: String,
    val totalSpots: Int,
    val hourlyRate: Double,
    val latitude: Double,
    val longitude: Double
)

@Entity(
    tableName = "parking_status",
    foreignKeys = [
        ForeignKey(
            entity = ParkingFacilityEntity::class,
            parentColumns = ["id"],
            childColumns = ["facilityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("facilityId")]
)
data class ParkingStatusEntity(
    @PrimaryKey
    val facilityId: String,
    val availableSpots: Int,
    val lastUpdated: Date
)

@Entity(
    tableName = "parking_session",
    foreignKeys = [
        ForeignKey(
            entity = ParkingFacilityEntity::class,
            parentColumns = ["id"],
            childColumns = ["facilityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CouponEntity::class,
            parentColumns = ["id"],
            childColumns = ["appliedCouponId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("facilityId"), Index("userId"), Index("appliedCouponId")]
)
data class ParkingSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val facilityId: String,
    val entryTime: Date,
    val exitTime: Date?,
    val paymentStatus: PaymentStatus,
    val appliedCouponId: String?,
    val spotNumber: String?
)

@Entity(
    tableName = "payment",
    foreignKeys = [
        ForeignKey(
            entity = ParkingSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val timestamp: Date,
    val status: PaymentStatus
)

@Entity(tableName = "coupon")
data class CouponEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val discountPercentage: Int,
    val expiryDate: Date,
    val facilityId: String?
)

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String
)

@Entity(
    tableName = "saved_payment_method",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SavedPaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: PaymentMethod,
    val isDefault: Boolean,
    val lastFour: String?,
    val nickName: String?
) 