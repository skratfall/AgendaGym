package com.gym.agenda.di.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.NotificationEvent
import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val _refreshTrigger = MutableSharedFlow<Unit>()
    
    private val _notification = MutableStateFlow<NotificationEvent?>(null)
    val notification: StateFlow<NotificationEvent?> = _notification.asStateFlow()

    val users: StateFlow<List<User>> = authRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var previousAppointments: List<GymAppointment> = emptyList()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allAppointments: StateFlow<List<GymAppointment>> = merge(
        flowOf(Unit),
        _refreshTrigger
    ).flatMapLatest {
        appointmentRepository.getAllAppointments()
    }
        .onEach { appointments ->
            // Detectar cambios en tiempo real
            if (previousAppointments.isNotEmpty()) {
                checkForChanges(appointments)
            } else {
                previousAppointments = appointments
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
    }.stateIn(viewModelScope, SharingStarted.Lazily, AdminUiState(isLoading = true))

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            authRepository.updateUserRole(userId, role)
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(appointmentId, status)
                .onSuccess {
                    _notification.value = NotificationEvent.Success("Estado actualizado: ${status.displayName}")
                }
                .onFailure { error ->
                    _notification.value = NotificationEvent.Error(error.message ?: "Error al actualizar")
                }
        }
    }

    private fun checkForChanges(newList: List<GymAppointment>) {
        // Detectar citas eliminadas
        previousAppointments.forEach { oldApp ->
            val still_exists = newList.find { it.id == oldApp.id }
            if (still_exists == null) {
                _notification.value = NotificationEvent.Info("Cita eliminada: ${oldApp.service}")
            }
        }
        
        // Detectar nuevas citas y cambios
        newList.forEach { newApp ->
            val oldApp = previousAppointments.find { it.id == newApp.id }
            
            if (oldApp == null) {
                // Nueva cita creada
                _notification.value = NotificationEvent.Success("✨ Nueva cita: ${newApp.service}")
            } else if (oldApp.status != newApp.status) {
                // Cambio de estado
                _notification.value = NotificationEvent.Info("Estado actualizado: ${newApp.service} - ${newApp.status.displayName}")
            } else if (oldApp != newApp) {
                // Otros cambios
                _notification.value = NotificationEvent.Info("Cambios en: ${newApp.service}")
            }
        }
        previousAppointments = newList
    }

    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointmentId)
                .onSuccess {
                    _notification.value = NotificationEvent.Success("Cita eliminada")
                }
                .onFailure { error ->
                    _notification.value = NotificationEvent.Error(error.message ?: "Error al eliminar")
                }
        }
    }

    fun refreshAppointments() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            authRepository.deleteUser(userId)
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }
}