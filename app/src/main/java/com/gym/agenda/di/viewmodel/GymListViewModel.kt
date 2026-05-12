package com.gym.agenda.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.state.AppointmentListUiState
import com.gym.agenda.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GymListViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentListUiState(isLoading = true))
    val uiState: StateFlow<AppointmentListUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<GymAppointment>>(emptyList())
    val appointments: StateFlow<List<GymAppointment>> = _appointments.asStateFlow()

    // 🔄 SharedFlow para disparar refresco explícito de citas
    private val _refreshTrigger = MutableSharedFlow<Unit>()
    
    private var previousAppointments: List<GymAppointment> = emptyList()

    init {
        Timber.d("📋 GymListViewModel inicializado")
        observeAppointments()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAppointments() {
        Timber.d("🔄 Iniciando observación de citas...")
        viewModelScope.launch {
            authRepository.authState
                .filterNotNull()
                .flatMapLatest { user ->
                    Timber.d("👤 Usuario autenticado: ${user.id}")
                    merge(
                        flowOf(Unit),
                        _refreshTrigger
                    ).flatMapLatest {
                        Timber.d("📥 Consultando citas de Firestore...")
                        appointmentRepository.getAppointments(user.id)
                    }
                }
                .catch { e ->
                    Timber.e(e, "❌ Error al cargar citas")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Error al cargar citas"
                    )
                }
                .collect { list ->
                    Timber.i("✅ Citas cargadas: ${list.size} citas encontradas")
                    
                    // Detectar cambios de estado para notificar al usuario localmente
                    if (previousAppointments.isNotEmpty()) {
                        checkForStatusChanges(list)
                    } else {
                        previousAppointments = list
                    }
                    
                    _appointments.value = list
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = list
                    )
                }
        }
    }

    private fun checkForStatusChanges(newList: List<GymAppointment>) {
        newList.forEach { newApp ->
            val oldApp = previousAppointments.find { it.id == newApp.id }
            
            // Si la cita existía y su estado cambió
            if (oldApp != null && oldApp.status != newApp.status) {
                // Solo notificar si pasa a CONFIRMED o CANCELLED
                if (newApp.status == AppointmentStatus.CONFIRMED || newApp.status == AppointmentStatus.CANCELLED) {
                    Timber.i("🔔 Cambio de estado detectado: ${newApp.id} -> ${newApp.status}")
                    triggerLocalNotification(newApp)
                }
            }
        }
        previousAppointments = newList
    }

    private fun triggerLocalNotification(appointment: GymAppointment) {
        val title = when (appointment.status) {
            AppointmentStatus.CONFIRMED -> "✅ ¡Cita Confirmada!"
            AppointmentStatus.CANCELLED -> "❌ Cita Cancelada"
            else -> "📅 Actualización de Cita"
        }
        val message = "Tu sesión de ${appointment.service} ha cambiado a: ${appointment.status.displayName}"
        notificationScheduler.showImmediateNotification(title, message)
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

    fun refreshAppointments() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}
