package com.example.parkingactivity.di

import android.content.Context
import com.example.parkingactivity.data.database.AppDatabase
import com.example.parkingactivity.data.database.dao.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    // DAOs
    @Provides
    fun provideParkingFacilityDao(database: AppDatabase): ParkingFacilityDao {
        return database.parkingFacilityDao()
    }
    
    @Provides
    fun provideParkingStatusDao(database: AppDatabase): ParkingStatusDao {
        return database.parkingStatusDao()
    }
    
    @Provides
    fun provideParkingSessionDao(database: AppDatabase): ParkingSessionDao {
        return database.parkingSessionDao()
    }
    
    @Provides
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }
    
    @Provides
    fun provideCouponDao(database: AppDatabase): CouponDao {
        return database.couponDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideSavedPaymentMethodDao(database: AppDatabase): SavedPaymentMethodDao {
        return database.savedPaymentMethodDao()
    }
    
    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }
    
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }
    
    // Location
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
