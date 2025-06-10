package com.isoft.weighttracker.feature.antropometria.ui

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.core.notifications.recordatorios.AlarmScheduler
import com.isoft.weighttracker.shared.UserViewModel
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.core.permissions.PermissionViewModel
import com.isoft.weighttracker.core.permissions.PermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAntropometricoScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    antropometriaViewModel: AntropometriaViewModel = viewModel(),
    onNuevoRegistro: (Antropometria?) -> Unit,
    permissionViewModel: PermissionViewModel,
    requestPermission: (String) -> Unit
) {
    val context = LocalContext.current
    val personaState = userViewModel.personaProfile.collectAsState()
    val persona = personaState.value

    val registros by antropometriaViewModel.registros.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var registroAEliminar by remember { mutableStateOf<Antropometria?>(null) }

    val formatoCompleto = remember { SimpleDateFormat("EEEE dd MMM yyyy HH:mm", Locale.getDefault()) }

    var horaLocal by remember { mutableIntStateOf(8) }
    var minutoLocal by remember { mutableIntStateOf(0) }
    var expandirFrecuencia by remember { mutableStateOf(false) }
    val frecuenciaActual = remember { mutableStateOf(persona?.frecuenciaMedicion ?: "diaria") }
    val recordatorioActivo = persona?.recordatorioActivo == true

    var intentoActivarRecordatorios by remember { mutableStateOf(false) }
    val notificationPermissionState by permissionViewModel.notificationPermission.collectAsState()

    var proximaNotificacion by remember {
        mutableStateOf(AlarmScheduler.calcularProximaFecha(frecuenciaActual.value, horaLocal, minutoLocal))
    }

    val timePicker = remember {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                horaLocal = selectedHour
                minutoLocal = selectedMinute
                userViewModel.updateHoraNotificacion(context, selectedHour, selectedMinute)
                proximaNotificacion = AlarmScheduler.calcularProximaFecha(frecuenciaActual.value, selectedHour, selectedMinute)
                AlarmScheduler.programarRepeticion(context, frecuenciaActual.value, selectedHour, selectedMinute)
                Toast.makeText(context, "⏰ Próximo recordatorio: ${formatoCompleto.format(proximaNotificacion.time)}", Toast.LENGTH_LONG).show()
            },
            horaLocal,
            minutoLocal,
            true
        )
    }

    val alarmManager = remember {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    val canScheduleExactAlarms = remember { mutableStateOf(true) }

    // Variables para secciones expandibles
    var mostrarConfiguracion by remember { mutableStateOf(false) }
    var mostrarRegistros by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userViewModel.loadPersonaProfile()
        antropometriaViewModel.cargarRegistros()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExactAlarms.value = alarmManager.canScheduleExactAlarms()
        }
    }

    LaunchedEffect(persona) {
        persona?.let {
            horaLocal = it.horaRecordatorio ?: 8
            minutoLocal = it.minutoRecordatorio ?: 0
            frecuenciaActual.value = it.frecuenciaMedicion ?: "diaria"
            proximaNotificacion = AlarmScheduler.calcularProximaFecha(frecuenciaActual.value, horaLocal, minutoLocal)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val puedeProglamarAlarmas = alarmManager.canScheduleExactAlarms()
                    canScheduleExactAlarms.value = puedeProglamarAlarmas

                    if (intentoActivarRecordatorios &&
                        puedeProglamarAlarmas &&
                        notificationPermissionState == PermissionState.GRANTED &&
                        !recordatorioActivo) {

                        userViewModel.cambiarEstadoRecordatorio(context, true)
                        proximaNotificacion = AlarmScheduler.calcularProximaFecha(
                            frecuenciaActual.value, horaLocal, minutoLocal
                        )
                        Toast.makeText(context, "✅ Recordatorios activados automáticamente", Toast.LENGTH_LONG).show()
                        intentoActivarRecordatorios = false
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    //AQUI HICE CAMBIO
    // Verificar si el PersonaProfile existe en Firestore
    if (persona == null) {
        var mostrandoMensaje by remember { mutableStateOf(true) }

        // Redirigir después de mostrar el mensaje por unos segundos
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(5000) // Esperar 2.5 segundos 2500
            navController.navigate("datosPersonales") {
                popUpTo("historialAntropometrico") { inclusive = true }
            }
        }

        // Mostrar mensaje explicativo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Para registrar mediciones antropométricas necesitas completar tu perfil personal primero",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Te redirigiremos en un momento...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        return
    }

// Verificar si los datos están completos (por si acaso)
    val datosIncompletos = persona.estatura <= 0f || persona.sexo.isBlank() || persona.edad <= 0

    if (datosIncompletos) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000) // Esperar 2 segundos
            navController.navigate("datosPersonales")
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Datos incompletos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Algunos datos personales están incompletos. Te redirigiremos para completarlos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        return
    }
    //TERMINA EL CAMBIO

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Antropométrico") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNuevoRegistro(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo registro")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Sección de configuración de recordatorios
            NotificationConfigSection(
                mostrarConfiguracion = mostrarConfiguracion,
                recordatorioActivo = recordatorioActivo,
                notificationPermissionState = notificationPermissionState,
                canScheduleExactAlarms = canScheduleExactAlarms.value,
                horaLocal = horaLocal,
                minutoLocal = minutoLocal,
                frecuenciaActual = frecuenciaActual.value,
                proximaNotificacion = proximaNotificacion,
                expandirFrecuencia = expandirFrecuencia,
                formatoCompleto = formatoCompleto,
                onToggleVisibility = { mostrarConfiguracion = !mostrarConfiguracion },
                onToggleRecordatorio = { nuevoEstado ->
                    if (nuevoEstado) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            notificationPermissionState != PermissionState.GRANTED
                        ) {
                            requestPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            intentoActivarRecordatorios = true
                            return@NotificationConfigSection
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            !alarmManager.canScheduleExactAlarms()
                        ) {
                            Toast.makeText(
                                context,
                                "⚠️ Activa el permiso de alarmas exactas",
                                Toast.LENGTH_LONG
                            ).show()
                            intentoActivarRecordatorios = true
                            return@NotificationConfigSection
                        }
                    }

                    userViewModel.cambiarEstadoRecordatorio(context, nuevoEstado)

                    if (nuevoEstado) {
                        proximaNotificacion = AlarmScheduler.calcularProximaFecha(
                            frecuenciaActual.value, horaLocal, minutoLocal
                        )
                        Toast.makeText(context, "✅ Recordatorio activado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ Recordatorio desactivado", Toast.LENGTH_SHORT).show()
                    }

                    if (!nuevoEstado) {
                        intentoActivarRecordatorios = false
                    }
                },
                onShowTimePicker = { timePicker.show() },
                onFrecuenciaChange = { expandirFrecuencia = !expandirFrecuencia },
                onFrecuenciaSelected = { opcion ->
                    frecuenciaActual.value = opcion
                    expandirFrecuencia = false
                    userViewModel.updateFrecuenciaMedicion(opcion)
                    proximaNotificacion = AlarmScheduler.calcularProximaFecha(opcion, horaLocal, minutoLocal)
                    AlarmScheduler.programarRepeticion(context, opcion, horaLocal, minutoLocal)
                    Toast.makeText(
                        context,
                        "📆 Próximo recordatorio: ${formatoCompleto.format(proximaNotificacion.time)}",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onActivateAlarms = {
                    intentoActivarRecordatorios = true
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de registros
            RecordsSection(
                mostrarRegistros = mostrarRegistros,
                registros = registros,
                formatoCompleto = formatoCompleto,
                onToggleVisibility = { mostrarRegistros = !mostrarRegistros },
                onEditRegistro = onNuevoRegistro,
                onDeleteRegistro = { registroAEliminar = it }
            )
        }

        // Diálogo de confirmación de eliminación
        registroAEliminar?.let { registro ->
            DeleteConfirmationDialog(
                registro = registro,
                onConfirm = {
                    registroAEliminar = null
                    scope.launch {
                        val eliminado = antropometriaViewModel.eliminarRegistro(registro.id ?: "")
                        if (eliminado) {
                            val result = snackbarHostState.showSnackbar(
                                message = "Registro eliminado",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                antropometriaViewModel.guardarRegistroNuevo(
                                    peso = registro.peso,
                                    cintura = registro.cintura,
                                    cuello = registro.cuello,
                                    cadera = registro.cadera,
                                    estatura = persona.estatura,
                                    sexo = persona.sexo,
                                    edad = persona.edad
                                ) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Registro restaurado ✔️")
                                    }
                                }
                            }
                        } else {
                            snackbarHostState.showSnackbar("Error al eliminar registro")
                        }
                    }
                },
                onDismiss = { registroAEliminar = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationConfigSection(
    mostrarConfiguracion: Boolean,
    recordatorioActivo: Boolean,
    notificationPermissionState: PermissionState,
    canScheduleExactAlarms: Boolean,
    horaLocal: Int,
    minutoLocal: Int,
    frecuenciaActual: String,
    proximaNotificacion: Calendar,
    expandirFrecuencia: Boolean,
    formatoCompleto: SimpleDateFormat,
    onToggleVisibility: () -> Unit,
    onToggleRecordatorio: (Boolean) -> Unit,
    onShowTimePicker: () -> Unit,
    onFrecuenciaChange: () -> Unit,
    onFrecuenciaSelected: (String) -> Unit,
    onActivateAlarms: () -> Unit
) {
    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recordatorioActivo)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (recordatorioActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recordatorios de medición",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (recordatorioActivo) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = recordatorioActivo,
                    onCheckedChange = onToggleRecordatorio
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (mostrarConfiguracion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarConfiguracion) {
                Spacer(modifier = Modifier.height(12.dp))

                // Mostrar botón si falta permiso de alarmas exactas
                if (notificationPermissionState == PermissionState.GRANTED &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !canScheduleExactAlarms
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "El permiso de alarmas exactas está desactivado",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onActivateAlarms,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Activar alarmas exactas")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (recordatorioActivo) {
                    // Selector de frecuencia
                    ExposedDropdownMenuBox(
                        expanded = expandirFrecuencia,
                        onExpandedChange = { onFrecuenciaChange() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = frecuenciaActual.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            label = { Text("Frecuencia de medición") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirFrecuencia) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandirFrecuencia,
                            onDismissRequest = { onFrecuenciaChange() }
                        ) {
                            listOf("diaria", "semanal", "quincenal", "mensual").forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.replaceFirstChar { it.uppercase() }) },
                                    onClick = { onFrecuenciaSelected(opcion) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de hora
                    OutlinedButton(
                        onClick = onShowTimePicker,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hora del recordatorio: %02d:%02d".format(horaLocal, minutoLocal))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Información del próximo recordatorio
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Próximo: ${formatoCompleto.format(proximaNotificacion.time)}",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsSection(
    mostrarRegistros: Boolean,
    registros: List<Antropometria>,
    formatoCompleto: SimpleDateFormat,
    onToggleVisibility: () -> Unit,
    onEditRegistro: (Antropometria) -> Unit,
    onDeleteRegistro: (Antropometria) -> Unit
) {
    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registros antropométricos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                if (registros.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("${registros.size}")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (mostrarRegistros) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarRegistros) {
                if (registros.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No hay registros aún. ¡Comienza con el botón +!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(registros) { registro ->
                            AntropometriaItem(
                                registro = registro,
                                formatoCompleto = formatoCompleto,
                                onEdit = onEditRegistro,
                                onDelete = onDeleteRegistro
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    registro: Antropometria,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("¿Eliminar registro?") },
        text = {
            Text("¿Seguro que deseas eliminar este registro antropométrico? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AntropometriaItem(
    registro: Antropometria,
    formatoCompleto: SimpleDateFormat,
    onEdit: (Antropometria) -> Unit,
    onDelete: (Antropometria) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📅 ${formatoCompleto.format(Date(registro.fecha))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            "⚖️ ${registro.peso} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "📊 IMC: %.1f".format(registro.imc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    RegistroItem("Peso", "${registro.peso} kg")
                    RegistroItem("Cintura", "${registro.cintura} cm")
                    RegistroItem("Cuello", "${registro.cuello} cm")
                    registro.cadera?.let { RegistroItem("Cadera", "$it cm") }
                    RegistroItem("IMC", "%.2f".format(registro.imc))
                    RegistroItem("Grasa corporal", "%.2f %%".format(registro.porcentajeGrasa))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onEdit(registro) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete(registro) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistroItem(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}