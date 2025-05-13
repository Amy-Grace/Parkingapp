package com.example.parkingactivity.data.models

data class Parkingspot(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val pricePerHour: Double = 0.0,
    val address: String = "")
