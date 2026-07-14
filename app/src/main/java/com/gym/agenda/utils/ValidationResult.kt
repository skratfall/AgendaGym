package com.gym.agenda.utils

/**
 * Resultado tipado para validaciones
 * Permite distinguir entre validaciones exitosas y fallidas con información detallada
 */
sealed class ValidationResult {
    object Valid : ValidationResult() {
        override fun toString() = "✅ Validación exitosa"
    }

    data class Invalid(
        val message: String,
        val fieldName: String? = null,
        val severity: Severity = Severity.ERROR
    ) : ValidationResult() {
        override fun toString() = message
    }

    enum class Severity {
        WARNING,  // ⚠️ Advertencia (puede continuar)
        ERROR     // ❌ Error (debe detener operación)
    }

    // 🔧 Helpers
    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = !isValid()

    fun getErrorMessage(): String? = (this as? Invalid)?.message
}

/**
 * Para encadenar múltiples validaciones
 */
data class ValidationErrors(
    val errors: List<ValidationResult.Invalid> = emptyList()
) {
    fun add(error: ValidationResult.Invalid) = copy(errors = errors + error)

    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun isCritical(): Boolean = errors.any {
        it.severity == ValidationResult.Severity.ERROR
    }

    fun getErrorMessage(): String = if (errors.isEmpty()) {
        "Sin errores"
    } else {
        errors.joinToString("\n") { "• ${it.message}" }
    }
}
