package com.gym.agenda.di.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.Crossfade
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gym.agenda.di.viewmodel.AdminViewModel
import com.gym.agenda.di.viewmodel.AuthViewModel
import com.valentinilk.shimmer.shimmer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val adminUiState by adminViewModel.uiState.collectAsState()

    // Refrescar datos cuando la pantalla se compone (al regresaro desde AdminAppointmentsScreen)
    LaunchedEffect(Unit) {
        adminViewModel.refreshAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = adminUiState.isLoading,
            label = "AdminShimmerTransition",
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    // Header
                    if (isLoading) {
                        ShimmerAdminHeader()
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Administración",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gestiona usuarios y citas del gimnasio",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isLoading) {
                            ShimmerStatCard(modifier = Modifier.weight(1f))
                            ShimmerStatCard(modifier = Modifier.weight(1f))
                        } else {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "Ingresos",
                                value = "$${String.format(Locale.getDefault(), "%.2f", adminUiState.totalRevenue)}",
                                icon = Icons.Default.AttachMoney,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "Citas",
                                value = "${adminUiState.totalAppointments}",
                                icon = Icons.Default.EventAvailable,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Popular Service Card
                    if (isLoading) {
                        ShimmerStatCard(modifier = Modifier.fillMaxWidth())
                    } else {
                        StatCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Servicio Popular",
                            value = adminUiState.popularService,
                            icon = Icons.Default.Star,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isLoading) {
                        Text(
                            text = "Gestión",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (isLoading) {
                    // Mostrar placeholders de menú mientras carga
                    items(3) {
                        ShimmerMenuCard()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    item {
                        AdminMenuCard(
                            icon = Icons.Default.People,
                            title = "Usuarios",
                            description = "Gestionar usuarios y permisos",
                            onClick = onNavigateToUsers
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AdminMenuCard(
                            icon = Icons.Default.Event,
                            title = "Todas las Citas",
                            description = "Ver y gestionar todas las citas",
                            onClick = onNavigateToAppointments
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AdminMenuCard(
                            icon = Icons.Default.Assessment,
                            title = "Reportes",
                            description = "Estadísticas y reportes",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerAdminHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Placeholder para el título
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder para el subtítulo
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp)
            )
        }
    }
}

@Composable
private fun ShimmerStatCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shimmer(),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Placeholder para el ícono
            Box(
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder para el título
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder para el valor
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
            )
        }
    }
}

@Composable
private fun ShimmerMenuCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder para el ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Placeholder para el título
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(18.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Placeholder para la descripción
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                )
            }

            // Placeholder para el chevron
            Box(
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navegar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}