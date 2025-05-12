package com.example.parkingactivity.ui.screens.map

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingStatus
import com.example.parkingactivity.data.repository.ParkingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    
    private val _facilities = MutableStateFlow<List<ParkingFacility>>(emptyList())
    val facilities: StateFlow<List<ParkingFacility>> = _facilities.asStateFlow()
    
    private val _statuses = MutableStateFlow<Map<String, ParkingStatus>>(emptyMap())
    val statuses: StateFlow<Map<String, ParkingStatus>> = _statuses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    
    private var locationCallback: LocationCallback? = null
    
    init {
        loadFacilities()
        setupLocationUpdates()
    }
    
    private fun loadFacilities() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load all facilities
            parkingRepository.getAllParkingFacilities().collect { facilities ->
                _facilities.value = facilities
                _isLoading.value = false
            }
            
            // Get real-time statuses
            parkingRepository.getRealtimeParkingStatuses().collect { statusMap ->
                _statuses.value = statusMap
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        try {
            // Get last known location first
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
                
                // Then start location updates
                val locationRequest = LocationRequest.Builder(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                
                locationCallback?.let {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        it,
                        null
                    )
                }
            }
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }
    
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
} 