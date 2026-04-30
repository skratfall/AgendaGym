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
import com.gym.agenda.ui.utils.UiUtils
import com.gym.agenda.di.viewmodel.GymListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onNavigateToAddEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GymListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    var showFilterDropdown by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<AppointmentStatus?>(null) }

    val filteredAppointments = if (selectedFilter == null) {
        appointments
    } else {
        appointments.filter { it.status == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Citas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterDropdown = true }) {
                            Icon(Icons.Default.FilterList, "Filtrar")
                        }

                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    selectedFilter = null
                                    showFilterDropdown = false
                                }
                            )
                            AppointmentStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        selectedFilter = status
                                        showFilterDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddEdit("new") }) {
                Icon(Icons.Default.Add, "Nueva cita")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay citas")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(filteredAppointments, key = { it.id }) { appointment ->
                    AppointmentListItem(
                        appointment = appointment,
                        onEdit = { onNavigateToAddEdit(appointment.id) },
                        onDelete = { viewModel.deleteAppointment(appointment) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AppointmentListItem(
    appointment: GymAppointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cita") },
            text = { Text("¿Estás seguro de eliminar esta cita?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.service,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = UiUtils.formatDate(appointment.dateMillis),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UiUtils.formatTime(appointment.timeHour, appointment.timeMinute),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                UiUtils.StatusChip(appointment.status)
            }

            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appointment.notes,
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
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}