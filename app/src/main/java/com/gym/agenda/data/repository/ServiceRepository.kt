package com.gym.agenda.data.repository


import com.gym.agenda.data.firebase.FirestoreService
import com.gym.agenda.data.model.Service
import com.gym.agenda.data.model.ServiceCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {

    // 🔹 Obtener servicios activos
    fun getActiveServices(): Flow<List<Service>> {
        return firestoreService.getAllServicesFlow()
            .map { services ->
                services.filter { it.isActive }
            }
    }

    // 🔹 Obtener servicios por categoría
    fun getServicesByCategory(category: ServiceCategory): Flow<List<Service>> {
        return firestoreService.getAllServicesFlow()
            .map { services ->
                services.filter { it.category == category && it.isActive }
            }
    }

    // 🔹 Obtener todos los servicios (ADMIN)
    fun getAllServices(): Flow<List<Service>> {
        return firestoreService.getAllServicesFlow()
    }

    // 🔹 Crear nuevo servicio (ADMIN)
    suspend fun createService(service: Service): Result<String> {
        return firestoreService.createService(service)
    }

    // 🔹 Actualizar servicio (ADMIN)
    suspend fun updateService(service: Service): Result<Unit> {
        if (service.id.isBlank()) {
            return Result.failure(Exception("Service ID is required"))
        }
        return firestoreService.updateService(service.id, service.toMap())
    }

    // 🔹 Activar/desactivar servicio (ADMIN)
    suspend fun toggleServiceActive(serviceId: String, isActive: Boolean): Result<Unit> {
        return firestoreService.updateService(serviceId, mapOf("isActive" to isActive))
    }

    // 🔹 Eliminar servicio (ADMIN)
    suspend fun deleteService(serviceId: String): Result<Unit> {
        return firestoreService.deleteService(serviceId)
    }

    // 🔹 Obtener servicio por ID
    suspend fun getServiceById(serviceId: String): Result<Service> {
        val servicesResult = firestoreService.getAllServices()
        return servicesResult.mapCatching { services ->
            services.find { it.id == serviceId } ?: throw Exception("Service not found")
        }
    }
}