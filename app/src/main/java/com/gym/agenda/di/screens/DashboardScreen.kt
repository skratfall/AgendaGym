package com.gym.agenda.di.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.presentation.state.GymListState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: GymListState,
    onDelete: (GymAppointment) -> Unit,
    onAddClick: () -> Unit
) {
    val serviceIcons: Map<String, ImageVector> = mapOf(
        "Personal Trainer" to Icons.Filled.FitnessCenter,
        "Yoga" to Icons.Filled.SelfImprovement,
        "Spinning" to Icons.Filled.PedalBike,
        "Nutrición" to Icons.Filled.Restaurant,
        "CrossFit" to Icons.Filled.SportsGymnastics
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏋️ Agenda Gym", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva Cita", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.EventBusy, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                    Spacer(Modifier.height(16.dp))
                    Text("No hay citas programadas", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Text("Toca + para agendar tu primera sesión", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        icon = serviceIcons[appointment.service] ?: Icons.Filled.Star,
                        onDelete = { onDelete(appointment) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: GymAppointment,
    icon: ImageVector,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val cal = Calendar.getInstance().apply {
        timeInMillis = appointment.dateMillis
        set(Calendar.HOUR_OF_DAY, appointment.timeHour)
        set(Calendar.MINUTE, appointment.timeMinute)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.clientName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text("${appointment.service} • ${dateFormat.format(cal.time)} • ${timeFormat.format(cal.time)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (appointment.notes.isNotBlank()) {
                    Text(appointment.notes, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f))
                }
            }
            TextButton(onClick = onDelete) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}