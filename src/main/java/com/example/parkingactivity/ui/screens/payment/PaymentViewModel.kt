package com.example.parkingactivity.ui.screens.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingSession
import com.example.parkingactivity.data.Payment
import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.repository.CouponRepository
import com.example.parkingactivity.data.repository.ParkingRepository
import com.example.parkingactivity.data.repository.PaymentRepository
import com.example.parkingactivity.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    private val couponRepository: CouponRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    
    private val _session = MutableStateFlow<ParkingSession?>(null)
    val session: StateFlow<ParkingSession?> = _session.asStateFlow()
    
    private val _facility = MutableStateFlow<ParkingFacility?>(null)
    val facility: StateFlow<ParkingFacility?> = _facility.asStateFlow()
    
    private val _amount = MutableStateFlow(0.0)
    val amount: StateFlow<Double> = _amount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()
    
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    private val _savedPaymentMethods = MutableStateFlow<List<com.example.parkingactivity.data.SavedPaymentMethod>>(emptyList())
    val savedPaymentMethods: StateFlow<List<com.example.parkingactivity.data.SavedPaymentMethod>> = _savedPaymentMethods.asStateFlow()
    
    init {
        loadPaymentDetails()
    }
    
    private fun loadPaymentDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Get session details
                val session = parkingRepository.getParkingSessionById(sessionId)
                _session.value = session
                
                // Get facility details
                session?.let { s ->
                    val facility = parkingRepository.getParkingFacilityById(s.facilityId)
                    _facility.value = facility
                    
                    // Calculate amount
                    val calculatedAmount = parkingRepository.calculateParkingFee(sessionId, s.appliedCouponId)
                    _amount.value = calculatedAmount
                    
                    // Get user's saved payment methods
                    val currentUser = userRepository.getCurrentUser()
                    currentUser?.let { user ->
                        userRepository.getSavedPaymentMethodsByUserId(user.id).collect { methods ->
                            _savedPaymentMethods.value = methods
                            
                            // Pre-select default payment method if available
                            val defaultMethod = methods.find { it.isDefault }
                            defaultMethod?.let {
                                _selectedPaymentMethod.value = it.type
                            }
                        }
                    }
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _paymentState.value = PaymentState.Error(e.message ?: "Failed to load payment details")
            }
        }
    }
    
    fun setSelectedPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }
    
    fun processPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing
            
            try {
                val selectedMethod = _selectedPaymentMethod.value
                if (selectedMethod == null) {
                    _paymentState.value = PaymentState.Error("Please select a payment method")
                    return@launch
                }
                
                val payment = when (selectedMethod) {
                    PaymentMethod.MPESA -> processMpesaPayment()
                    PaymentMethod.PAYPAL -> processPayPalPayment()
                }
                
                if (payment != null) {
                    _paymentState.value = PaymentState.Success(payment)
                    onSuccess()
                } else {
                    _paymentState.value = PaymentState.Error("Payment failed")
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Payment processing failed")
            }
        }
    }
    
    private suspend fun processMpesaPayment(): Payment? {
        // In a real app, this would integrate with the M-Pesa API
        return paymentRepository.processMpesaPayment(
            sessionId = sessionId,
            amount = amount.value,
            mpesaResponse = "success" // Mock response
        )
    }
    
    private suspend fun processPayPalPayment(): Payment? {
        // In a real app, this would integrate with the PayPal API
        return paymentRepository.processPayPalPayment(
            sessionId = sessionId,
            amount = amount.value,
            payPalResponse = "success" // Mock response
        )
    }
    
    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
    
    sealed class PaymentState {
        object Idle : PaymentState()
        object Processing : PaymentState()
        data class Success(val payment: Payment) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }
} 