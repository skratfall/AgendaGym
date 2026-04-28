package com.gym.agenda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.data.repository.GymRepository
import com.gym.agenda.presentation.state.FormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditViewModel(private val repo: GymRepository) : ViewModel() {
    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack.asStateFlow()

    fun setClientName(name: String) = updateForm { copy(clientName = name) }
    fun setService(service: String) = updateForm { copy(service = service) }
    fun setDate(millis: Long) = updateForm { copy(dateMillis = millis) }
    fun setTime(hour: Int, minute: Int) = updateForm { copy(timeHour = hour, timeMinute = minute) }
    fun setNotes(notes: String) = updateForm { copy(notes = notes) }

    private fun updateForm(update: FormState.() -> FormState) {
        _form.update { current ->
            val updated = current.update()
            updated.copy(isValid = validate(updated))
        }
    }

    private fun validate(s: FormState): Boolean =
        s.clientName.isNotBlank() && s.service.isNotBlank() && s.dateMillis > 0

    fun save() {
        if (!form.value.isValid) return
        viewModelScope.launch {
            val f = form.value
            repo.save(GymAppointment(
                clientName = f.clientName, service = f.service,
                dateMillis = f.dateMillis, timeHour = f.timeHour,
                timeMinute = f.timeMinute, notes = f.notes
            ))
            _navigateBack.value = true
        }
    }

    fun cancel() { _navigateBack.value = true }
}