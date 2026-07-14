package com.gym.agenda.utils

import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminNotificationObserver @Inject constructor(
    private val authRepository: AuthRepository,
    private val appointmentRepository: AppointmentRepository,
    private val notificationScheduler: NotificationScheduler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastProcessedCount = -1
    private var isObserving = false

    fun startObserving() {
        if (isObserving) return
        isObserving = true
        
        scope.launch {
            // Solo observar si el usuario es ADMIN
            authRepository.authState.collectLatest { user ->
                if (user?.role == UserRole.ADMIN) {
                    Timber.d("🕵️ Admin detectado. Iniciando vigilancia de citas...")
                    observeAppointments()
                } else {
                    Timber.d("👤 Usuario normal detectado. Vigilancia desactivada.")
                    lastProcessedCount = -1
                }
            }
        }
    }

    private suspend fun observeAppointments() {
        appointmentRepository.getAllAppointments().collect { appointments ->
            if (lastProcessedCount == -1) {
                lastProcessedCount = appointments.size
                return@collect
            }

            if (appointments.size > lastProcessedCount) {
                val now = System.currentTimeMillis()
                val newPending = appointments.filter { 
                    it.status == AppointmentStatus.PENDING && 
                    (now - it.createdAt) < 60000 
                }

                newPending.forEach { appointment ->
                    notificationScheduler.showImmediateNotification(
                        title = "🔔 NUEVA CITA: ${appointment.service}",
                        message = "El cliente ${appointment.clientName} ha agendado una cita."
                    )
                    Timber.i("📢 Alerta de Admin disparada para: ${appointment.clientName}")
                }
            }
            lastProcessedCount = appointments.size
        }
    }
}
