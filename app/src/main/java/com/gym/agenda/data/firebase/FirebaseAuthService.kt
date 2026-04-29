package com.gym.agenda.data.firebase

import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // 🔹 Obtener usuario actual autenticado
    val currentUser: FirebaseAuthUser?
        get() = auth.currentUser?.let {
            FirebaseAuthUser(it.uid, it.email ?: "", it.displayName ?: "")
        }

    // 🔹 Flow del estado de autenticación
    val authState: Flow<FirebaseAuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.let {
                FirebaseAuthUser(it.uid, it.email ?: "", it.displayName ?: "")
            })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // 🔹 Registrar nuevo usuario
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String = "",
        role: UserRole = UserRole.USER
    ): Result<User> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID not found")

            // 2. Actualizar perfil con nombre
            authResult.user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            )?.await()

            // 3. Crear documento de usuario en Firestore
            val user = User(
                id = userId,
                email = email,
                name = name,
                phone = phone,
                role = role,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users").document(userId).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(handleAuthException(e))
        }
    }

    // 🔹 Iniciar sesión
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 1. Autenticar
            auth.signInWithEmailAndPassword(email, password).await()

            // 2. Obtener datos del usuario desde Firestore
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val userDoc = firestore.collection("users").document(userId).get().await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User data not found in Firestore")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(handleAuthException(e))
        }
    }

    // 🔹 Cerrar sesión
    fun logout() {
        auth.signOut()
    }

    // 🔹 Resetear contraseña
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(handleAuthException(e))
        }
    }

    // 🔹 Obtener datos completos del usuario
    suspend fun getCurrentUserData(): Result<User> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("No user logged in")

            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User data not found")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar perfil de usuario
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("No user logged in")

            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Verificar si es admin
    suspend fun isAdmin(): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role")
            role == UserRole.ADMIN.name
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 Manejar excepciones de autenticación
    private fun handleAuthException(e: Exception): Exception {
        return if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> Exception("Este email ya está registrado")
                "ERROR_INVALID_EMAIL" -> Exception("Email inválido")
                "ERROR_WRONG_PASSWORD" -> Exception("Contraseña incorrecta")
                "ERROR_USER_NOT_FOUND" -> Exception("Usuario no encontrado")
                "ERROR_USER_DISABLED" -> Exception("Usuario deshabilitado")
                "ERROR_TOO_MANY_REQUESTS" -> Exception("Demasiados intentos. Intenta más tarde")
                "ERROR_WEAK_PASSWORD" -> Exception("Contraseña demasiado débil")
                else -> Exception("Error de autenticación: ${e.message}")
            }
        } else {
            e
        }
    }
}

// 🔹 Data class para usuario autenticado
data class FirebaseAuthUser(
    val uid: String,
    val email: String,
    val displayName: String
)