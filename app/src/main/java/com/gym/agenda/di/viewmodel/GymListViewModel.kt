package com.gym.agenda.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.state.AppointmentListUiState
import com.gym.agenda.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GymListViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentListUiState())
    val uiState: StateFlow<AppointmentListUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<GymAppointment>>(emptyList())
    val appointments: StateFlow<List<GymAppointment>> = _appointments.asStateFlow()

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            authRepository.authState
                .filterNotNull()
                .flatMapLatest { user ->
                    appointmentRepository.getAppointments(user.id)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Error al cargar citas"
                    )
                }
                .collect { list ->
                    _appointments.value = list
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = list
                    )
                }
        }
    }

    fun deleteAppointment(appointment: GymAppointment) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointment.id)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al eliminar"
                    )
                }
        }
    }

    fun filterByStatus(status: AppointmentStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getFilteredAppointments(): List<GymAppointment> {
        return _uiState.value.filterStatus?.let { status ->
            _appointments.value.filter { it.status == status }
        } ?: _appointments.value
    }
}