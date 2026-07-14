package com.gym.agenda.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.worker.NotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleAppointmentNotification(appointment: GymAppointment) {
        val now = System.currentTimeMillis()
        val notificationTime = appointment.dateTimeMillis - (30 * 60000L) // 30 minutos antes
        val delay = notificationTime - now

        if (delay > 0) {
            Timber.d("⏰ Programando notificación para la cita ${appointment.id} en ${delay / 60000} minutos")
            val data = Data.Builder()
                .putString("title", "Recordatorio de Cita")
                .putString("message", "Tu sesión de ${appointment.service} comienza en 30 minutos.")
                .build()

            val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("notification_${appointment.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                notificationRequest
            )
        } else {
            Timber.w("⚠️ No se pudo programar la notificación: la cita es en menos de 30 minutos o ya pasó")
        }
    }

    fun showImmediateNotification(title: String, message: String) {
        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(notificationRequest)
    }

    fun cancelAppointmentNotification(appointmentId: String) {
        Timber.d("🚫 Cancelando notificación para la cita $appointmentId")
        WorkManager.getInstance(context).cancelUniqueWork("notification_$appointmentId")
    }
}
