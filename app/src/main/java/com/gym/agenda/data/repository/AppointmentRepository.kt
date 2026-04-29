package com.gym.agenda.data.repository

import com.gym.agenda.data.firebase.FirestoreService
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {

    // 🔹 Obtener citas del usuario (en tiempo real)
    fun getAppointments(userId: String): Flow<List<GymAppointment>> {
        return firestoreService.getAppointments(userId)
            .map { appointments ->
                appointments.filter { it.isActive() }
            }
    }

    // 🔹 Obtener citas con filtro de estado
    fun getAppointmentsByStatus(userId: String, status: AppointmentStatus): Flow<List<GymAppointment>> {
        return firestoreService.getAppointments(userId)
            .map { appointments ->
                appointments.filter { it.status == status && it.isActive() }
            }
    }

    // 🔹 Obtener citas futuras del usuario
    fun getUpcomingAppointments(userId: String): Flow<List<GymAppointment>> {
        return firestoreService.getAppointments(userId)
            .map { appointments ->
                appointments.filter { it.isUpcoming && it.isActive() }
            }
    }

    // 🔹 Obtener historial de citas (pasadas)
    fun getAppointmentHistory(userId: String): Flow<List<GymAppointment>> {
        return firestoreService.getAppointments(userId)
            .map { appointments ->
                appointments.filter { it.isPast && it.isActive() }
            }
    }

    // 🔹 Obtener TODAS las citas (ADMIN - en tiempo real)
    fun getAllAppointments(): Flow<List<GymAppointment>> {
        return firestoreService.getAllAppointments()
            .map { appointments ->
                appointments.filter { it.isActive() }
            }
    }

    // 🔹 Obtener citas por fecha específica
    suspend fun getAppointmentsByDate(dateMillis: Long): List<GymAppointment> {
        return firestoreService.getAppointmentsByDate(dateMillis)
            .filter { it.isActive() }
    }

    // 🔹 Obtener citas por rango de fechas (ADMIN)
    suspend fun getAppointmentsByDateRange(startMillis: Long, endMillis: Long): List<GymAppointment> {
        return firestoreService.getAppointmentsByDateRange(startMillis, endMillis)
            .filter { it.isActive() }
    }

    // 🔹 Crear nueva cita
    suspend fun createAppointment(appointment: GymAppointment): Result<String> {
        // Validaciones básicas
        if (appointment.userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        if (appointment.service.isBlank()) {
            return Result.failure(Exception("Service is required"))
        }
        if (appointment.dateMillis <= 0) {
            return Result.failure(Exception("Valid date is required"))
        }

        return firestoreService.createAppointment(appointment)
    }

    // 🔹 Actualizar cita completa
    suspend fun updateAppointment(appointment: GymAppointment): Result<Unit> {
        if (appointment.id.isBlank()) {
            return Result.failure(Exception("Appointment ID is required"))
        }

        return firestoreService.updateAppointment(
            appointment.id,
            appointment.copy(updatedAt = System.currentTimeMillis()).toMap()
        )
    }

    // 🔹 Actualizar campos específicos
    suspend fun updateAppointmentFields(appointmentId: String, updates: Map<String, Any>): Result<Unit> {
        if (appointmentId.isBlank()) {
            return Result.failure(Exception("Appointment ID is required"))
        }

        return firestoreService.updateAppointment(
            appointmentId,
            updates + ("updatedAt" to System.currentTimeMillis())
        )
    }

    // 🔹 Cambiar estado de cita
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return updateAppointmentFields(appointmentId, mapOf("status" to status.name))
    }

    // 🔹 Actualizar notas del admin
    suspend fun updateAdminNotes(appointmentId: String, notes: String): Result<Unit> {
        return updateAppointmentFields(appointmentId, mapOf("adminNotes" to notes))
    }

    // 🔹 Actualizar estado de pago
    suspend fun updatePaymentStatus(appointmentId: String, status: PaymentStatus): Result<Unit> {
        return updateAppointmentFields(appointmentId, mapOf("paymentStatus" to status.name))
    }

    // 🔹 Cancelar cita (soft delete)
    suspend fun cancelAppointment(appointmentId: String, reason: String = ""): Result<Unit> {
        return updateAppointmentFields(
            appointmentId,
            mapOf(
                "status" to AppointmentStatus.CANCELLED.name,
                "adminNotes" to reason,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    // 🔹 Eliminar cita permanentemente (solo admin)
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return firestoreService.deleteAppointment(appointmentId)
    }

    // 🔹 Obtener cita por ID
    suspend fun getAppointmentById(appointmentId: String): Result<GymAppointment> {
        return firestoreService.getAppointmentById(appointmentId)
    }

    // 🔹 Verificar disponibilidad de horario
    suspend fun isTimeSlotAvailable(
        dateMillis: Long,
        timeHour: Int,
        timeMinute: Int,
        duration: Int,
        excludeAppointmentId: String? = null
    ): Boolean {
        val appointments = firestoreService.getAppointmentsByDate(dateMillis)

        val requestedStart = dateMillis + (timeHour * 3600000L) + (timeMinute * 60000L)
        val requestedEnd = requestedStart + (duration * 60000L)

        return appointments.none { appointment ->
            if (excludeAppointmentId != null && appointment.id == excludeAppointmentId) {
                return@none false
            }
            if (appointment.status == AppointmentStatus.CANCELLED) {
                return@none false
            }

            val existingStart = appointment.dateTimeMillis
            val existingEnd = appointment.endTimeMillis

            // Verificar solapamiento
            requestedStart < existingEnd && requestedEnd > existingStart
        }
    }
}

// 🔹 Extensión para verificar si la cita está "activa" (no eliminada)
private fun GymAppointment.isActive(): Boolean {
    return status != AppointmentStatus.CANCELLED
}