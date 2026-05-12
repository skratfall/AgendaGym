package com.gym.agenda.utils

object NotificationMessages {
    // ========== AUTH MESSAGES ==========
    const val LOGIN_SUCCESS = " Sesión iniciada correctamente"
    const val LOGIN_ERROR = "❌ Email o contraseña incorrectos"
    const val REGISTER_SUCCESS = " Cuenta creada. ¡Bienvenido!"
    const val REGISTER_ERROR = "❌ El email ya está registrado"
    const val LOGOUT_SUCCESS = " Sesión cerrada correctamente"
    const val SESSION_EXPIRED = "⚠️ Tu sesión ha expirado. Por favor, inicia sesión de nuevo"

    // ========== APPOINTMENT MESSAGES ==========
    const val APPOINTMENT_CREATED = " Cita agendada exitosamente"
    const val APPOINTMENT_UPDATED = " Cita actualizada correctamente"
    const val APPOINTMENT_DELETED = " Cita eliminada correctamente"
    const val APPOINTMENT_ERROR = "❌ Error al procesar la cita. Intenta de nuevo"
    const val APPOINTMENT_TIME_CONFLICT = "❌ Horario no disponible. Elige otro"
    const val APPOINTMENT_FUTURE_DATE = "❌ La cita debe ser en una fecha futura"
    const val APPOINTMENT_SAVED_DRAFT = "💾 Cambios guardados"

    // ========== APPOINTMENT STATUS MESSAGES ==========
    const val STATUS_CONFIRMED = " Cita confirmada"
    const val STATUS_PENDING = "⏳ Cita pendiente"
    const val STATUS_CANCELLED = "❌ Cita cancelada"
    const val STATUS_REJECTED = "🚫 Cita rechazada"
    const val STATUS_COMPLETED = " Cita completada"

    // ========== ADMIN MESSAGES ==========
    const val ADMIN_NEW_APPOINTMENT = "🔔 Nueva solicitud de cita de %s"
    const val ADMIN_STATUS_CHANGED = "✅ Estado de la cita cambió a %s"
    const val ADMIN_USER_DELETED = " Usuario eliminado correctamente"
    const val ADMIN_USER_ROLE_UPDATED = "✅ Rol del usuario actualizado"
    const val ADMIN_ACTION_ERROR = "❌ Error en la acción. Intenta de nuevo"

    // ========== VALIDATION MESSAGES ==========
    const val FIELD_REQUIRED = "Este campo es requerido"
    const val INVALID_EMAIL = "Email inválido"
    const val INVALID_PRICE = "El precio debe ser mayor a 0"
    const val WEAK_PASSWORD = "La contraseña debe tener al menos 6 caracteres"

    // ========== NETWORK MESSAGES ==========
    const val NETWORK_ERROR = "❌ Error de conexión. Verifica tu internet"
    const val SYNC_SUCCESS = "✅ Datos sincronizados correctamente"
    const val SYNC_PENDING = "⏳ Sincronizando cambios..."
}
