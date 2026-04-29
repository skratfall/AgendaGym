package com.gym.agenda.di.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gym.agenda.data.model.UserRole
import com.gym.agenda.di.viewmodel.AuthViewModel
import com.gym.agenda.di.screens.*
import com.gym.agenda.viewmodel.AddEditViewModel
import com.gym.agenda.viewmodel.GymListViewModel

@Composable
fun GymNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val userRole = currentUser?.role

    val startDestination = when {
        currentUser == null -> GymNav.Login.route
        userRole == UserRole.ADMIN -> GymNav.AdminDashboard.route
        else -> GymNav.Dashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flows
        composable(GymNav.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(GymNav.Dashboard.route) {
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
                onRegisterSuccess = {
                    navController.navigate(GymNav.Dashboard.route) {
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
            DashboardScreen(
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
            ListScreen(
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
                }
            )
        }
    }
}