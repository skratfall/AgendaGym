package com.gym.agenda.utils

import com.gym.agenda.data.model.GymAppointment
import timber.log.Timber
import java.util.regex.Pattern

/**
 * Validador centralizado para citas del gimnasio
 * Encapsula toda la lógica de validación en un solo lugar
 */
object AppointmentValidator {

    // 🔹 Constantes de validación
    private const val MIN_ADVANCE_HOURS = 1  // Mínimo 1 hora de anticipación
    private const val DEFAULT_DURATION_MINUTES = 60
    private const val MAX_DURATION_MINUTES = 480  // 8 horas máx

    // 🔹 Regex para email
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    )

    /**
     * Valida una cita completa
     * @return ValidationResult con el resultado detallado
     */
    fun validateAppointment(
        appointment: GymAppointment,
        checkTimeSlot: Boolean = false,
        existingAppointments: List<GymAppointment> = emptyList()
    ): ValidationResult {
        Timber.d("🔍 Validando cita: ${appointment.service}")

        // Ejecutar todas las validaciones
        val validations = listOf(
            validateClientName(appointment.clientName),
            validateClientEmail(appointment.clientEmail),
            validateService(appointment.service),
            validatePrice(appointment.price),
            validateHourRange(appointment.timeHour),
            validateMinuteRange(appointment.timeMinute),
            validateDateNotInPast(appointment.dateMillis, appointment.timeHour, appointment.timeMinute),
            validateAdvanceNotification(appointment.dateMillis, appointment.timeHour, appointment.timeMinute)
        )

        // 🔹 Si checkTimeSlot es true, agregar validación de solapamiento
        if (checkTimeSlot) {
            validations.plus(
                validateTimeSlotAvailable(appointment, existingAppointments)
            )
        }

        // 🔹 Encontrar el primer error crítico
        val firstError = validations.firstOrNull {
            it is ValidationResult.Invalid && it.severity == ValidationResult.Severity.ERROR
        }

        return if (firstError != null) {
            Timber.w("❌ Validación fallida: $firstError")
            firstError
        } else {
            Timber.i("✅ Cita validada correctamente")
            ValidationResult.Valid
        }
    }

    /**
     * Valida solo los campos básicos (rápido)
     */
    fun validateBasicFields(
        clientName: String,
        clientEmail: String,
        service: String,
        price: Double
    ): ValidationResult {
        Timber.d("🔍 Validando campos básicos...")

        return validateClientName(clientName)
            .takeIf { it.isInvalid() } ?: validateClientEmail(clientEmail)
            .takeIf { it.isInvalid() } ?: validateService(service)
            .takeIf { it.isInvalid() } ?: validatePrice(price)
            .takeIf { it.isInvalid() } ?: ValidationResult.Valid
    }

    /**
     * Valida solo los horarios (rápido)
     */
    fun validateTimeFields(
        dateMillis: Long,
        timeHour: Int,
        timeMinute: Int
    ): ValidationResult {
        Timber.d("🔍 Validando horario...")

        return validateHourRange(timeHour)
            .takeIf { it.isInvalid() } ?: validateMinuteRange(timeMinute)
            .takeIf { it.isInvalid() } ?: validateDateNotInPast(dateMillis, timeHour, timeMinute)
            .takeIf { it.isInvalid() } ?: validateAdvanceNotification(dateMillis, timeHour, timeMinute)
            .takeIf { it.isInvalid() } ?: ValidationResult.Valid
    }

    // ============ VALIDACIONES INDIVIDUALES ============

    private fun validateClientName(name: String): ValidationResult {
        Timber.d("🔍 Validando nombre del cliente...")
        return when {
            name.isBlank() -> {
                Timber.w("❌ Nombre vacío")
                ValidationResult.Invalid(
                    message = "El nombre del cliente es requerido",
                    fieldName = "clientName"
                )
            }
            name.length < 3 -> {
                Timber.w("❌ Nombre muy corto")
                ValidationResult.Invalid(
                    message = "El nombre debe tener al menos 3 caracteres",
                    fieldName = "clientName"
                )
            }
            name.length > 100 -> {
                Timber.w("❌ Nombre muy largo")
                ValidationResult.Invalid(
                    message = "El nombre no puede exceder 100 caracteres",
                    fieldName = "clientName"
                )
            }
            else -> {
                Timber.d("✅ Nombre válido: $name")
                ValidationResult.Valid
            }
        }
    }

    private fun validateClientEmail(email: String): ValidationResult {
        Timber.d("🔍 Validando email del cliente...")
        return when {
            email.isBlank() -> {
                Timber.d("ℹ️ Email opcional vacío")
                ValidationResult.Valid
            }
            !EMAIL_PATTERN.matcher(email).matches() -> {
                Timber.w("❌ Email inválido: $email")
                ValidationResult.Invalid(
                    message = "El formato del email no es válido",
                    fieldName = "clientEmail"
                )
            }
            email.length > 200 -> {
                Timber.w("❌ Email muy largo")
                ValidationResult.Invalid(
                    message = "El email no puede exceder 200 caracteres",
                    fieldName = "clientEmail"
                )
            }
            else -> {
                Timber.d("✅ Email válido: $email")
                ValidationResult.Valid
            }
        }
    }

    private fun validateService(service: String): ValidationResult {
        Timber.d("🔍 Validando servicio...")
        return when {
            service.isBlank() -> {
                Timber.w("❌ Servicio vacío")
                ValidationResult.Invalid(
                    message = "El servicio es requerido",
                    fieldName = "service"
                )
            }
            service.length < 3 -> {
                Timber.w("❌ Servicio muy corto")
                ValidationResult.Invalid(
                    message = "El servicio debe tener al menos 3 caracteres",
                    fieldName = "service"
                )
            }
            service.length > 100 -> {
                Timber.w("❌ Servicio muy largo")
                ValidationResult.Invalid(
                    message = "El servicio no puede exceder 100 caracteres",
                    fieldName = "service"
                )
            }
            else -> {
                Timber.d("✅ Servicio válido: $service")
                ValidationResult.Valid
            }
        }
    }

    private fun validatePrice(price: Double): ValidationResult {
        Timber.d("🔍 Validando precio: $$price")
        return when {
            price < 0 -> {
                Timber.w("❌ Precio negativo")
                ValidationResult.Invalid(
                    message = "El precio no puede ser negativo",
                    fieldName = "price"
                )
            }
            price > 100000 -> {
                Timber.w("❌ Precio demasiado alto: $$price")
                ValidationResult.Invalid(
                    message = "El precio parece ser demasiado alto",
                    fieldName = "price",
                    severity = ValidationResult.Severity.WARNING
                )
            }
            else -> {
                Timber.d("✅ Precio válido: $$price")
                ValidationResult.Valid
            }
        }
    }

    private fun validateHourRange(hour: Int): ValidationResult {
        Timber.d("🔍 Validando hora: $hour")
        return when {
            hour < 0 || hour > 23 -> {
                Timber.w("❌ Hora fuera de rango: $hour")
                ValidationResult.Invalid(
                    message = "La hora debe estar entre 0 y 23",
                    fieldName = "timeHour"
                )
            }
            else -> {
                Timber.d("✅ Hora válida: $hour")
                ValidationResult.Valid
            }
        }
    }

    private fun validateMinuteRange(minute: Int): ValidationResult {
        Timber.d("🔍 Validando minuto: $minute")
        return when {
            minute < 0 || minute > 59 -> {
                Timber.w("❌ Minuto fuera de rango: $minute")
                ValidationResult.Invalid(
                    message = "Los minutos deben estar entre 0 y 59",
                    fieldName = "timeMinute"
                )
            }
            else -> {
                Timber.d("✅ Minuto válido: $minute")
                ValidationResult.Valid
            }
        }
    }

    private fun validateDateNotInPast(
        dateMillis: Long,
        hour: Int,
        minute: Int
    ): ValidationResult {
        Timber.d("🔍 Validando que fecha no esté en el pasado...")
        val appointmentTimeMillis = dateMillis + (hour * 3600000L) + (minute * 60000L)
        val now = System.currentTimeMillis()

        return if (appointmentTimeMillis < now) {
            Timber.w("❌ Fecha en el pasado")
            ValidationResult.Invalid(
                message = "No puedes agendar citas en el pasado",
                fieldName = "timestamp"
            )
        } else {
            Timber.d("✅ Fecha válida (futura)")
            ValidationResult.Valid
        }
    }

    private fun validateAdvanceNotification(
        dateMillis: Long,
        hour: Int,
        minute: Int
    ): ValidationResult {
        Timber.d("🔍 Validando anticipación mínima...")
        val appointmentTimeMillis = dateMillis + (hour * 3600000L) + (minute * 60000L)
        val now = System.currentTimeMillis()
        val minAdvanceMillis = MIN_ADVANCE_HOURS * 3600000L
        val timeUntilAppointment = appointmentTimeMillis - now

        return if (timeUntilAppointment < minAdvanceMillis) {
            Timber.w("❌ Anticipación insuficiente: ${timeUntilAppointment / 60000} minutos")
            ValidationResult.Invalid(
                message = "Las citas deben agendarse con al menos $MIN_ADVANCE_HOURS hora(s) de anticipación",
                fieldName = "timestamp"
            )
        } else {
            Timber.d("✅ Anticipación válida")
            ValidationResult.Valid
        }
    }

    private fun validateTimeSlotAvailable(
        appointment: GymAppointment,
        existingAppointments: List<GymAppointment>
    ): ValidationResult {
        Timber.d("🔍 Validando disponibilidad del horario...")

        val appointmentStart = appointment.dateTimeMillis
        val appointmentEnd = appointmentStart + (DEFAULT_DURATION_MINUTES * 60000L)

        val hasConflict = existingAppointments.any { existing ->
            // Ignorar la misma cita si es edición
            if (existing.id == appointment.id) return@any false

            // Comparar horarios
            val existingStart = existing.dateTimeMillis
            val existingEnd = existingStart + (DEFAULT_DURATION_MINUTES * 60000L)

            // Verificar solapamiento
            appointmentStart < existingEnd && appointmentEnd > existingStart
        }

        return if (hasConflict) {
            Timber.w("❌ Horario ya reservado")
            ValidationResult.Invalid(
                message = "Este horario ya está reservado. Por favor, elige otro tiempo",
                fieldName = "timestamp",
                severity = ValidationResult.Severity.ERROR
            )
        } else {
            Timber.d("✅ Horario disponible")
            ValidationResult.Valid
        }
    }
}
