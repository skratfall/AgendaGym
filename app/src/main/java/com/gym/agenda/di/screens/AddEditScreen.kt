package com.gym.agenda.presentation.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gym.agenda.presentation.viewmodel.AddEditViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: AddEditViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.form.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { if (it) onNavigateBack() }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("📝 Nueva Cita") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = formState.clientName,
                onValueChange = { viewModel.setClientName(it) },
                label = { Text("Nombre del Cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.service,
                onValueChange = { viewModel.setService(it) },
                label = { Text("Servicio (Personal Trainer, Yoga, Spinning...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker(context) { viewModel.setDate(it) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (formState.dateMillis > 0)
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(formState.dateMillis))
                        else "Fecha"
                    )
                }

                OutlinedButton(
                    onClick = { showTimePicker(context) { h, m -> viewModel.setTime(h, m) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(String.format("%02d:%02d", formState.timeHour, formState.timeMinute))
                }
            }

            OutlinedTextField(
                value = formState.notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = viewModel::cancel, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = viewModel::save,
                    enabled = formState.isValid,
                    modifier = Modifier.weight(1f)
                ) { Text("Guardar Cita") }
            }
        }
    }
}

private fun showDatePicker(context: android.content.Context, onSelected: (Long) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(
        context, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            onSelected(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(context: android.content.Context, onSelected: (Int, Int) -> Unit) {
    val cal = Calendar.getInstance()
    TimePickerDialog(
        context, { _, h, m -> onSelected(h, m) },
        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
    ).show()
}