# 🔄 Próximos Pasos - Extender Logging

## 📝 Archivos Pendientes de Logging

Esta es una guía para agregar logging similar a otros componentes importantes.

### 1️⃣ AdminViewModel.kt

Agregar estos logs:

```kotlin
override fun onCreate() {
    super.onCreate()
    Timber.d("📊 AdminViewModel inicializado")
    observeAllAppointments()
    calculateStatistics()
}

private fun observeAllAppointments() {
    Timber.d("📥 Obteniendo TODAS las citas (Admin)...")
    viewModelScope.launch {
        appointmentRepository.getAllAppointments()
            .catch { e ->
                Timber.e(e, "❌ Error al cargar citas admin")
            }
            .collect { list ->
                Timber.i("✅ Citas Admin cargadas: ${list.size} citas")
            }
    }
}

fun deleteAppointment(appointmentId: String) {
    Timber.d("🗑️ Admin eliminando cita: $appointmentId")
    viewModelScope.launch {
        appointmentRepository.deleteAppointment(appointmentId)
            .onSuccess {
                Timber.i("✅ Cita eliminada por Admin: $appointmentId")
                refreshAppointments()
            }
            .onFailure { error ->
                Timber.e(error, "❌ Admin no pudo eliminar: $appointmentId")
            }
    }
}

fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
    Timber.d("🔄 Admin cambiando estado: $appointmentId -> $status")
    viewModelScope.launch {
        appointmentRepository.updateAppointmentStatus(appointmentId, status)
            .onSuccess {
                Timber.i("✅ Estado actualizado por Admin: $status")
            }
            .onFailure { error ->
                Timber.e(error, "❌ Error al cambiar estado")
            }
    }
}
```

### 2️⃣ AuthViewModel.kt

```kotlin
init {
    Timber.d("🔐 AuthViewModel inicializado")
    checkAuthState()
}

fun login(email: String, password: String) {
    Timber.d("🔑 Intento de login: $email")
    viewModelScope.launch {
        authRepository.login(email, password)
            .onSuccess { user ->
                Timber.i("✅ Login exitoso: ${user.name} (${user.role})")
            }
            .onFailure { error ->
                Timber.w("⚠️ Login fallido: ${error.message}")
            }
    }
}

fun register(email: String, password: String, name: String, role: UserRole) {
    Timber.d("📝 Registro de usuario: $name ($role)")
    viewModelScope.launch {
        authRepository.register(email, password, name, role)
            .onSuccess {
                Timber.i("✅ Registro exitoso: $name")
            }
            .onFailure { error ->
                Timber.e(error, "❌ Error en registro")
            }
    }
}

fun logout() {
    Timber.i("👋 Usuario cerrando sesión")
    authRepository.logout()
}
```

### 3️⃣ AddEditViewModel.kt

```kotlin
init {
    Timber.d("✏️ AddEditViewModel inicializado para cita: $appointmentId")
    loadAppointment()
}

fun saveAppointment() {
    Timber.d("💾 Guardando cita...")
    viewModelScope.launch {
        val result = if (appointmentId.isEmpty()) {
            Timber.d("➕ Crear nueva cita")
            appointmentRepository.createAppointment(currentAppointment)
        } else {
            Timber.d("✏️ Actualizar cita: $appointmentId")
            appointmentRepository.updateAppointment(currentAppointment)
        }
        
        result.onSuccess {
            Timber.i("✅ Cita guardada exitosamente")
        }.onFailure { error ->
            Timber.e(error, "❌ Error al guardar cita")
        }
    }
}

fun checkAvailability(date: Long, hour: Int, minute: Int) {
    Timber.d("🔍 Verificando disponibilidad de horario...")
    viewModelScope.launch {
        val available = appointmentRepository.isTimeSlotAvailable(
            date, hour, minute, duration = 60
        )
        Timber.d("📊 Disponibilidad: ${if (available) "✅ Disponible" else "❌ Ocupado"}")
    }
}
```

### 4️⃣ FirestoreService.kt

```kotlin
suspend fun createAppointment(appointment: GymAppointment): Result<String> {
    Timber.d("🔥 Firestore: Creando documento de cita...")
    return try {
        val docRef = db.collection("appointments").document()
        val appointmentWithId = appointment.copy(id = docRef.id)
        docRef.set(appointmentWithId).await()
        Timber.d("✅ Documento creado en Firestore: ${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "❌ Error Firestore al crear cita")
        Result.failure(e)
    }
}

fun getAppointments(userId: String): Flow<List<GymAppointment>> {
    Timber.d("🔥 Firestore: Escuchando citas de usuario: $userId")
    return db.collection("appointments")
        .whereEqualTo("userId", userId)
        .snapshotFlow()
        .map { snapshot ->
            Timber.d("🔄 Snapshot recibido: ${snapshot.size()} documentos")
            snapshot.toObjects(GymAppointment::class.java)
        }
}
```

---

## 🎯 Patrón de Logging Recomendado

```kotlin
// 1. Inicio de operación
Timber.d("🔄 Iniciando operación: $detalles")

// 2. Pasos intermedios (si aplica)
Timber.d("✓ Paso 1: validación completada")
Timber.d("✓ Paso 2: consulta Firestore completada")

// 3. Resultado final
Timber.i("✅ Operación completada exitosamente")
// O
Timber.e(exception, "❌ Operación falló: ${error.message}")
```

---

## ⚠️ Cosas a Evitar

❌ **No hagas:**
```kotlin
Timber.d("Password: $password")  // ¡Expone datos sensibles!
Timber.d("Token: $authToken")    // ¡Nunca!
```

✅ **Haz esto:**
```kotlin
Timber.d("👤 Autenticación iniciada para: ${user.email}")  // Sin password
Timber.i("🔐 Token recibido - ${token.length} caracteres")  // Sin mostrar token
```

---

## 📊 Beneficios en Producción

1. **Debug Remoto**: Analizar problemas sin acceso al dispositivo
2. **Performance Tracking**: Identificar operaciones lentas
3. **Error Analytics**: Tendencias de errores
4. **User Behavior**: Entender cómo los usuarios usan la app
5. **Compliance**: Auditoría de acciones importantes

---

## 🔗 Próxima Integración

Para aún más potencia, considera integrar:

```kotlin
// Crashlytics Integration
if (!BuildConfig.DEBUG) {
    Timber.plant(CrashlyticsTree())
}

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}
```


