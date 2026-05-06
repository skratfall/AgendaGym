package com.gym.agenda.data.model

sealed class NotificationEvent(
    val message: String,
    val type: NotificationType
) {
    data class Success(val successMessage: String) : NotificationEvent(successMessage, NotificationType.SUCCESS)
    data class Error(val errorMessage: String) : NotificationEvent(errorMessage, NotificationType.ERROR)
    data class Info(val infoMessage: String) : NotificationEvent(infoMessage, NotificationType.INFO)
}

enum class NotificationType {
    SUCCESS, ERROR, INFO
}
