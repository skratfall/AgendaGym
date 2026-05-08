package com.gym.agenda.di.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentFilters
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.NotificationEvent
import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.utils.NotificationMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar
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

    // Estado de filtros
    private val _filters = MutableStateFlow(AppointmentFilters())
    val filters: StateFlow<AppointmentFilters> = _filters.asStateFlow()

    val users: StateFlow<List<User>> = authRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allAppointmentsRaw: StateFlow<List<GymAppointment>> = merge(
        flowOf(Unit),
        _refreshTrigger
    ).flatMapLatest {
        appointmentRepository.getAllAppointments()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Citas filtradas basadas en los filtros activos
    @OptIn(ExperimentalCoroutinesApi::class)
    val allAppointments: StateFlow<List<GymAppointment>> = combine(
        allAppointmentsRaw,
        _filters
    ) { appointments, filters ->
        applyFilters(appointments, filters)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AdminUiState> = allAppointments.transformLatest { appointments ->
        emit(AdminUiState(isLoading = true)) // Mostrar shimmer inmediatamente
        delay(2000) // Esperar 2 segundos para que se vea bien

        if (appointments.isEmpty()) {
            emit(AdminUiState())
        } else {
            val totalRevenue = appointments.sumOf { it.price }
            val totalCount = appointments.size
            val mostPopular = appointments.groupBy { it.service }
                .maxByOrNull { it.value.size }?.key ?: "N/A"
            
            emit(AdminUiState(
                totalRevenue = totalRevenue,
                totalAppointments = totalCount,
                popularService = mostPopular
            ))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminUiState(isLoading = true))

    /**
     * Aplica los filtros a la lista de citas
     */
    private fun applyFilters(appointments: List<GymAppointment>, filters: AppointmentFilters): List<GymAppointment> {
        return appointments.filter { appointment ->
            // Filtro por usuario
            if (filters.selectedUserId != null && appointment.userId != filters.selectedUserId) {
                return@filter false
            }

            // Filtro por servicio
            if (filters.selectedService != null && appointment.service != filters.selectedService) {
                return@filter false
            }

            // Filtro por fecha de inicio
            if (filters.startDate != null) {
                val appointmentStartOfDay = getStartOfDay(appointment.dateMillis)
                val filterStartOfDay = getStartOfDay(filters.startDate)
                if (appointmentStartOfDay < filterStartOfDay) {
                    return@filter false
                }
            }

            // Filtro por fecha de fin
            if (filters.endDate != null) {
                val appointmentEndOfDay = getEndOfDay(appointment.dateMillis)
                val filterEndOfDay = getEndOfDay(filters.endDate)
                if (appointmentEndOfDay > filterEndOfDay) {
                    return@filter false
                }
            }

            // Filtro por estado
            if (filters.selectedStatus != null && appointment.status != filters.selectedStatus) {
                return@filter false
            }

            true
        }
    }

    /**
     * Obtiene el inicio del día en milisegundos
     */
    private fun getStartOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Obtiene el final del día en milisegundos
     */
    private fun getEndOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Actualiza los filtros
     */
    fun updateFilters(newFilters: AppointmentFilters) {
        _filters.value = newFilters
    }

    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        _filters.value = AppointmentFilters()
    }

    /**
     * Actualiza un filtro específico
     */
    fun setUserFilter(userId: String?) {
        _filters.value = _filters.value.copy(selectedUserId = userId)
    }

    fun setServiceFilter(service: String?) {
        _filters.value = _filters.value.copy(selectedService = service)
    }

    fun setDateRangeFilter(startDate: Long?, endDate: Long?) {
        _filters.value = _filters.value.copy(startDate = startDate, endDate = endDate)
    }

    fun setStatusFilter(status: AppointmentStatus?) {
        _filters.value = _filters.value.copy(selectedStatus = status)
    }

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            authRepository.updateUserRole(userId, role)
            _notification.value = NotificationEvent.Success(NotificationMessages.ADMIN_USER_ROLE_UPDATED)
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(appointmentId, status)
            val msg = NotificationMessages.STATUS_CONFIRMED.replace("%s", status.name)
            _notification.value = NotificationEvent.Success(msg)
        }
    }

    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointmentId)
            _notification.value = NotificationEvent.Success(NotificationMessages.APPOINTMENT_DELETED)
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
            _notification.value = NotificationEvent.Success(NotificationMessages.ADMIN_USER_DELETED)
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }
}