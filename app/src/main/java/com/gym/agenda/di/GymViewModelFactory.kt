package com.gym.agenda.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.di.viewmodel.AddEditViewModel
import com.gym.agenda.di.viewmodel.GymListViewModel

class GymViewModelFactory(
    private val appointmentRepo: AppointmentRepository,
    private val authRepo: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(GymListViewModel::class.java) ->
            GymListViewModel(appointmentRepo, authRepo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
