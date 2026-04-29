package com.gym.agenda.di.navigation

sealed class GymNav(val route: String) {
    object Login : GymNav("login")
    object Register : GymNav("register")
    object Dashboard : GymNav("dashboard")
    object AppointmentList : GymNav("appointments")
    object AddEditAppointment : GymNav("appointments/add_edit/{appointmentId}") {
        fun createRoute(appointmentId: String? = null) =
            "appointments/add_edit/${appointmentId ?: "new"}"
    }
    object AdminDashboard : GymNav("admin/dashboard")
    object AdminUsers : GymNav("admin/users")
    object AdminAppointments : GymNav("admin/appointments")
}

object NavArgs {
    const val APPOINTMENT_ID = "appointmentId"
    const val USER_ID = "userId"
}