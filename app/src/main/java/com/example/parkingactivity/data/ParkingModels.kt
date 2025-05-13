package com.example.parkingactivity.data

import java.util.Date
import java.util.UUID

// Represents a parking facility (mall, hospital, park)
data class ParkingFacility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FacilityType,
    val address: String,
    val totalSpots: Int,
    val hourlyRate: Double,
    val latitude: Double,
    val longitude: Double
)

enum class FacilityType {
    MALL, HOSPITAL, PARK, OTHER
}

// Represents the current status of a parking facility
data class ParkingStatus(
    val facilityId: String,
    val availableSpots: Int,
    val lastUpdated: Date = Date()
)

// Represents a user parking session
data class ParkingSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val facilityId: String,
    val entryTime: Date = Date(),
    val exitTime: Date? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val appliedCouponId: String? = null,
    val spotNumber: String? = null
)

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED
}

// Represents a payment for a parking session
data class Payment(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val timestamp: Date = Date(),
    val status: PaymentStatus
)

enum class PaymentMethod {
    MPESA, PAYPAL
}

// Represents a discount coupon
data class Coupon(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val discountPercentage: Int,
    val expiryDate: Date,
    val facilityId: String? = null // null means applicable to all facilities
)

// Represents a user of the app
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val savedPaymentMethods: List<SavedPaymentMethod> = emptyList()
)

// Represents a saved payment method for a user
data class SavedPaymentMethod(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: PaymentMethod,
    val isDefault: Boolean = false,
    val lastFour: String? = null,
    val nickName: String? = null
) 