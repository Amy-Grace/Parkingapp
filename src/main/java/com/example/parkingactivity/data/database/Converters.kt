package com.example.parkingactivity.data.database

import androidx.room.TypeConverter
import com.example.parkingactivity.data.FacilityType
import com.example.parkingactivity.data.PaymentMethod
import com.example.parkingactivity.data.PaymentStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromFacilityType(value: FacilityType): String {
        return value.name
    }

    @TypeConverter
    fun toFacilityType(value: String): FacilityType {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return enumValueOf(value)
    }
} 