package com.gym.agenda.di.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gym.agenda.data.repository.GymRepository
import com.gym.agenda.di.GymViewModelFactory
import com.gym.agenda.di.screens.DashboardScreen
import com.gym.agenda.presentation.screens.AddEditScreen
import com.gym.agenda.presentation.viewmodel.AddEditViewModel
import com.gym.agenda.presentation.viewmodel.GymListViewModel

object GymRoutes {
    const val DASHBOARD = "dashboard"
    const val ADD_APPOINTMENT = "add_appointment"
}

@Composable
fun GymNavHost(repository: GymRepository) {
    val navController = rememberNavController()
    val factory = GymViewModelFactory(repository)

    NavHost(navController = navController, startDestination = GymRoutes.DASHBOARD) {
        composable(GymRoutes.DASHBOARD) {
            val vm: GymListViewModel = viewModel(factory = factory)
            DashboardScreen(
                state = vm.state.collectAsStateWithLifecycle().value,
                onDelete = { appointment -> vm.delete(appointment) },
                onAddClick = { navController.navigate(GymRoutes.ADD_APPOINTMENT) }
            )
        }
        composable(GymRoutes.ADD_APPOINTMENT) {
            val vm: AddEditViewModel = viewModel(factory = factory)
            AddEditScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}