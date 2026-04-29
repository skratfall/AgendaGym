package com.gym.agenda.state

import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.User

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

data class AppointmentListUiState(
    val isLoading: Boolean = false,
    val appointments: List<GymAppointment> = emptyList(),
    val errorMessage: String? = null,
    val filterStatus: AppointmentStatus? = null
)

data class AddEditAppointmentUiState(
    val isLoading: Boolean = false,
    val appointment: GymAppointment? = null,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val upcomingAppointments: Int = 0,
    val totalAppointments: Int = 0,
    val errorMessage: String? = null
)