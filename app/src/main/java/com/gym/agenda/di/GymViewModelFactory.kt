package com.gym.agenda.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gym.agenda.data.repository.GymRepository
import com.gym.agenda.presentation.viewmodel.AddEditViewModel
import com.gym.agenda.presentation.viewmodel.GymListViewModel

class GymViewModelFactory(private val repo: GymRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(GymListViewModel::class.java) ->
            GymListViewModel(repo) as T
        modelClass.isAssignableFrom(AddEditViewModel::class.java) ->
            AddEditViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel")
    }
}