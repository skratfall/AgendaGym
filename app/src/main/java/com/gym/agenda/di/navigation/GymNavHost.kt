package com.gym.agenda.di.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.di.viewmodel.AuthViewModel
import com.gym.agenda.di.screens.*
import com.gym.agenda.di.viewmodel.GymListViewModel
import kotlinx.coroutines.flow.first

@Composable
fun GymNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // 1. Observamos el usuario actual
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

    // 2. Estado para fijar la ruta inicial y evitar que el NavHost se recree durante la sesión
    var finalStartDestination by remember { mutableStateOf<String?>(null) }

    // 3. Determinamos la ruta inicial basándonos en el primer estado disponible
    LaunchedEffect(Unit) {
        val user = authViewModel.currentUser.first()
        finalStartDestination = when {
            user == null -> GymNav.Login.route
            user.role == UserRole.ADMIN -> GymNav.AdminDashboard.route
            else -> GymNav.Dashboard.route
        }
    }

    // 4. Mientras se determina el destino (Splash/Loading)
    if (finalStartDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = finalStartDestination!!
    ) {
        // Auth flows
        composable(GymNav.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { user ->
                    val destination = if (user.role == UserRole.ADMIN) {
                        GymNav.AdminDashboard.route
                    } else {
                        GymNav.Dashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(GymNav.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(GymNav.Register.route)
                }
            )
        }

        composable(GymNav.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { user ->
                    val destination = if (user.role == UserRole.ADMIN) {
                        GymNav.AdminDashboard.route
                    } else {
                        GymNav.Dashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(GymNav.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // User flows
        composable(GymNav.Dashboard.route) {
            val listViewModel: GymListViewModel = hiltViewModel()
            DashboardScreen(
                authViewModel = authViewModel,
                listViewModel = listViewModel,
                onNavigateToAppointments = {
                    navController.navigate(GymNav.AppointmentList.route)
                },
                onNavigateToNewAppointment = {
                    navController.navigate(GymNav.AddEditAppointment.createRoute())
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(GymNav.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        composable(GymNav.AppointmentList.route) {
            val dashboardBackStackEntry = remember(navController.currentBackStackEntry) { navController.getBackStackEntry(GymNav.Dashboard.route) }
            val listViewModel: GymListViewModel = hiltViewModel(dashboardBackStackEntry)
            ListScreen(
                viewModel = listViewModel,
                onNavigateToAddEdit = { appointmentId ->
                    navController.navigate(
                        GymNav.AddEditAppointment.createRoute(appointmentId)
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = GymNav.AddEditAppointment.route,
            arguments = listOf(
                navArgument(NavArgs.APPOINTMENT_ID) {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString(NavArgs.APPOINTMENT_ID)

            AddEditScreen(
                appointmentId = if (appointmentId == "new") null else appointmentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin flows
        composable(GymNav.AdminDashboard.route) {
            AdminDashboardScreen(
                authViewModel = authViewModel,
                onNavigateToUsers = {
                    navController.navigate(GymNav.AdminUsers.route)
                },
                onNavigateToAppointments = {
                    navController.navigate(GymNav.AdminAppointments.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(GymNav.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(GymNav.AdminUsers.route) {
            AdminUsersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(GymNav.AdminAppointments.route) {
            AdminAppointmentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { appointmentId ->
                    navController.navigate(
                        GymNav.AddEditAppointment.createRoute(appointmentId)
                    )
                }
            )
        }
    }
}