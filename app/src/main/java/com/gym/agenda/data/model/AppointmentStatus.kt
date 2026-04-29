package com.gym.agenda.data.model



enum class AppointmentStatus {
    PENDING,        // Solicitada, esperando confirmación
    CONFIRMED,      // Confirmada por admin
    CANCELLED,      // Cancelada por usuario o admin
    COMPLETED,      // Servicio completado
    NO_SHOW         // Usuario no asistió
}