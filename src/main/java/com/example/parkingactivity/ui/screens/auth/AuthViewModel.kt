package com.example.parkingactivity.ui.screens.auth

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
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        // Check if user is already logged in
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }
    
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = userRepository.loginUser(email, password)
                if (user != null) {
                    _currentUser.value = user
                    onSuccess()
                } else {
                    onError("Failed to login")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred during login")
            }
        }
    }
    
    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = userRepository.createUser(email, password, name, phone)
                _currentUser.value = user
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred during registration")
            }
        }
    }
    
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.logoutUser()
            _currentUser.value = null
            onComplete()
        }
    }
    
    fun updateUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedUser = userRepository.updateUserProfile(user)
                _currentUser.value = updatedUser
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred while updating profile")
            }
        }
    }
} 