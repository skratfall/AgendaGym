package com.gym.agenda.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gym.agenda.data.model.NotificationEvent
import com.gym.agenda.data.model.NotificationType
import kotlinx.coroutines.delay

@Composable
fun ActionFeedbackSnackbar(
    notification: NotificationEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(notification) {
        if (notification != null) {
            isVisible = true
            delay(3500)
            isVisible = false
            delay(400)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && notification != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        notification?.let { notif ->
            val backgroundColor = when (notif.type) {
                NotificationType.SUCCESS -> MaterialTheme.colorScheme.primary
                NotificationType.ERROR -> MaterialTheme.colorScheme.error
                NotificationType.INFO -> MaterialTheme.colorScheme.secondary
            }

            val icon = when (notif.type) {
                NotificationType.SUCCESS -> Icons.Default.CheckCircle
                NotificationType.ERROR -> Icons.Default.Error
                NotificationType.INFO -> Icons.Default.Info
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = notif.message,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    IconButton(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


