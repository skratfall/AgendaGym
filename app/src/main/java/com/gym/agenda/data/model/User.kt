package com.gym.agenda.data.model


import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = 0L
) {
    // 🔹 Constructor desde Firestore Document
    constructor(documentId: String, map: Map<String, Any?>) : this(
        id = documentId,
        email = map["email"] as? String ?: "",
        name = map["name"] as? String ?: "",
        phone = map["phone"] as? String ?: "",
        photoUrl = map["photoUrl"] as? String,
        role = try {
            UserRole.valueOf((map["role"] as? String)?.uppercase() ?: "USER")
        } catch (e: Exception) {
            UserRole.USER
        },
        isActive = map["isActive"] as? Boolean ?: (map["active"] as? Boolean ?: true),
        fcmToken = map["fcmToken"] as? String,
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        lastLogin = (map["lastLogin"] as? Number)?.toLong() ?: 0L
    )

    // 🔹 Convert a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "email" to email,
            "name" to name,
            "phone" to phone,
            "photoUrl" to photoUrl,
            "role" to role.name,
            "isActive" to isActive,
            "fcmToken" to fcmToken,
            "createdAt" to createdAt,
            "lastLogin" to lastLogin
        )
    }

    // 🔹 Propiedades computadas útiles - Marcadas con @Exclude para que Firebase las ignore
    @get:Exclude
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN

    @get:Exclude
    val displayName: String
        get() = if (name.isNotBlank()) name else email.split("@").first()
}