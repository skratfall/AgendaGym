package com.gym.agenda.di.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.di.navigation.NavArgs
import com.gym.agenda.state.AddEditAppointmentUiState
import com.gym.agenda.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
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

            // 1. Validación de anticipación (mínimo 1 hora antes)
            val now = System.currentTimeMillis()
            val appointmentTime = appointment.dateTimeMillis
            val oneHourInMillis = 3600000L
            
            if (appointmentTime < (now + oneHourInMillis)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Las citas deben programarse con al menos 1 hora de anticipación."
                )
                return@launch
            }

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
                // Editar cita - mantener el userId original de la cita que estamos editando
                appointment.copy(
                    userId = _uiState.value.appointment?.userId ?: currentUser.id
                )
            }

            val result = if (appointmentId == null) {
                appointmentRepository.createAppointment(appointmentToSave)
            } else {
                appointmentRepository.updateAppointment(appointmentToSave)
            }

            result
                .onSuccess { id ->
                    val finalId = if (appointmentId == null) id.toString() else appointmentId
                    scheduleNotification(appointmentToSave.copy(id = finalId))
                    
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

    private fun scheduleNotification(appointment: GymAppointment) {
        val now = System.currentTimeMillis()
        val notificationTime = appointment.dateTimeMillis - (30 * 60000L) // 30 minutos antes
        val delay = notificationTime - now

        if (delay > 0) {
            val data = Data.Builder()
                .putString("title", "Recordatorio de Cita")
                .putString("message", "Tu sesión de ${appointment.service} comienza en 30 minutos.")
                .build()

            val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("notification_${appointment.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_${appointment.id}",
                androidx.work.ExistingWorkPolicy.REPLACE,
                notificationRequest
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}