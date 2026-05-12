package com.gym.agenda.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gym.agenda.R
import timber.log.Timber

class AgendaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("📩 Mensaje FCM recibido de: ${remoteMessage.from}")

        // Mostrar notificación si el mensaje trae datos o notificación
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Agenda Gym", it.body ?: "")
        } ?: remoteMessage.data.let { data ->
            if (data.isNotEmpty()) {
                showNotification(data["title"] ?: "Agenda Gym", data["message"] ?: "")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("🔑 Nuevo token FCM: $token")
        // El token se guarda en Firestore desde el AuthViewModel al iniciar sesión
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fcm_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Importantes",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
