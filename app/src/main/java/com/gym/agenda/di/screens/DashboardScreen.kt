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
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.di.viewmodel.AuthViewModel
import com.gym.agenda.ui.utils.UiUtils
import com.gym.agenda.di.viewmodel.GymListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAppointments: () -> Unit,
    onNavigateToNewAppointment: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    listViewModel: GymListViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val appointments by listViewModel.appointments.collectAsState()

    val upcomingAppointments = appointments.filter { it.isUpcoming }
    val userName = currentUser?.name ?: "Usuario"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewAppointment) {
                Icon(Icons.Default.Add, "Nueva cita")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Saludo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¡Hola, $userName!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bienvenido a Gym Agenda",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Citas Pendientes",
                        count = upcomingAppointments.size,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Citas",
                        count = appointments.size,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Próximas Citas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (upcomingAppointments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No tienes citas programadas",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onNavigateToNewAppointment) {
                                Text("Agendar una cita")
                            }
                        }
                    }
                }
            } else {
                items(upcomingAppointments.take(5)) { appointment ->
                    AppointmentCard(appointment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToAppointments,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver todas las citas")
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppointmentCard(appointment: GymAppointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.service,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
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
    }
}
