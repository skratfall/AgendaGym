package com.gym.agenda.di.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val totalRevenue: Double = 0.0,
    val totalAppointments: Int = 0,
    val popularService: String = "N/A",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = authRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<GymAppointment>> = appointmentRepository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AdminUiState> = allAppointments.map { appointments ->
        if (appointments.isEmpty()) {
            AdminUiState()
        } else {
            val totalRevenue = appointments.sumOf { it.price }
            val totalCount = appointments.size
            val mostPopular = appointments.groupBy { it.service }
                .maxByOrNull { it.value.size }?.key ?: "N/A"
            
            AdminUiState(
                totalRevenue = totalRevenue,
                totalAppointments = totalCount,
                popularService = mostPopular
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminUiState(isLoading = true))

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            authRepository.updateUserRole(userId, role)
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(appointmentId, status)
        }
    }
}