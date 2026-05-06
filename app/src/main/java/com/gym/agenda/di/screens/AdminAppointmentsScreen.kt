package com.gym.agenda.di.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gym.agenda.data.model.AppointmentFilters
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.model.User
import com.gym.agenda.di.viewmodel.AdminViewModel
import com.gym.agenda.ui.utils.UiUtils
import com.gym.agenda.ui.utils.ActionFeedbackSnackbar
import com.gym.agenda.utils.GymServices


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val appointments by viewModel.allAppointments.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val users by viewModel.users.collectAsState()

    var appointmentToDelete by remember { mutableStateOf<GymAppointment?>(null) }
    var showFiltersPanel by remember { mutableStateOf(false) }

    // Refrescar cuando vuelve de editar
    LaunchedEffect(Unit) {
        viewModel.refreshAppointments()
    }

    // Diálogo de confirmación para eliminar
    if (appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text("Eliminar Cita") },
            text = { Text("¿Estás seguro de que quieres eliminar esta cita?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appointmentToDelete?.let { appointment ->
                            viewModel.deleteAppointment(appointment.id)
                            viewModel.refreshAppointments()
                        }
                        appointmentToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Todas las Citas") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFiltersPanel = !showFiltersPanel }) {
                            Icon(Icons.Default.Tune, "Filtros")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Panel de filtros
                if (showFiltersPanel) {
                    FilterPanel(
                        filters = filters,
                        users = users,
                        onFiltersChanged = { viewModel.updateFilters(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (appointments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.EventNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No hay citas disponibles",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(appointments, key = { it.id }) { appointment ->
                            AdminAppointmentCard(
                                appointment = appointment,
                                onStatusChange = { newStatus ->
                                    viewModel.updateAppointmentStatus(appointment.id, newStatus)
                                },
                                onEdit = { onNavigateToEdit(appointment.id) },
                                onDelete = { appointmentToDelete = appointment }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Notificación de feedback
        ActionFeedbackSnackbar(
            notification = notification,
            onDismiss = { viewModel.dismissNotification() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Panel de filtros avanzados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    filters: AppointmentFilters,
    users: List<User>,
    onFiltersChanged: (AppointmentFilters) -> Unit,
    onClearFilters: () -> Unit
) {
    var expandedUser by remember { mutableStateOf(false) }
    var expandedService by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (filters.hasActiveFilters()) {
                    TextButton(onClick = onClearFilters) {
                        Text("Limpiar", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Usuario
            ExposedDropdownMenuBox(
                expanded = expandedUser,
                onExpandedChange = { expandedUser = it }
            ) {
                OutlinedTextField(
                    value = users.find { it.id == filters.selectedUserId }?.displayName ?: "Todos los usuarios",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Usuario") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUser) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedUser,
                    onDismissRequest = { expandedUser = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los usuarios") },
                        onClick = {
                            onFiltersChanged(filters.copy(selectedUserId = null))
                            expandedUser = false
                        }
                    )
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.displayName) },
                            onClick = {
                                onFiltersChanged(filters.copy(selectedUserId = user.id))
                                expandedUser = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Servicio
            ExposedDropdownMenuBox(
                expanded = expandedService,
                onExpandedChange = { expandedService = it }
            ) {
                OutlinedTextField(
                    value = filters.selectedService ?: "Todos los servicios",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Servicio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedService) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedService,
                    onDismissRequest = { expandedService = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los servicios") },
                        onClick = {
                            onFiltersChanged(filters.copy(selectedService = null))
                            expandedService = false
                        }
                    )
                    GymServices.ALL_SERVICES.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service) },
                            onClick = {
                                onFiltersChanged(filters.copy(selectedService = service))
                                expandedService = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Estado
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = it }
            ) {
                OutlinedTextField(
                    value = filters.selectedStatus?.name ?: "Todos los estados",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Estado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los estados") },
                        onClick = {
                            onFiltersChanged(filters.copy(selectedStatus = null))
                            expandedStatus = false
                        }
                    )
                    AppointmentStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                onFiltersChanged(filters.copy(selectedStatus = status))
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por Fecha
            OutlinedTextField(
                value = if (filters.startDate != null) {
                    "Desde: ${UiUtils.formatDate(filters.startDate!!)}" +
                    (if (filters.endDate != null) " - Hasta: ${UiUtils.formatDate(filters.endDate!!)}" else "")
                } else {
                    "Todas las fechas"
                },
                onValueChange = { },
                readOnly = true,
                label = { Text("Rango de fechas") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Event, null)
                    }
                },
                singleLine = true
            )

            if (filters.startDate != null) {
                TextButton(
                    onClick = { onFiltersChanged(filters.copy(startDate = null, endDate = null)) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Limpiar fechas", fontSize = 12.sp)
                }
            }
        }
    }

    // Diálogo para seleccionar rango de fechas
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Seleccionar Rango de Fechas") },
            text = {
                Column {
                    DatePicker(state = datePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { date ->
                        onFiltersChanged(filters.copy(startDate = date, endDate = date))
                    }
                    showDatePicker = false
                }) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAppointmentCard(
    appointment: GymAppointment,
    onStatusChange: (AppointmentStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<AppointmentStatus?>(null) }

    if (showConfirmDialog && selectedStatus != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Cambio") },
            text = { Text("¿Estás seguro de cambiar el estado de la cita a ${selectedStatus?.name}?") },
            confirmButton = {
                Button(onClick = {
                    onStatusChange(selectedStatus!!)
                    showConfirmDialog = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.service,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = appointment.clientName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${UiUtils.formatDate(appointment.dateMillis)} - ${UiUtils.formatTime(appointment.timeHour, appointment.timeMinute)}",
                        fontSize = 14.sp
                    )
                }

                UiUtils.StatusChip(appointment.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    ) {
                        Text("Cambiar estado")
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AppointmentStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    selectedStatus = status
                                    showConfirmDialog = true
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notas: ${appointment.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
