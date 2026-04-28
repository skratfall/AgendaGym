package com.gym.agenda.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_appointments")
data class GymAppointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val service: String, // Ej: "Personal Trainer", "Yoga", "Spinning"
    val dateMillis: Long,
    val timeHour: Int,
    val timeMinute: Int,
    val notes: String = ""
)