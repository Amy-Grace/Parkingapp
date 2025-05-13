package com.example.parkingactivity.data.models

data class Ticket(
    val ticketId: String = "",
    val userId: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val amountPaid: Double = 0.0
)
