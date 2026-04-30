package com.gym.agenda.data.model

// ✅ Sin anotaciones de Room - Modelo plano para Firestore
data class GymAppointment(
    val id: String = "",              // 🔹 ID de Firestore (auto-generado)
    val userId: String = "",          // 🔹 ID del usuario (para filtrar por cliente)
    val clientName: String = "",
    val clientEmail: String = "",     // 🔹 Nuevo: para contacto
    val service: String,
    val price: Double = 0.0,
    val dateMillis: Long,             // ✅ Mantenemos para compatibilidad
    val timeHour: Int,
    val timeMinute: Int,
    val notes: String = "",
    val status: AppointmentStatus = AppointmentStatus.PENDING, // 🔹 Nuevo: estado
    val createdAt: Long = System.currentTimeMillis(),           // 🔹 Nuevo: timestamp
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 🔹 Constructor auxiliar para crear desde Firestore Document
    constructor(documentId: String, map: Map<String, Any?>) : this(
        id = documentId,
        userId = map["userId"] as? String ?: "",
        clientName = map["clientName"] as? String ?: "",
        clientEmail = map["clientEmail"] as? String ?: "",
        service = map["service"] as? String ?: "",
        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
        dateMillis = (map["dateMillis"] as? Number)?.toLong() ?: 0L,
        timeHour = (map["timeHour"] as? Number)?.toInt() ?: 0,
        timeMinute = (map["timeMinute"] as? Number)?.toInt() ?: 0,
        notes = map["notes"] as? String ?: "",
        status = try {
            AppointmentStatus.valueOf((map["status"] as? String)?.uppercase() ?: "PENDING")
        } catch (e: Exception) { AppointmentStatus.PENDING },
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
    )

    // 🔹 Convertir a Map para guardar en Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "clientName" to clientName,
            "clientEmail" to clientEmail,
            "service" to service,
            "price" to price,
            "dateMillis" to dateMillis,
            "timeHour" to timeHour,
            "timeMinute" to timeMinute,
            "notes" to notes,
            "status" to status.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    val isUpcoming: Boolean
        get() = dateTimeMillis > System.currentTimeMillis()

    val isPast: Boolean
        get() = dateTimeMillis < System.currentTimeMillis()

    val dateTimeMillis: Long
        get() = dateMillis + (timeHour * 3600000L) + (timeMinute * 60000L)

    val endTimeMillis: Long
        get() = dateTimeMillis + (60 * 60000L) // Por ahora 60 min por defecto
}