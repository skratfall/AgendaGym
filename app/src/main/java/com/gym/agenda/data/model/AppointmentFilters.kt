package com.gym.agenda.data.model

/**
 * Modelo de datos que representa los filtros aplicados a las citas
 */
data class AppointmentFilters(
    val selectedUserId: String? = null,           // ID del usuario creador de la cita
    val selectedService: String? = null,          // Servicio seleccionado
    val startDate: Long? = null,                  // Fecha de inicio (en milisegundos)
    val endDate: Long? = null,                    // Fecha de fin (en milisegundos)
    val selectedStatus: AppointmentStatus? = null // Estado de la cita
) {
    /**
     * Verifica si hay algún filtro activo
     */
    fun hasActiveFilters(): Boolean {
        return selectedUserId != null ||
               selectedService != null ||
               startDate != null ||
               endDate != null ||
               selectedStatus != null
    }

    /**
     * Limpia todos los filtros
     */
    fun clear(): AppointmentFilters {
        return copy(
            selectedUserId = null,
            selectedService = null,
            startDate = null,
            endDate = null,
            selectedStatus = null
        )
    }
}

