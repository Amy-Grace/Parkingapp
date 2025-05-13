package com.example.parkingactivity.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingactivity.data.User
import com.example.parkingactivity.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val currentUser = userRepository.getCurrentUser()
                _user.value = currentUser
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.logoutUser()
                _user.value = null
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun refreshUserProfile() {
        loadUserProfile()
    }
}