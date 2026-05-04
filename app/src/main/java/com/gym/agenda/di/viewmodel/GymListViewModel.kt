package com.gym.agenda.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.state.AppointmentListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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

    // 🔄 SharedFlow para disparar refresco explícito de citas
    private val _refreshTrigger = MutableSharedFlow<Unit>()

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
                    // 🔄 Combinar emisión inicial + triggers de refresco manual
                    merge(
                        flowOf(Unit),  // Emitir una vez al inicio
                        _refreshTrigger  // Emitir cuando se llame a refreshAppointments()
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
                    _appointments.value = list
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = list
                    )
                }
        }
    }

    fun deleteAppointment(appointment: GymAppointment) {
        Timber.d("🗑️ Eliminando cita: ${appointment.id}")
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointment.id)
                .onSuccess {
                    Timber.i("✅ Cita eliminada exitosamente: ${appointment.id}")
                }
                .onFailure { error ->
                    Timber.e(error, "❌ Error al eliminar cita: ${appointment.id}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al eliminar"
                    )
                }
        }
    }

    fun filterByStatus(status: AppointmentStatus?) {
        Timber.d("🔍 Filtrando por estado: $status")
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun clearError() {
        Timber.d("🧹 Limpiando mensaje de error")
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getFilteredAppointments(): List<GymAppointment> {
        val filtered = _uiState.value.filterStatus?.let { status ->
            _appointments.value.filter { it.status == status }
        } ?: _appointments.value
        Timber.d("📊 Citas filtradas: ${filtered.size} de ${_appointments.value.size}")
        return filtered
    }

    fun refreshAppointments() {
        Timber.d("🔄 Disparando refresco de citas...")
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}