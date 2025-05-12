package com.example.parkingactivity.ui.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.Coupon
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingSession
import com.example.parkingactivity.data.repository.CouponRepository
import com.example.parkingactivity.data.repository.CouponRepository.CouponValidationResult
import com.example.parkingactivity.data.repository.ParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val couponRepository: CouponRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    
    private val _session = MutableStateFlow<ParkingSession?>(null)
    val session: StateFlow<ParkingSession?> = _session.asStateFlow()
    
    private val _facility = MutableStateFlow<ParkingFacility?>(null)
    val facility: StateFlow<ParkingFacility?> = _facility.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _duration = MutableStateFlow("00:00:00")
    val duration: StateFlow<String> = _duration.asStateFlow()
    
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()
    
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()
    
    private val _couponValidationState = MutableStateFlow<CouponValidationState>(CouponValidationState.None)
    val couponValidationState: StateFlow<CouponValidationState> = _couponValidationState.asStateFlow()
    
    init {
        loadSessionDetails()
        startDurationTimer()
    }
    
    private fun loadSessionDetails() {
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
                    
                    // Get applied coupon if any
                    s.appliedCouponId?.let { couponId ->
                        // In a real app, you'd get the coupon details
                    }
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error
            }
        }
    }
    
    private fun startDurationTimer() {
        viewModelScope.launch {
            while (true) {
                _session.value?.let { session ->
                    val startTime = session.entryTime.time
                    val currentTime = System.currentTimeMillis()
                    val durationMs = currentTime - startTime
                    
                    _duration.value = formatDuration(durationMs)
                    
                    // Calculate amount
                    _facility.value?.let { facility ->
                        val durationHours = durationMs / (1000.0 * 60 * 60)
                        val amount = durationHours * facility.hourlyRate
                        _totalAmount.value = amount
                    }
                }
                
                delay(1000) // Update every second
            }
        }
    }
    
    private fun formatDuration(durationMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    fun applyCoupon(couponCode: String) {
        viewModelScope.launch {
            _couponValidationState.value = CouponValidationState.Loading
            
            try {
                // Validate the coupon
                when (val result = couponRepository.validateCoupon(couponCode, _facility.value?.id)) {
                    is CouponValidationResult.Valid -> {
                        val coupon = result.coupon
                        _appliedCoupon.value = coupon
                        
                        // Apply the discount
                        val discountedAmount = _totalAmount.value * (1 - coupon.discountPercentage / 100.0)
                        _totalAmount.value = discountedAmount
                        
                        // Update in database
                        parkingRepository.parkingSessionDao.applyCoupon(sessionId, coupon.id)
                        
                        _couponValidationState.value = CouponValidationState.Valid(coupon)
                    }
                    is CouponValidationResult.Invalid -> {
                        _couponValidationState.value = CouponValidationState.Invalid(result.reason)
                    }
                }
            } catch (e: Exception) {
                _couponValidationState.value = CouponValidationState.Invalid(e.message ?: "Error validating coupon")
            }
        }
    }
    
    fun endSession(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val updatedSession = parkingRepository.endParkingSession(sessionId)
                _session.value = updatedSession
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    sealed class CouponValidationState {
        object None : CouponValidationState()
        object Loading : CouponValidationState()
        data class Valid(val coupon: Coupon) : CouponValidationState()
        data class Invalid(val reason: String) : CouponValidationState()
    }
} 