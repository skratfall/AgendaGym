package com.gym.agenda.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gym.agenda.di.GymViewModelFactory
import com.gym.agenda.data.repository.GymRepository
import com.gym.agenda.presentation.screens.AddEditScreen
import com.gym.agenda.presentation.screens.GymListScreen
import com.gym.agenda.presentation.viewmodel.GymListViewModel

sealed class GymScreen(val route: String) {
    object List : GymScreen("list")
    object Add : GymScreen("add")
}

@Composable
fun GymNavigation(repo: GymRepository) {
    val nav = rememberNavController()
    val factory = GymViewModelFactory(repo)

    NavHost(nav, startDestination = GymScreen.List.route) {
        composable(GymScreen.List.route) {
            val vm: GymListViewModel = viewModel(factory = factory)
            GymListScreen(
                state = vm.state.collectAsState().value,
                onDelete = vm::delete,
                onAdd = { nav.navigate(GymScreen.Add.route) }
            )
        }
        composable(GymScreen.Add.route) {
            val vm: com.gym.agenda.presentation.viewmodel.AddEditViewModel = viewModel(factory = factory)
            AddEditScreen(viewModel = vm, onNavigateBack = { nav.popBackStack() })
        }
    }
}