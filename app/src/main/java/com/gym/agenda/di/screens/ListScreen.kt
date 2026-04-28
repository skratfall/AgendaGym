package com.gym.agenda.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.presentation.state.GymListState
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymListScreen(
    state: GymListState,
    onDelete: (GymAppointment) -> Unit,
    onAdd: () -> Unit
) {
    val services = mapOf("Personal Trainer" to Icons.Filled.FitnessCenter, "Yoga" to Icons.Filled.SelfImprovement, "Spinning" to Icons.Filled.PedalBike, "Nutrición" to Icons.Filled.Restaurant)

    Scaffold(
        topBar = { TopAppBar(title = { Text("🏋️‍♂️ Agenda Gym", color = MaterialTheme.colorScheme.onPrimary) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cita")
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay citas programadas", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            else -> LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.id }) { apt ->
                    GymCard(apt, onDelete, services)
                }
            }
        }
    }
}

@Composable
fun GymCard(apt: GymAppointment, onDelete: (GymAppointment) -> Unit, services: Map<String, androidx.compose.ui.graphics.vector.ImageVector>) {
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(apt.dateMillis)
    val time = String.format("%02d:%02d", apt.timeHour, apt.timeMinute)
    val icon = services[apt.service] ?: Icons.Default.Star

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(apt.clientName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text("${apt.service} • $date $time", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (apt.notes.isNotBlank()) Text(apt.notes, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { onDelete(apt) }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
        }
    }
}