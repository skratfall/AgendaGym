package com.gym.agenda.di.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.di.viewmodel.AdminViewModel
import com.gym.agenda.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val appointments by viewModel.allAppointments.collectAsState()
    var appointmentToDelete by remember { mutableStateOf<GymAppointment?>(null) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las Citas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
