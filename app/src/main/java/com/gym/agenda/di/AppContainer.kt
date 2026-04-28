package com.gym.agenda.di
import android.content.Context
import com.gym.agenda.data.database.AppDatabase
import com.gym.agenda.data.repository.GymRepository
class AppContainer(ctx: Context) {
    private val db = AppDatabase.getInstance(ctx)
    val repo = GymRepository(db.appointmentDao())
}