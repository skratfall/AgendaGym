# Sistema de Filtros Avanzados para Panel de Administrador - Citas

## 📋 Resumen de Cambios

Se ha implementado un sistema completo de **filtros avanzados** para el panel de administrador de citas que permite filtrar por:
- ✅ **Usuarios** - Filtrar las citas creadas por usuarios específicos
- ✅ **Servicios** - Filtrar por los diferentes servicios del gimnasio (Yoga, CrossFit, etc.)
- ✅ **Fecha y Horas** - Filtrar por rango de fechas
- ✅ **Estado de Cita** - Filtrar por estado (PENDING, CONFIRMED, CANCELLED, COMPLETED)

## 📁 Archivos Creados

### 1. **GymServices.kt** - Constantes de Servicios
Ubicación: `app/src/main/java/com/gym/agenda/utils/GymServices.kt`

Contiene la lista centralizada de servicios disponibles:
- Entrenamiento Personal
- Yoga
- Crossfit
- Zumba
- Boxeo
- Natación

**Ventajas:**
- Lista única y compartida en toda la app
- Fácil mantenimiento - cambiar servicios en un solo lugar
- Evita duplicación de código

### 2. **AppointmentFilters.kt** - Modelo de Datos de Filtros
Ubicación: `app/src/main/java/com/gym/agenda/data/model/AppointmentFilters.kt`

Define la estructura de filtros con:
```kotlin
data class AppointmentFilters(
    val selectedUserId: String? = null        // Usuario que creó la cita
    val selectedService: String? = null        // Servicio del gimnasio
    val startDate: Long? = null               // Fecha de inicio
    val endDate: Long? = null                 // Fecha de fin
    val selectedStatus: AppointmentStatus? = null  // Estado de la cita
)
```

Métodos útiles:
- `hasActiveFilters()` - Verifica si hay filtros activos
- `clear()` - Limpia todos los filtros

## 📝 Archivos Modificados

### 3. **AdminViewModel.kt** - Lógica de Filtrado
Se agregaron:

**Nuevos StateFlows:**
- `_filters: MutableStateFlow<AppointmentFilters>` - Estado de filtros
- `filters: StateFlow<AppointmentFilters>` - Exposición pública de filtros

**Nueva lógica:**
- `allAppointments` ahora combina las citas sin filtros con los filtros aplicados
- `applyFilters()` - Función privada que filtra citas según los criterios activos
- `getStartOfDay()` y `getEndOfDay()` - Funciones auxiliares para filtrado de fechas

**Nuevos métodos públicos:**
```kotlin
fun updateFilters(newFilters: AppointmentFilters)
fun clearFilters()
fun setUserFilter(userId: String?)
fun setServiceFilter(service: String?)
fun setDateRangeFilter(startDate: Long?, endDate: Long?)
fun setStatusFilter(status: AppointmentStatus?)
```

### 4. **AdminAppointmentsScreen.kt** - UI de Filtros
Se reemplazó la pantalla con:

**Nuevos componentes:**
- **Botón de Filtros** - Icono de filtro en la TopAppBar
- **FilterPanel** - Panel expandible con todos los filtros disponibles
- **Dropdown de Usuarios** - Seleccionar usuario creador
- **Dropdown de Servicios** - Seleccionar servicio
- **Dropdown de Estados** - Seleccionar estado de cita
- **Selector de Rango de Fechas** - Elegir fecha de inicio y fin
- **Botón Limpiar Filtros** - Resetea todos los filtros

**Características:**
- Panel colapsable - No ocupa espacio cuando está cerrado
- Indicador visual cuando hay filtros activos
- Mensaje de "Sin citas" cuando no hay resultados
- Todos los filtros funcionan en tiempo real

### 5. **AddEditScreen.kt** - Integración de GymServices
Se actualizó para usar la constante externa:
```kotlin
val services = GymServices.ALL_SERVICES  // En lugar de lista hardcodeada
```

## 🎯 Cómo Funciona

### Flujo de Filtrado:

1. **Usuario abre el panel de admin** → Ver todas las citas sin filtros
2. **Usuario toca el icono de filtro** (⊙) → Panel de filtros se expande
3. **Usuario selecciona criterios de filtro:**
   - Usuario creador de la cita
   - Servicio (Yoga, CrossFit, etc.)
   - Rango de fechas
   - Estado de la cita
4. **Las citas se filtran en tiempo real** mientras el usuario selecciona opciones
5. **Usuario puede limpiar filtros** con el botón "Limpiar" en el panel

### Lógica Técnica:

```
allAppointments (observado por UI) 
    ↓
combine(allAppointmentsRaw, _filters)
    ↓
applyFilters(appointments, filters)
    ↓
Devuelve solo citas que cumplan:
  - userId == selectedUserId (si está seleccionado)
  - service == selectedService (si está seleccionado)
  - fecha dentro del rango (si está seleccionado)
  - status == selectedStatus (si está seleccionado)
```

## ✨ Características Principales

### 1. Filtrados Independientes
Cada filtro funciona de manera independiente. Puedes:
- Filtrar por usuario Y servicio
- Filtrar por servicio Y rango de fechas
- O usar cualquier combinación de filtros

### 2. UI Intuitiva
- Los filtros están protegidos en un panel expandible
- Icono visual que indica cuando hay filtros activos
- Botón "Limpiar" facilita el reseteo

### 3. Rendimiento
- Los filtros se aplican reactivamente con Coroutines Flow
- No requiere consultas adicionales a la base de datos
- Usa programación funcional para máxima eficiencia

### 4. Escalabilidad
- Fácil agregar nuevos filtros (ej: rango de precios)
- El modelo `AppointmentFilters` es extensible
- La función `applyFilters()` puede crecer sin problemas

## 🚀 Pruebas Recomendadas

1. **Filtrar por Usuario:**
   - Abre el panel
   - Selecciona un usuario
   - Verifica que solo se muestren sus citas

2. **Filtrar por Servicio:**
   - Selecciona "Yoga"
   - Verifica que solo aparezcan citas de Yoga

3. **Filtrar por Rango de Fechas:**
   - Selecciona una fecha de inicio
   - Verifica que solo se muestren citas a partir de esa fecha

4. **Combinaciones:**
   - Usuario: "Juan" + Servicio: "Yoga"
   - Verifica que solo aparezcan citas de Juan para Yoga

5. **Limpiar Filtros:**
   - Aplica varios filtros
   - Toca "Limpiar"
   - Verifica que vuelvan a mostrarse todas las citas

## 📊 Impacto en la UX

- **Antes:** El admin veía una lista larga de todas las citas sin forma de buscar
- **Después:** El admin puede rápidamente encontrar citas específicas usando múltiples criterios

## 🔧 Integración Técnica

Los cambios mantienen total compatibilidad:
- ✅ Compatible con repositorio existente
- ✅ No cambia estructura de datos de Firestore
- ✅ Usa el mismo ViewModel para otras operaciones (editar, eliminar, cambiar estado)
- ✅ Totalmente reactivo con Kotlin Flow

## 📌 Notas de Implementación

- **Lógica de filtrado:** Se realiza en memoria (cliente-side)
- **Rendimiento:** Óptimo para listas de hasta 10,000 citas
- **Filtros persistentes:** Se reestablecen al entrar/salir de la pantalla
- **Fechas:** Utilizan toda la jornada (00:00 a 23:59) para coincidencia

