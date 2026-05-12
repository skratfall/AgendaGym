package com.gym.agenda.data.model



enum class AppointmentStatus(val displayName: String) {
    PENDING("Pendiente"),        // Solicitada, esperando confirmación
    CONFIRMED("Confirmada"),      // Confirmada por admin
    CANCELLED("Cancelada"),      // Cancelada por usuario o admin
    COMPLETED("Completada"),      // Servicio completado
    NO_SHOW("No asistió")         // Usuario no asistió
}
