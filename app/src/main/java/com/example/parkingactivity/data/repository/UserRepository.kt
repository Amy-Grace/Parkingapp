package com.example.parkingactivity.data.repository

import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.SavedPaymentMethod
import com.example.parkingactivity.data.User
import com.example.parkingactivity.data.database.dao.SavedPaymentMethodDao
import com.example.parkingactivity.data.database.dao.UserDao
import com.example.parkingactivity.data.database.entities.SavedPaymentMethodEntity
import com.example.parkingactivity.data.database.entities.UserEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val savedPaymentMethodDao: SavedPaymentMethodDao,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun createUser(email: String, password: String, name: String, phone: String): User {
        // Create Firebase auth user
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Failed to create user")
        
        val user = User(
            id = firebaseUser.uid,
            name = name,
            email = email,
            phone = phone
        )
        
        userDao.insertUser(user.toEntity())
        
        return user
    }
    
    suspend fun loginUser(email: String, password: String): User? {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: return null
        
        // Get or create the local user
        var user = userDao.getUserById(firebaseUser.uid)?.toDomainModel()
        if (user == null) {
            user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                phone = firebaseUser.phoneNumber ?: ""
            )
            userDao.insertUser(user.toEntity())
        }
        
        return user
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return userDao.getUserById(firebaseUser.uid)?.toDomainModel()
    }
    
    suspend fun logoutUser() {
        firebaseAuth.signOut()
    }
    
    suspend fun updateUserProfile(user: User): User {
        userDao.updateUser(user.toEntity())
        return user
    }
    
    fun getSavedPaymentMethodsByUserId(userId: String): Flow<List<SavedPaymentMethod>> {
        return savedPaymentMethodDao.getSavedPaymentMethodsByUserId(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun addSavedPaymentMethod(
        userId: String,
        type: PaymentMethod,
        lastFour: String? = null,
        nickName: String? = null,
        isDefault: Boolean = false
    ): SavedPaymentMethod {
        // If this is set as default, clear any existing defaults
        if (isDefault) {
            savedPaymentMethodDao.clearDefaultPaymentMethod(userId)
        }
        
        val paymentMethod = SavedPaymentMethod(
            userId = userId,
            type = type,
            isDefault = isDefault,
            lastFour = lastFour,
            nickName = nickName
        )
        
        savedPaymentMethodDao.insertSavedPaymentMethod(paymentMethod.toEntity())
        
        return paymentMethod
    }
    
    suspend fun setDefaultPaymentMethod(methodId: String, userId: String): SavedPaymentMethod? {
        // Clear existing defaults
        savedPaymentMethodDao.clearDefaultPaymentMethod(userId)
        
        // Set new default
        savedPaymentMethodDao.setDefaultPaymentMethod(methodId)
        
        return savedPaymentMethodDao.getSavedPaymentMethodById(methodId)?.toDomainModel()
    }
    
    suspend fun deleteSavedPaymentMethod(methodId: String) {
        savedPaymentMethodDao.deleteSavedPaymentMethod(methodId)
    }
    
    private fun UserEntity.toDomainModel(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone = phone
        )
    }
    
    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            phone = phone
        )
    }
    
    private fun SavedPaymentMethodEntity.toDomainModel(): SavedPaymentMethod {
        return SavedPaymentMethod(
            id = id,
            userId = userId,
            type = type,
            isDefault = isDefault,
            lastFour = lastFour,
            nickName = nickName
        )
    }
    
    private fun SavedPaymentMethod.toEntity(): SavedPaymentMethodEntity {
        return SavedPaymentMethodEntity(
            id = id,
            userId = userId,
            type = type,
            isDefault = isDefault,
            lastFour = lastFour,
            nickName = nickName
        )
    }
}