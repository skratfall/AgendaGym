package com.gym.agenda.di.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.di.navigation.NavArgs
import com.gym.agenda.state.AddEditAppointmentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: String? = savedStateHandle.get<String>(NavArgs.APPOINTMENT_ID)
        .takeIf { it != "new" }

    private val _uiState = MutableStateFlow(AddEditAppointmentUiState())
    val uiState: StateFlow<AddEditAppointmentUiState> = _uiState.asStateFlow()

    init {
        if (appointmentId != null) {
            loadAppointment()
        }
    }

    private fun loadAppointment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            appointmentRepository.getAppointmentById(appointmentId!!)
                .onSuccess { appointment ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointment = appointment,
                        isEditMode = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar cita"
                    )
                }
        }
    }

    fun saveAppointment(appointment: GymAppointment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Obtenemos el usuario de forma segura desde el flow
            val currentUser = authRepository.authState.first()
            
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sesión no válida. Por favor, inicia sesión de nuevo."
                )
                return@launch
            }

            val appointmentToSave = if (appointmentId == null) {
                // Nueva cita - agregar userId y nombre del cliente si falta
                appointment.copy(
                    userId = currentUser.id,
                    clientName = if (appointment.clientName.isBlank()) currentUser.name else appointment.clientName
                )
            } else {
                appointment
            }

            val result = if (appointmentId == null) {
                appointmentRepository.createAppointment(appointmentToSave)
            } else {
                appointmentRepository.updateAppointment(appointmentToSave)
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al guardar"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}