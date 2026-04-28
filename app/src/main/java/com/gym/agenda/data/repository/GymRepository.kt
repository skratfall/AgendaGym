package com.gym.agenda.data.repository

import com.gym.agenda.data.database.AppointmentDao
import com.gym.agenda.data.model.GymAppointment
import kotlinx.coroutines.flow.Flow

class GymRepository(private val dao: AppointmentDao) {
    val appointments: Flow<List<GymAppointment>> = dao.getAll()
    suspend fun save(a: GymAppointment) = dao.insert(a)
    suspend fun remove(a: GymAppointment) = dao.delete(a)
}