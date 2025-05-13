package com.example.parkingactivity.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.ParkingFacility
import com.example.parkingactivity.data.ParkingSession
import com.example.parkingactivity.data.ParkingStatus
import com.example.parkingactivity.data.repository.ParkingRepository
import com.example.parkingactivity.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _facilities = MutableStateFlow<List<ParkingFacility>>(emptyList())
    val facilities: StateFlow<List<ParkingFacility>> = _facilities.asStateFlow()
    
    private val _statuses = MutableStateFlow<Map<String, ParkingStatus>>(emptyMap())
    val statuses: StateFlow<Map<String, ParkingStatus>> = _statuses.asStateFlow()
    
    private val _activeSessions = MutableStateFlow<List<ParkingSession>>(emptyList())
    val activeSessions: StateFlow<List<ParkingSession>> = _activeSessions.asStateFlow()
    
    private val _selectedFacilityType = MutableStateFlow<FacilityType?>(null)
    val selectedFacilityType: StateFlow<FacilityType?> = _selectedFacilityType.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredFacilities = MutableStateFlow<List<ParkingFacility>>(emptyList())
    val filteredFacilities: StateFlow<List<ParkingFacility>> = _filteredFacilities.asStateFlow()
    
    init {
        loadData()
        
        // Combine facilities, selected type and search query to filter facilities
        viewModelScope.launch {
            combine(
                _facilities,
                _selectedFacilityType,
                _searchQuery
            ) { facilities, selectedType, query ->
                filterFacilities(facilities, selectedType, query)
            }.collect { filtered ->
                _filteredFacilities.value = filtered
            }
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load parking facilities
            parkingRepository.getAllParkingFacilities().collect { facilities ->
                _facilities.value = facilities
                _isLoading.value = false
            }
            
            // Load real-time parking statuses
            parkingRepository.getRealtimeParkingStatuses().collect { statusMap ->
                _statuses.value = statusMap
            }
            
            // Load active sessions for current user
            val currentUser = userRepository.getCurrentUser()
            currentUser?.let { user ->
                parkingRepository.getActiveParkingSessionsByUserId(user.id).collect { sessions ->
                    _activeSessions.value = sessions
                }
            }
        }
    }
    
    fun setSelectedFacilityType(type: FacilityType?) {
        _selectedFacilityType.value = type
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private fun filterFacilities(
        facilities: List<ParkingFacility>,
        selectedType: FacilityType?,
        query: String
    ): List<ParkingFacility> {
        return facilities.filter { facility ->
            val matchesType = selectedType == null || facility.type == selectedType
            val matchesQuery = query.isEmpty() || 
                facility.name.contains(query, ignoreCase = true) ||
                facility.address.contains(query, ignoreCase = true)
            
            matchesType && matchesQuery
        }
    }
    
    fun getAvailableSpotsForFacility(facilityId: String): Int {
        return statuses.value[facilityId]?.availableSpots ?: 0
    }
    
    fun refreshData() {
        loadData()
    }
} 