package com.gym.agenda.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.gym.agenda.di.viewmodel.GymListViewModel
import com.gym.agenda.utils.NotificationScheduler

class GymViewModelFactory(
    private val appointmentRepo: AppointmentRepository,
    private val authRepo: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(GymListViewModel::class.java) ->
            GymListViewModel(appointmentRepo, authRepo, notificationScheduler) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
