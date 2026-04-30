package com.gym.agenda.data.repository

import com.gym.agenda.data.firebase.AuthService
import com.gym.agenda.data.firebase.FirestoreService
import com.gym.agenda.data.model.User
import com.gym.agenda.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val firestoreService: FirestoreService
) {

    // 🔹 Estado de autenticación
    val authState: Flow<User?> = authService.authState.map { firebaseUser ->
        firebaseUser?.let {
            firestoreService.getUserById(it.uid).getOrNull()
        }
    }

    // 🔹 Usuario actual (síncrono)
    val currentUser: User?
        get() = authService.currentUser?.let {
            // Nota: Esto debería ser suspend, pero para acceso rápido...
            // Mejor usar authState en ViewModels
            null
        }

    // 🔹 Registrar nuevo usuario
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String = "",
        role: UserRole = UserRole.USER
    ): Result<User> {
        return authService.register(email, password, name, phone, role)
    }

    // 🔹 Iniciar sesión
    suspend fun login(email: String, password: String): Result<User> {
        return authService.login(email, password).onSuccess { user ->
            // Actualizar lastLogin
            firestoreService.updateUser(
                user.id,
                mapOf("lastLogin" to System.currentTimeMillis())
            )
        }
    }

    // 🔹 Cerrar sesión
    fun logout() {
        authService.logout()
    }

    // 🔹 Resetear contraseña
    suspend fun resetPassword(email: String): Result<Unit> {
        return authService.resetPassword(email)
    }

    // 🔹 Obtener datos del usuario actual
    suspend fun getCurrentUser(): Result<User> {
        return authService.getCurrentUserData()
    }

    // 🔹 Actualizar perfil
    suspend fun updateProfile(updates: Map<String, Any>): Result<Unit> {
        return authService.updateUserProfile(updates)
    }

    // 🔹 Verificar si es admin
    suspend fun isAdmin(): Boolean {
        return authService.isAdmin()
    }

    // 🔹 Obtener todos los usuarios (ADMIN)
    fun getAllUsers(): Flow<List<User>> {
        return firestoreService.getAllUsers()
    }

    // 🔹 Actualizar rol de usuario (ADMIN)
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return firestoreService.updateUserRole(userId, role)
    }

    // 🔹 Activar/desactivar usuario (ADMIN)
    suspend fun toggleUserActive(userId: String, isActive: Boolean): Result<Unit> {
        return firestoreService.updateUser(userId, mapOf("isActive" to isActive))
    }

    // 🔹 Eliminar usuario (ADMIN)
    suspend fun deleteUser(userId: String): Result<Unit> {
        return firestoreService.deleteUser(userId)
    }
}