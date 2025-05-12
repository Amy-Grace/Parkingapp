package com.example.parkingactivity.data.models

data class Parkingspot(
    val id: String = ""
)data class Location(
    val name: String = "",
    val totalSpots: Int = 0,
    val availableSpots: Int = 0,
    val isAvailable: Boolean = true,
    val pricePerHour: Double = 0.0
)
