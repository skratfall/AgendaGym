package com.gym.agenda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.repository.GymRepository
import com.gym.agenda.presentation.state.GymListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GymListViewModel(private val repo: GymRepository) : ViewModel() {
    private val _state = MutableStateFlow(GymListState())
    val state: StateFlow<GymListState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                repo.appointments.collect { list ->
                    _state.value = _state.value.copy(items = list, isLoading = false, error = null)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Error al cargar citas")
            }
        }
    }

    fun delete(appointment: com.gym.agenda.data.model.GymAppointment) {
        viewModelScope.launch { repo.remove(appointment) }
    }
}