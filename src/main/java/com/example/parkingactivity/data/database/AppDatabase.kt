package com.example.parkingactivity.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.parkingactivity.data.database.dao.*
import com.example.parkingactivity.data.database.entities.*

@Database(
    entities = [
        ParkingFacilityEntity::class,
        ParkingStatusEntity::class,
        ParkingSessionEntity::class,
        PaymentEntity::class,
        CouponEntity::class,
        UserEntity::class,
        SavedPaymentMethodEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun parkingFacilityDao(): ParkingFacilityDao
    abstract fun parkingStatusDao(): ParkingStatusDao
    abstract fun parkingSessionDao(): ParkingSessionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun couponDao(): CouponDao
    abstract fun userDao(): UserDao
    abstract fun savedPaymentMethodDao(): SavedPaymentMethodDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parking_app_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 