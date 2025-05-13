package com.example.parkingactivity.data.repository

import com.example.parkingactivity.data.Payment
import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.PaymentStatus
import com.example.parkingactivity.data.database.dao.PaymentDao
import com.example.parkingactivity.data.database.dao.ParkingSessionDao
import com.example.parkingactivity.data.database.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao,
    private val parkingSessionDao: ParkingSessionDao
) {
    fun getPaymentsBySessionId(sessionId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsBySessionId(sessionId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun processPayment(
        sessionId: String,
        amount: Double,
        paymentMethod: PaymentMethod
    ): Payment {
        // In a real app, this would integrate with a payment gateway
        // Here we're simulating a successful payment
        
        val transactionId = UUID.randomUUID().toString()
        
        val payment = Payment(
            sessionId = sessionId,
            amount = amount,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            status = PaymentStatus.COMPLETED
        )
        
        paymentDao.insertPayment(payment.toEntity())
        
        // Update the session payment status
        parkingSessionDao.updatePaymentStatus(sessionId, PaymentStatus.COMPLETED)
        
        return payment
    }
    
    suspend fun processPayPalPayment(
        sessionId: String,
        amount: Double,
        payPalResponse: String
    ): Payment {
        // In a real app, this would parse the PayPal response and verify the payment
        // Here we're simulating a successful payment
        
        return processPayment(sessionId, amount, PaymentMethod.PAYPAL)
    }
    
    suspend fun processMpesaPayment(
        sessionId: String,
        amount: Double,
        mpesaResponse: String
    ): Payment {
        // In a real app, this would parse the M-Pesa response and verify the payment
        // Here we're simulating a successful payment
        
        return processPayment(sessionId, amount, PaymentMethod.MPESA)
    }
    
    private fun PaymentEntity.toDomainModel(): Payment {
        return Payment(
            id = id,
            sessionId = sessionId,
            amount = amount,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            timestamp = timestamp,
            status = status
        )
    }
    
    private fun Payment.toEntity(): PaymentEntity {
        return PaymentEntity(
            id = id,
            sessionId = sessionId,
            amount = amount,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            timestamp = timestamp,
            status = status
        )
    }
}
 