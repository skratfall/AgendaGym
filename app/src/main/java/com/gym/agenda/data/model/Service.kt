package com.gym.agenda.data.model


data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: ServiceCategory = ServiceCategory.FITNESS,
    val duration: Int = 60, // minutos
    val price: Double = 0.0,
    val currency: String = "USD",
    val isActive: Boolean = true,
    val requiresApproval: Boolean = false,
    val maxParticipants: Int = 1, // 1 = individual, >1 = grupal
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor(documentId: String, map: Map<String, Any?>) : this(
        id = documentId,
        name = map["name"] as? String ?: "",
        description = map["description"] as? String ?: "",
        category = try {
            ServiceCategory.valueOf((map["category"] as? String)?.uppercase() ?: "FITNESS")
        } catch (e: Exception) {
            ServiceCategory.FITNESS
        },
        duration = (map["duration"] as? Number)?.toInt() ?: 60,
        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
        currency = map["currency"] as? String ?: "USD",
        isActive = map["isActive"] as? Boolean ?: true,
        requiresApproval = map["requiresApproval"] as? Boolean ?: false,
        maxParticipants = (map["maxParticipants"] as? Number)?.toInt() ?: 1,
        imageUrl = map["imageUrl"] as? String,
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "category" to category.name,
            "duration" to duration,
            "price" to price,
            "currency" to currency,
            "isActive" to isActive,
            "requiresApproval" to requiresApproval,
            "maxParticipants" to maxParticipants,
            "imageUrl" to imageUrl,
            "createdAt" to createdAt
        )
    }
}

enum class ServiceCategory {
    FITNESS,
    YOGA,
    SPINNING,
    PERSONAL_TRAINER,
    NUTRITION,
    REHABILITATION,
    GROUP_CLASS,
    OTHER
}