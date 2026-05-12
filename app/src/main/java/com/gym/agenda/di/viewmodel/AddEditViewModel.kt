package com.gym.agenda.di.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.NotificationEvent
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.di.navigation.NavArgs
import com.gym.agenda.state.AddEditAppointmentUiState
import com.gym.agenda.utils.AppointmentValidator
import com.gym.agenda.utils.NotificationMessages
import com.gym.agenda.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: String? = savedStateHandle.get<String>(NavArgs.APPOINTMENT_ID)
        .takeIf { it != "new" }

    private val _uiState = MutableStateFlow(AddEditAppointmentUiState())
    val uiState: StateFlow<AddEditAppointmentUiState> = _uiState.asStateFlow()

    private val _notification = MutableStateFlow<NotificationEvent?>(null)
    val notification: StateFlow<NotificationEvent?> = _notification.asStateFlow()

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
                    _notification.value = NotificationEvent.Error(error.message ?: NotificationMessages.APPOINTMENT_ERROR)
                }
        }
    }

    fun saveAppointment(appointment: GymAppointment) {
        Timber.d("💾 Iniciando guardado de cita...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 🔍 VALIDACIÓN CON EL NUEVO SISTEMA
            val validationResult = AppointmentValidator.validateAppointment(
                appointment = appointment,
                checkTimeSlot = true,
                existingAppointments = emptyList()  // TODO: Obtener citas conflictivas de Firestore
            )

            if (validationResult.isInvalid()) {
                Timber.e("❌ Validación fallida: ${validationResult.getErrorMessage()}")
                val errorMsg = validationResult.getErrorMessage() ?: NotificationMessages.APPOINTMENT_ERROR
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                _notification.value = NotificationEvent.Error(errorMsg)
                return@launch
            }

            // ✅ VALIDACIÓN EXITOSA - Continuar con guardado
            Timber.i("✅ Validación exitosa - procediendo a guardar...")

            // Obtenemos el usuario de forma segura desde el flow
            val currentUser = authRepository.authState.first()
            
            if (currentUser == null) {
                Timber.e("❌ Usuario no autenticado")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sesión no válida. Por favor, inicia sesión de nuevo."
                )
                _notification.value = NotificationEvent.Error(NotificationMessages.SESSION_EXPIRED)
                return@launch
            }

            val appointmentToSave = if (appointmentId == null) {
                // Nueva cita - agregar userId y nombre del cliente si falta
                Timber.d("➕ Creando nueva cita para usuario: ${currentUser.id}")
                appointment.copy(
                    userId = currentUser.id,
                    clientName = if (appointment.clientName.isBlank()) currentUser.name else appointment.clientName
                )
            } else {
                // Editar cita - mantener el userId original de la cita que estamos editando
                Timber.d("✏️ Actualizando cita: $appointmentId")
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
                    Timber.i("✅ Cita guardada: $finalId")
                    
                    val finalAppointment = appointmentToSave.copy(id = finalId)
                    if (finalAppointment.status == AppointmentStatus.CONFIRMED) {
                        notificationScheduler.scheduleAppointmentNotification(finalAppointment)
                    } else {
                        Timber.d("ℹ️ No se programa notificación: estado es ${finalAppointment.status}")
                        notificationScheduler.cancelAppointmentNotification(finalId)
                    }
                    
                    val successMsg = if (appointmentId == null) {
                        NotificationMessages.APPOINTMENT_CREATED
                    } else {
                        NotificationMessages.APPOINTMENT_UPDATED
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                    _notification.value = NotificationEvent.Success(successMsg)
                }
                .onFailure { error ->
                    Timber.e(error, "❌ Error al guardar cita")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al guardar"
                    )
                    _notification.value = NotificationEvent.Error(error.message ?: NotificationMessages.APPOINTMENT_ERROR)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun dismissNotification() {
        _notification.value = null
    }

    /**
     * Valida los campos básicos en tiempo real (util para desactivar botones)
     * @return true si los campos son válidos
     */
    fun validateBasicFields(
        clientName: String,
        clientEmail: String,
        service: String,
        price: Double
    ): Boolean {
        val result = AppointmentValidator.validateBasicFields(
            clientName = clientName,
            clientEmail = clientEmail,
            service = service,
            price = price
        )
        Timber.d("⚡ Validación en tiempo real: ${if (result.isValid()) "✅ Válido" else "❌ Inválido"}")
        return result.isValid()
    }

    /**
     * Valida los campos de horario en tiempo real
     * @return true si el horario es válido
     */
    fun validateTimeFields(
        dateMillis: Long,
        timeHour: Int,
        timeMinute: Int
    ): Boolean {
        val result = AppointmentValidator.validateTimeFields(
            dateMillis = dateMillis,
            timeHour = timeHour,
            timeMinute = timeMinute
        )
        Timber.d("⚡ Validación de hora en tiempo real: ${if (result.isValid()) "✅ Válida" else "❌ Inválida"}")
        return result.isValid()
    }
}