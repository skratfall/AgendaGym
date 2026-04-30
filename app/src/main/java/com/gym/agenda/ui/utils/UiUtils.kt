package com.gym.agenda.ui.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.agenda.data.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.util.TimeZone

object UiUtils {
    private val colombiaTimeZone = TimeZone.getTimeZone("America/Bogota")

    @Composable
    fun StatusChip(status: AppointmentStatus) {
        val (label, color) = when (status) {
            AppointmentStatus.PENDING -> "Pendiente" to MaterialTheme.colorScheme.tertiary
            AppointmentStatus.CONFIRMED -> "Confirmada" to MaterialTheme.colorScheme.primary
            AppointmentStatus.CANCELLED -> "Cancelada" to MaterialTheme.colorScheme.error
            else -> "Completada" to MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            color = color.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = color
            )
        }
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = colombiaTimeZone
        return sdf.format(Date(millis))
    }

    fun formatTime(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun addToCalendar(context: Context, service: String, startTimeMillis: Long, notes: String = "") {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTimeMillis + 3600000L) // 1 hora
            .putExtra(CalendarContract.Events.TITLE, "Cita Gym: $service")
            .putExtra(CalendarContract.Events.DESCRIPTION, notes)
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        
        context.startActivity(intent)
    }
}
