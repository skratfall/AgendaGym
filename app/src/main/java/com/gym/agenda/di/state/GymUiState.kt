package com.gym.agenda.presentation.state

import com.gym.agenda.data.model.GymAppointment

data class GymListState(
    val isLoading: Boolean = true,
    val items: List<GymAppointment> = emptyList(),
    val error: String? = null
)

data class FormState(
    val clientName: String = "",
    val service: String = "",
    val dateMillis: Long = 0L,
    val timeHour: Int = 9,
    val timeMinute: Int = 0,
    val notes: String = "",
    val isValid: Boolean = false
)