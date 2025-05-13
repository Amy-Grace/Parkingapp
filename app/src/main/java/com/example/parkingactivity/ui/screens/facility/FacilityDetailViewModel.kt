package com.example.parkingactivity.ui.screens.facility

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingStatus
import com.example.parkingactivity.data.repository.ParkingRepository
import com.example.parkingactivity.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FacilityDetailViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val facilityId: String = checkNotNull(savedStateHandle["facilityId"])
    
    private val _facility = MutableStateFlow<ParkingFacility?>(null)
    val facility: StateFlow<ParkingFacility?> = _facility.asStateFlow()
    
    private val _status = MutableStateFlow<ParkingStatus?>(null)
    val status: StateFlow<ParkingStatus?> = _status.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _startSessionState = MutableStateFlow<StartSessionState>(StartSessionState.Idle)
    val startSessionState: StateFlow<StartSessionState> = _startSessionState.asStateFlow()
    
    init {
        loadFacilityDetails()
    }
    
    private fun loadFacilityDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Get facility details
                _facility.value = parkingRepository.getParkingFacilityById(facilityId)
                
                // Get real-time status
                parkingRepository.getParkingStatusByFacilityId(facilityId).collectLatest { status ->
                    _status.value = status
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error
            }
        }
    }
    
    fun startParkingSession(spotNumber: String? = null) {
        viewModelScope.launch {
            _startSessionState.value = StartSessionState.Loading
            
            try {
                val currentUser = userRepository.getCurrentUser()
                
                if (currentUser == null) {
                    _startSessionState.value = StartSessionState.Error("User not logged in")
                    return@launch
                }
                
                val session = parkingRepository.startParkingSession(
                    userId = currentUser.id,
                    facilityId = facilityId,
                    spotNumber = spotNumber
                )
                
                _startSessionState.value = StartSessionState.Success(session.id)
            } catch (e: Exception) {
                _startSessionState.value = StartSessionState.Error(e.message ?: "Failed to start parking session")
            }
        }
    }
    
    fun resetStartSessionState() {
        _startSessionState.value = StartSessionState.Idle
    }
    
    sealed class StartSessionState {
        object Idle : StartSessionState()
        object Loading : StartSessionState()
        data class Success(val sessionId: String) : StartSessionState()
        data class Error(val message: String) : StartSessionState()
    }
} 