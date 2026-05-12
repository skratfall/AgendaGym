package com.gym.agenda.data.firebase

import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.Service
import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreService(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection get() = firestore.collection("users")
    private val appointmentsCollection get() = firestore.collection("appointments")
    private val servicesCollection get() = firestore.collection("services")

    // ==================== APPOINTMENTS ====================

    // 🔹 Obtener citas de un usuario (en tiempo real)
    fun getAppointments(userId: String): Flow<List<GymAppointment>> {
        return flow {
            val listener = appointmentsCollection
                .whereEqualTo("userId", userId)
                .orderBy("dateMillis", Query.Direction.ASCENDING)
                .orderBy("timeHour", Query.Direction.ASCENDING)
                .orderBy("timeMinute", Query.Direction.ASCENDING)
                .snapshots()

            listener.collect { snapshot ->
                val appointments = snapshot.documents.mapNotNull { doc ->
                    try {
                        GymAppointment(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }
                emit(appointments)
            }
        }
    }

    // 🔹 Obtener todas las citas (ADMIN)
    fun getAllAppointments(): Flow<List<GymAppointment>> {
        return flow {
            // Simplificamos la consulta para evitar errores de índices de Firestore
            val listener = appointmentsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()

            listener.collect { snapshot ->
                val appointments = snapshot.documents.mapNotNull { doc ->
                    try {
                        GymAppointment(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) { 
                        Timber.e(e, "Error al parsear cita")
                        null 
                    }
                }
                emit(appointments)
            }
        }
    }

    // 🔹 Obtener citas por fecha específica
    suspend fun getAppointmentsByDate(dateMillis: Long): List<GymAppointment> {
        return try {
            val startOfDay = dateMillis
            val endOfDay = dateMillis + 86400000 // +24 horas en millis

            val result = appointmentsCollection
                .whereGreaterThanOrEqualTo("dateMillis", startOfDay)
                .whereLessThan("dateMillis", endOfDay)
                .orderBy("dateMillis")
                .orderBy("timeHour")
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                GymAppointment(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 Crear nueva cita
    suspend fun createAppointment(appointment: GymAppointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.add(appointment.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar cita
    suspend fun updateAppointment(appointmentId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar cita
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar estado de cita
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return updateAppointment(appointmentId, mapOf("status" to status.name))
    }

    // 🔹 Obtener cita por ID
    suspend fun getAppointmentById(appointmentId: String): Result<GymAppointment> {
        return try {
            val doc = appointmentsCollection.document(appointmentId).get().await()
            if (doc.exists()) {
                val appointment = GymAppointment(doc.id, doc.data ?: emptyMap())
                Result.success(appointment)
            } else {
                Result.failure(Exception("Cita no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== USERS ====================

    // 🔹 Obtener todos los usuarios (ADMIN)
    fun getAllUsers(): Flow<List<User>> {
        return flow {
            val listener = usersCollection.snapshots()

            listener.collect { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        User(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) { null }
                }
                emit(users)
            }
        }
    }

    // 🔹 Obtener usuario por ID
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                val user = User(doc.id, doc.data ?: emptyMap())
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar usuario
    suspend fun updateUser(userId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar usuario (ADMIN)
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Primero eliminar sus citas
            val appointments = appointmentsCollection.whereEqualTo("userId", userId).get().await()
            val batch = firestore.batch()

            appointments.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Luego eliminar el usuario
            batch.delete(usersCollection.document(userId))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Cambiar rol de usuario (ADMIN)
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return updateUser(userId, mapOf("role" to role.name))
    }

    // ==================== SERVICES ====================

    // 🔹 Obtener todos los servicios (en tiempo real)
    fun getAllServicesFlow(): Flow<List<Service>> {
        return servicesCollection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try {
                    Service(doc.id, doc.data ?: emptyMap())
                } catch (e: Exception) { null }
            }
        }
    }

    // 🔹 Obtener todos los servicios
    suspend fun getAllServices(): Result<List<Service>> {
        return try {
            val result = servicesCollection.get().await()
            val services = result.documents.mapNotNull { doc ->
                try {
                    Service(doc.id, doc.data ?: emptyMap())
                } catch (e: Exception) { null }
            }
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Crear servicio (ADMIN)
    suspend fun createService(service: Service): Result<String> {
        return try {
            val docRef = servicesCollection.add(service.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar servicio (ADMIN)
    suspend fun updateService(serviceId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            servicesCollection.document(serviceId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar servicio (ADMIN)
    suspend fun deleteService(serviceId: String): Result<Unit> {
        return try {
            servicesCollection.document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Buscar citas por rango de fechas
    suspend fun getAppointmentsByDateRange(
        startDateMillis: Long,
        endDateMillis: Long
    ): List<GymAppointment> {
        return try {
            val result = appointmentsCollection
                .whereGreaterThanOrEqualTo("dateMillis", startDateMillis)
                .whereLessThanOrEqualTo("dateMillis", endDateMillis)
                .orderBy("dateMillis")
                .orderBy("timeHour")
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                GymAppointment(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}