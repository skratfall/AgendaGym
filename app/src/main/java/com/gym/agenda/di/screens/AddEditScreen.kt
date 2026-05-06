package com.gym.agenda.di.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.gym.agenda.data.model.AppointmentStatus
import com.gym.agenda.data.model.GymAppointment
import com.gym.agenda.di.viewmodel.AddEditViewModel
import com.gym.agenda.ui.utils.*
import com.gym.agenda.R
import com.gym.agenda.utils.GymServices
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    appointmentId: String?,
    onNavigateBack: () -> Unit,
    onAppointmentSaved: () -> Unit = {},
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notification by viewModel.notification.collectAsState()

    var clientName by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var timeHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var timeMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var notes by remember { mutableStateOf("") }

    var expandedService by remember { mutableStateOf(false) }
    val services = GymServices.ALL_SERVICES

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (dateMillis > 0) dateMillis else System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = timeHour,
        initialMinute = timeMinute
    )

    // Cargar datos si es edición
    LaunchedEffect(uiState.appointment) {
        uiState.appointment?.let { appointment ->
            clientName = appointment.clientName
            service = appointment.service
            price = appointment.price.toString()
            dateMillis = appointment.dateMillis
            timeHour = appointment.timeHour
            timeMinute = appointment.timeMinute
            notes = appointment.notes
        }
    }

    // Navegar al guardar
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
            onAppointmentSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (appointmentId == null) "Nueva Cita" else "Editar Cita")
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Nombre del Cliente (Solo Admin puede editarlo o si es nueva cita?)
                // En este caso lo dejamos para que el usuario ponga su nombre o se autocompleta
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Servicio - Exposed Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = expandedService,
                    onExpandedChange = { expandedService = !expandedService },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = service,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Servicio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedService) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedService,
                        onDismissRequest = { expandedService = false }
                    ) {
                        services.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    service = item
                                    expandedService = false
                                    // Precios sugeridos
                                    if (price.isEmpty() || price == "0.0") {
                                        price = when(item) {
                                            "Entrenamiento Personal" -> "50.0"
                                            "Yoga" -> "30.0"
                                            "Crossfit" -> "40.0"
                                            else -> "25.0"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Precio
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha
                OutlinedTextField(
                    value = if (dateMillis > 0) UiUtils.formatDate(dateMillis) else "",
                    onValueChange = { },
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Event, "Seleccionar fecha")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hora
                OutlinedTextField(
                    value = UiUtils.formatTime(timeHour, timeMinute),
                    onValueChange = { },
                    label = { Text("Hora") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.AccessTime, "Seleccionar hora")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { error ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LottieAnimation(
                                spec = LottieCompositionSpec.RawRes(R.raw.error_animation), // Asumir que hay un archivo error_animation.json en res/raw
                                size = 100.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val appointment = GymAppointment(
                            id = appointmentId ?: "",
                            clientName = clientName,
                            service = service,
                            price = price.toDoubleOrNull() ?: 0.0,
                            dateMillis = dateMillis,
                            timeHour = timeHour,
                            timeMinute = timeMinute,
                            notes = notes,
                            status = if (appointmentId == null)
                                AppointmentStatus.PENDING
                            else
                                uiState.appointment?.status ?: AppointmentStatus.PENDING
                        )
                        viewModel.saveAppointment(appointment)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading &&
                            clientName.isNotBlank() &&
                            service.isNotBlank() &&
                            dateMillis > 0 &&
                            (appointmentId == null || uiState.appointment?.status == AppointmentStatus.PENDING)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        val isConfirmed = uiState.appointment?.status == AppointmentStatus.CONFIRMED
                        Text(
                            if (appointmentId == null) "Agendar Cita"
                            else if (isConfirmed) "Cita Confirmada (No editable)"
                            else "Guardar Cambios",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateMillis = datePickerState.selectedDateMillis ?: dateMillis
                        showDatePicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // TimePicker Dialog
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        timeHour = timePickerState.hour
                        timeMinute = timePickerState.minute
                        showTimePicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }

        // Notificación de feedback
        ActionFeedbackSnackbar(
            notification = notification,
            onDismiss = { viewModel.dismissNotification() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}