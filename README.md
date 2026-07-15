# <div align="center"> Gym Agenda - Sistema de Gestión de Citas </div> 
<div align="center"> 
    <img src="https://raw.githubusercontent.com/skratfall/AgendaGym/refs/heads/master/app/src/main/ic_launcher-playstore.png" width="190"  align="center"/>
</div>    

---
<div align="center">

![Android](https://img.shields.io/badge/Android-31+-green?style=flat-square&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple?style=flat-square&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue?style=flat-square)
![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20Auth%20%7C%20FCM-yellow?style=flat-square&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

**Una solución completa de gestión de citas para gimnasios, construida con tecnología moderna de Android.**

[Características](#-características-principales) • [Arquitectura](#-arquitectura) • [Instalación](#-instalación) • [Desarrollo](#-desarrollo)

</div>

---

## 📋 Descripción General

**Gym Agenda** es una aplicación de Android nativa que permite la gestión integral de citas y servicios en centros de entrenamiento físico. La aplicación proporciona funcionalidades tanto para clientes como para administradores, con sincronización en tiempo real mediante Firebase y notificaciones instantáneas.

### 🎯 Propósito Principal

- **Gestión de Citas**: Crear, editar, cancelar y consultar citas de forma intuitiva
- **Control de Servicios**: Administrar servicios disponibles y sus precios
- **Notificaciones en Tiempo Real**: Alertas automáticas a administradores y clientes
- **Filtrado Avanzado**: Sistema sofisticado de filtros para citas y busca rápida
- **Autenticación Segura**: Integración con Firebase Authentication
- **Sincronización en Vivo**: Datos actualizados instantáneamente con Firestore

---

## 🎯 Características Principales

### Para Usuarios (Clientes)
- ✅ Registro e inicio de sesión seguro
- ✅ Búsqueda y visualización de servicios disponibles
- ✅ Reserva de citas con selección de fecha y hora
- ✅ Historial de citas personales
- ✅ Notificaciones de confirmación y recordatorios
- ✅ Gestión de perfil de usuario

### Para Administradores
- ✅ Panel de control con estadísticas
- ✅ Visualización completa de todas las citas
- ✅ Sistema avanzado de filtros y búsqueda
- ✅ Cambio de estado de citas (Pendiente → Confirmada → Completada → Cancelada)
- ✅ Gestión de servicios y precios
- ✅ Notificaciones automáticas para nuevas citas
- ✅ Observador en tiempo real de cambios administrativos

---

## 🏗️ Arquitectura

### Patrón Arquitectónico: MVVM + Clean Architecture

```
app/src/main/java/com/gym/agenda/
├── 📁 data/                          # Capa de datos
│   ├── 📁 firebase/                  # Servicios de Firebase
│   │   ├── FirebaseAuthService.kt    # Autenticación
│   │   └── FirestoreService.kt       # Base de datos
│   ├── 📁 model/                     # Modelos de datos
│   │   ├── GymAppointment.kt
│   │   ├── User.kt
│   │   ├── Service.kt
│   │   ├── AppointmentStatus.kt
│   │   ├── PaymentStatus.kt
│   │   ├── UserRole.kt
│   │   ├── AppointmentFilters.kt
│   │   └── NotificationEvent.kt
│   └── 📁 repository/                # Repositorios
│       ├── AppointmentRepository.kt
│       ├── AuthRepository.kt
│       └── ServiceRepository.kt
├── 📁 di/                            # Inyección de dependencias (Hilt)
│   ├── FirebaseModule.kt
│   ├── GymViewModelFactory.kt
│   └── 📁 navigation/
│       ├── GymNav.kt
│       └── GymNavHost.kt
├── 📁 ui/                            # Capa de presentación (Compose)
│   ├── 📁 screens/
│   ├── 📁 components/
│   ├── 📁 theme/
│   └── viewmodels/
├── 📁 fcm/                           # Firebase Cloud Messaging
│   └── AgendaMessagingService.kt
├── 📁 utils/                         # Utilidades
│   ├── AdminNotificationObserver.kt  # Observador de cambios admin
│   └── [helpers/extensions]
├── AgendaApp.kt                      # Clase Application
└── MainActivity.kt                   # Actividad principal
```

---

## 📊 Modelos de Datos

### GymAppointment (Cita)
```kotlin
data class GymAppointment(
    val id: String,              // ID de Firestore
    val userId: String,          // ID del usuario
    val clientName: String,
    val clientEmail: String,
    val service: String,
    val price: Double,
    val dateMillis: Long,        // Fecha en milisegundos
    val timeHour: Int,
    val timeMinute: Int,
    val notes: String,
    val status: AppointmentStatus,  // PENDING, CONFIRMED, COMPLETED, CANCELLED
    val createdAt: Long,
    val updatedAt: Long
)
```

### AppointmentStatus
- `PENDING` - Cita pendiente de confirmación
- `CONFIRMED` - Cita confirmada
- `COMPLETED` - Cita completada
- `CANCELLED` - Cita cancelada

### User
- Datos personales del usuario
- Rol (USER o ADMIN)
- Información de contacto

### Service
- Nombre del servicio
- Precio
- Descripción
- Disponibilidad

---

## 🛠️ Stack Tecnológico

### Frontend
| Tecnología | Versión | Descripción |
|-----------|---------|------------|
| **Kotlin** | 2.0+ | Lenguaje de programación |
| **Jetpack Compose** | Latest | UI declarativa moderna |
| **Material Design 3** | Latest | Sistema de diseño |
| **Jetpack Navigation** | Latest | Navegación entre pantallas |
| **ViewModel** | Latest | Gestión de estado |
| **Lifecycle** | Latest | Ciclo de vida de componentes |

### Backend & Datos
| Tecnología | Descripción |
|-----------|------------|
| **Firebase Authentication** | Autenticación de usuarios |
| **Cloud Firestore** | Base de datos en tiempo real |
| **Firebase Cloud Messaging** | Notificaciones push |
| **Firebase Services** | Configuración (google-services.json) |

### Inyección de Dependencias
| Librería | Descripción |
|---------|------------|
| **Hilt** | Inyección de dependencias simplificada |
| **Dagger 2** | Framework subyacente de Hilt |

### Utilidades
| Librería | Versión | Descripción |
|---------|---------|------------|
| **Timber** | Latest | Logging avanzado |
| **Lottie** | 6.x | Animaciones JSON |
| **Shimmer** | Latest | Efecto de carga esquelética |
| **Accompanist** | Latest | Extensiones de Compose |
| **Coroutines** | Latest | Programación asincrónica |
| **WorkManager** | Latest | Tareas programadas |
| **Constraint Layout** | 2.2.1 | Layouts complejos |
| **RecyclerView** | 1.4.0 | Listas optimizadas |

---

## 🔧 Instalación y Configuración

### Requisitos Previos
- **Android Studio** 2023.1.1 o superior
- **JDK 21** o superior
- **Android SDK 37** (compileSdk)
- **Android 12+** (minSdk 31)
- Cuenta de **Firebase** configurada

### Pasos de Instalación

#### 1. Clonar el repositorio
```bash
git clone https://github.com/CamiloMinoTa/AndoidStudio.git
cd AndoidStudio
```

#### 2. Configurar Firebase
```bash
# 1. Ve a Firebase Console (https://console.firebase.google.com/)
# 2. Crea un nuevo proyecto o usa uno existente
# 3. Descarga el archivo google-services.json
# 4. Colócalo en: app/google-services.json
```

#### 3. Configurar propiedades locales
```bash
# Crear archivo local.properties (si no existe)
echo "sdk.dir=/path/to/android/sdk" > local.properties
```

#### 4. Abrir en Android Studio
```bash
# Abre Android Studio y carga el proyecto
# Android Studio → File → Open → Selecciona la carpeta del proyecto
```

#### 5. Sincronizar Gradle
```bash
# Android Studio → File → Sync Now
# O ejecutar:
./gradlew sync
```

#### 6. Ejecutar la aplicación
```bash
# Conecta un dispositivo o emulador
./gradlew installDebug
# O desde Android Studio: Run → Run 'app'
```

---

## 💻 Desarrollo

### Estructura de Dependencias

Las dependencias se gestionan mediante **Version Catalog** en `gradle/libs.versions.toml`:

```gradle
# Composición del proyecto
plugins {
    alias(libs.plugins.android.application)      # Plugin Android
    alias(libs.plugins.kotlin.android)           # Soporte Kotlin
    alias(libs.plugins.compose.compiler)         # Compilador Compose
    alias(libs.plugins.ksp)                      # Kotlin Symbol Processing
    alias(libs.plugins.hilt.android)             # Inyección de dependencias
    id("com.google.gms.google-services")         # Servicios Google/Firebase
}
```

### Configuración de Build

```gradle
android {
    namespace = "com.gym.agenda"
    compileSdk = 37
    
    defaultConfig {
        applicationId = "com.gym.agenda.camilo_12"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true      # Habilitado Compose
        buildConfig = true  # BuildConfig disponible
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
```

### Compilación y Build

```bash
# Build Debug
./gradlew assembleDebug

# Build Release
./gradlew assembleRelease

# Clean build
./gradlew clean build

# Ver dependencias
./gradlew dependencies
```

---

## 🔐 Seguridad y Permisos

### Permisos Solicitados
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
```

### Características de Seguridad
- ✅ Autenticación con Firebase (Contraseñas encriptadas)
- ✅ Autorización basada en roles (USER/ADMIN)
- ✅ Reglas de Firestore para control de acceso
- ✅ HTTPS para todas las comunicaciones
- ✅ ProGuard/R8 para ofuscación de código (release)

---

## 📱 Compatibilidad

| Aspecto | Especificación |
|--------|---------------|
| **Min SDK** | 31 (Android 12) |
| **Target SDK** | 37 (Android 14) |
| **Compilación** | SDK 37 |
| **Java** | 21 |
| **Kotlin** | 2.0+ |

---

## 🚀 Características Técnicas Avanzadas

### Logging con Timber
- **Desarrollo**: Logs completos en consola
- **Producción**: Solo errores críticos
- Clase `CrashReportingTree` personalizada

### AdminNotificationObserver
- Observador en tiempo real de cambios de citas
- Actualización automática del panel administrativo
- Notificaciones push instantáneas

### Filtrado Inteligente
- Filtros por fecha, estado, cliente, servicio
- Búsqueda por nombre/email
- Paginación de resultados

### Notificaciones Inteligentes
- FCM para push notifications
- Notificaciones de confirmación automática
- Recordatorios de citas próximas
- Alertas para administrador

---

## 📁 Archivos de Documentación

Documentación técnica adicional disponible:

- **DETALLES_TECNICOS_FILTROS.md** - Análisis profundo del sistema de filtros
- **FILTROS_ADMIN_CITAS.md** - Guía de filtros administrativos
- **GUIA_FILTROS_RAPIDA.md** - Referencia rápida de filtros
- **LOGGING_IMPLEMENTATION.md** - Detalles de implementación de logging
- **VALIDATION_SYSTEM.md** - Sistema de validación de datos

---

## 🔄 Flujo de Datos

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTACIÓN (UI)                         │
│                   Jetpack Compose                            │
└────────┬──────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────┐
│               VISTA - MODELO (ViewModel)                       │
│         Gestión de estado y lógica de negocio                │
└────────┬──────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────┐
│                REPOSITORIO                                     │
│    Abstracción de fuentes de datos                            │
│    ├── AppointmentRepository                                  │
│    ├── AuthRepository                                         │
│    └── ServiceRepository                                      │
└────────┬──────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────┐
│              SERVICIOS DE DATOS                                │
│         Firebase (Firestore, Auth, FCM)                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 Estadísticas del Proyecto

```
Lenguaje: Kotlin
Clases: 40+
Líneas de código: 5,000+
Pantallas UI: 15+
Modelos de datos: 8+
Repositorios: 3
```

---

## 🤝 Contribución

Este es un proyecto activo en desarrollo. Las contribuciones son bienvenidas:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📝 Licencia

Este proyecto está bajo la licencia **MIT**. Ver el archivo LICENSE para más detalles.

---

## 👨‍💻 Autores

**Camilo Mino** and **Skratfall** son los principales desarrolladores de este proyecto.
- GitHub: [@CamiloMinoTa](https://github.com/CamiloMinoTa)
- Github: [@skratfall](https://github.com/skratfall)
- Proyecto: Gym Agenda

---

## 📞 Soporte

Para reportar problemas o sugerencias:
- Abre un issue en GitHub
- Contacta directamente al autor

---

## 📚 Recursos Útiles

- [Documentación Android Oficial](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

<div align="center">

**Hecho con ❤️ en Kotlin**

⭐ Si te gusta este proyecto, no olvides darle una estrella

</div>

<div align="center">
  <img src="https://user-images.githubusercontent.com/74038190/212284115-f47cd8ff-2ffb-4b04-b5bf-4d1c14c0247f.gif" alt="Bottom Line" width="100%" />
</div>

# Siguenos en nuestras redes sociales:
<div align="center" >

[![GitHub](https://img.shields.io/badge/GitHub-@CamiloMinoTa-black?style=flat-square&logo=github)](https://github.com/CamiloMinoTa)
[![GitHub](https://img.shields.io/badge/GitHub-@skratfall-black?style=flat-square&logo=github)](https://github.com/skratfall)   
   
[![LinkedIn](https://img.shields.io/badge/LinkedIn-@skratfall-blue?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/brandondon-am-/)

[![Instagram](https://img.shields.io/badge/Instagram-@skratfall-purple?style=flat-square&logo=instagram)](https://www.instagram.com/brandon_a.m_/)

[![Discord](https://img.shields.io/badge/Discord-@skratfall-blueviolet?style=flat-square&logo=discord)](https://discord.com/users/742371315859587094)

[![Roblox](https://img.shields.io/badge/Roblox-@skratfall-red?style=flat-square&logo=roblox)](https://www.roblox.com/es/users/446755162/profile)

</div>
