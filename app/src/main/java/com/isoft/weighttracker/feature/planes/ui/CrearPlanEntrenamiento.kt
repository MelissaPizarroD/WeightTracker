package com.isoft.weighttracker.feature.planes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.feature.planes.model.*
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlanEntrenamientoScreen(
    navController: NavController,
    solicitud: SolicitudPlan,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel(),
    antropometriaViewModel: AntropometriaViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    val registros by antropometriaViewModel.registrosDeUsuario.collectAsState()
    val ultimoRegistro = registros.firstOrNull()

    var nombrePlan by remember { mutableStateOf("") }
    var objetivo by remember { mutableStateOf(solicitud.objetivoEntrenamiento) }
    var duracionSemanas by remember { mutableStateOf("8") }
    var frecuenciaSemanal by remember { mutableStateOf(solicitud.disponibilidadSemanal) }
    var sesiones by remember { mutableStateOf(listOf<SesionEntrenamiento>()) }

    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadUser()
        antropometriaViewModel.cargarRegistrosDeUsuario(solicitud.usuarioId)
    }

    mensaje?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            planesViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Plan de Entrenamiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            currentUser?.let { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("👤 Nombre: ${usuario.name}", fontWeight = FontWeight.Bold)
                        Text("📧 Email: ${usuario.email ?: "No disponible"}")
                    }
                }
            }

            InfoUsuarioSimpleCard(solicitud)

            if (ultimoRegistro != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📏 Último Registro Antropométrico", fontWeight = FontWeight.Bold)
                        Text("📅 Fecha: ${SimpleDateFormat("dd/MM/yyyy").format(ultimoRegistro.fecha)}")
                        Text("⚖️ Peso: ${ultimoRegistro.peso} kg")
                        Text("📐 IMC: ${String.format("%.1f", ultimoRegistro.imc)}")
                        Text("💧 % Grasa Corporal: ${String.format("%.1f", ultimoRegistro.porcentajeGrasa)}%")
                        Text("📏 Cintura: ${ultimoRegistro.cintura} cm")
                        Text("📏 Cuello: ${ultimoRegistro.cuello} cm")
                        if (ultimoRegistro.cadera != null) {
                            Text("🦵 Cadera: ${ultimoRegistro.cadera} cm")
                        }
                    }
                }
            }else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("⚠️ Sin registros antropométricos recientes", fontWeight = FontWeight.Bold)
                        Text("Este usuario aún no tiene datos de peso, IMC o medidas corporales registrados.")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Detalles del Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = nombrePlan,
                        onValueChange = { nombrePlan = it },
                        label = { Text("Nombre del Plan") },
                        placeholder = { Text("Ej: Plan Fuerza Intermedio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = objetivo,
                        onValueChange = {},
                        label = { Text("Objetivo") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = duracionSemanas,
                        onValueChange = { if (it.all { c -> c.isDigit() }) duracionSemanas = it },
                        label = { Text("Duración (semanas)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = frecuenciaSemanal,
                        onValueChange = {},
                        label = { Text("Frecuencia Semanal") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SesionesSection(sesiones = sesiones, onSesionesChange = { sesiones = it })

            Button(
                onClick = { mostrarDialogoConfirmacion = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nombrePlan.isNotBlank() && sesiones.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Plan de Entrenamiento")
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("💪 Confirmar Plan de Entrenamiento", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¿Estás seguro de enviar este plan de entrenamiento a:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("👤 ${solicitud.nombreUsuario}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📋 Plan: $nombrePlan")
                    Text("⏱️ Duración: $duracionSemanas semanas")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        val planEntrenamiento = PlanEntrenamiento(
                            usuarioId = solicitud.usuarioId,
                            profesionalId = currentUser?.uid ?: "",
                            nombreProfesional = currentUser?.name ?: "",
                            nombrePlan = nombrePlan,
                            objetivo = objetivo,
                            duracionSemanas = duracionSemanas.toIntOrNull() ?: 8,
                            frecuenciaSemanal = frecuenciaSemanal,
                            sesiones = sesiones
                        )
                        planesViewModel.crearPlanEntrenamiento(
                            solicitud.id,
                            planEntrenamiento,
                            onSuccess = { navController.popBackStack() },
                            onError = { }
                        )
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Continúa con InfoUsuarioSimpleCard, SesionesSection, SesionDialog, EjercicioDialog y DropdownField...
@Composable
fun InfoUsuarioSimpleCard(solicitud: SolicitudPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Información del Cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("👤 ${solicitud.nombreUsuario}")
            Text("🎯 Objetivo: ${solicitud.objetivoEntrenamiento}")
            Text("💪 Experiencia: ${solicitud.experienciaPrevia}")
            Text("📅 Disponibilidad: ${solicitud.disponibilidadSemanal}")
            Text("🏋️ Equipamiento: ${solicitud.equipamientoDisponible.joinToString(", ")}")
            if (solicitud.descripcion.isNotBlank()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("📝 Descripción: ${solicitud.descripcion}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EjercicioDialog(
    ejercicio: Ejercicio?,
    onDismiss: () -> Unit,
    onConfirm: (Ejercicio) -> Unit
) {
    var nombre by remember { mutableStateOf(ejercicio?.nombre ?: "") }

    val musculos = listOf("Pecho", "Espalda", "Piernas", "Bíceps", "Tríceps", "Hombros", "Core", "Otros")
    var musculoSeleccionado by remember { mutableStateOf(ejercicio?.musculoTrabajado ?: musculos.first()) }
    var musculoCustom by remember { mutableStateOf("") }
    val musculoFinal = if (musculoSeleccionado == "Otros") musculoCustom else musculoSeleccionado

    var series by remember { mutableStateOf(ejercicio?.series ?: "") }
    var repeticiones by remember { mutableStateOf(ejercicio?.repeticiones ?: "") }
    var descanso by remember { mutableStateOf(ejercicio?.descanso ?: "") }
    var notas by remember { mutableStateOf(ejercicio?.observaciones ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ejercicio == null) "Nuevo Ejercicio" else "Editar Ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Ejercicio") }, modifier = Modifier.fillMaxWidth())

                DropdownField("Músculo Trabajado", musculos, musculoSeleccionado) {
                    musculoSeleccionado = it
                }

                if (musculoSeleccionado == "Otros") {
                    OutlinedTextField(
                        value = musculoCustom,
                        onValueChange = { musculoCustom = it },
                        label = { Text("Músculo Personalizado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(series, { if (it.all(Char::isDigit)) series = it }, label = { Text("Series") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(repeticiones, { repeticiones = it }, label = { Text("Repeticiones") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(descanso, { if (it.all(Char::isDigit)) descanso = it }, label = { Text("Descanso (minutos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notas, { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    Ejercicio(
                        nombre = nombre,
                        musculoTrabajado = musculoFinal,
                        series = series,
                        repeticiones = repeticiones,
                        descanso = descanso,
                        observaciones = notas
                    )
                )
            }) {
                Text("Guardar")
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
fun SesionDialog(
    sesion: SesionEntrenamiento?,
    onDismiss: () -> Unit,
    onConfirm: (SesionEntrenamiento) -> Unit
) {
    var nombre by remember { mutableStateOf(sesion?.nombre ?: "") }

    val tiposSesion = listOf("FUERZA", "CARDIO", "MOVILIDAD", "OTRO")
    var tipoSeleccionado by remember { mutableStateOf(sesion?.tipoSesion ?: tiposSesion.first()) }
    var tipoSesionCustom by remember { mutableStateOf("") }
    val tipoFinal = if (tipoSeleccionado == "OTRO") tipoSesionCustom else tipoSeleccionado

    var dia by remember { mutableStateOf(sesion?.dia ?: "Lunes") }
    var duracion by remember { mutableStateOf(sesion?.duracionMinutos?.toString() ?: "60") }
    var ejercicios by remember { mutableStateOf(sesion?.ejercicios ?: emptyList()) }

    var mostrarDialogoEjercicio by remember { mutableStateOf(false) }
    var ejercicioEditando by remember { mutableStateOf<Ejercicio?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sesion == null) "Nueva Sesión" else "Editar Sesión") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la Sesión") },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownField("Tipo de Sesión", tiposSesion, tipoSeleccionado) {
                    tipoSeleccionado = it
                }

                if (tipoSeleccionado == "OTRO") {
                    OutlinedTextField(
                        value = tipoSesionCustom,
                        onValueChange = { tipoSesionCustom = it },
                        label = { Text("Tipo Personalizado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DropdownField("Día de la Semana", listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"), dia) {
                    dia = it
                }

                OutlinedTextField(
                    value = duracion,
                    onValueChange = { if (it.all(Char::isDigit)) duracion = it },
                    label = { Text("Duración (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ejercicios", fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        ejercicioEditando = null
                        mostrarDialogoEjercicio = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio")
                    }
                }

                if (ejercicios.isEmpty()) {
                    Text("No hay ejercicios aún", style = MaterialTheme.typography.bodySmall)
                } else {
                    ejercicios.forEachIndexed { index, ejercicio ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ejercicio.nombre)
                            Row {
                                IconButton(onClick = {
                                    ejercicioEditando = ejercicio
                                    mostrarDialogoEjercicio = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar ejercicio")
                                }
                                IconButton(onClick = {
                                    ejercicios = ejercicios.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar ejercicio")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    SesionEntrenamiento(
                        id = sesion?.id ?: UUID.randomUUID().toString(),
                        nombre = nombre,
                        tipoSesion = tipoFinal,
                        dia = dia,
                        duracionMinutos = duracion.toIntOrNull() ?: 60,
                        ejercicios = ejercicios
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (mostrarDialogoEjercicio) {
        EjercicioDialog(
            ejercicio = ejercicioEditando,
            onDismiss = {
                ejercicioEditando = null
                mostrarDialogoEjercicio = false
            },
            onConfirm = { nuevo ->
                ejercicios = if (ejercicioEditando != null) {
                    ejercicios.map { if (it == ejercicioEditando) nuevo else it }
                } else {
                    ejercicios + nuevo
                }
                mostrarDialogoEjercicio = false
                ejercicioEditando = null
            }
        )
    }
}

@Composable
fun SesionesSection(
    sesiones: List<SesionEntrenamiento>,
    onSesionesChange: (List<SesionEntrenamiento>) -> Unit
) {
    var mostrarDialogoSesion by remember { mutableStateOf(false) }
    var sesionEditando by remember { mutableStateOf<SesionEntrenamiento?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sesiones de Entrenamiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    sesionEditando = null
                    mostrarDialogoSesion = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir sesión")
                }
            }

            if (sesiones.isEmpty()) {
                Text(
                    "No hay sesiones añadidas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sesiones.forEachIndexed { index, sesion ->
                    SesionCard(
                        sesion = sesion,
                        onEdit = {
                            sesionEditando = sesion
                            mostrarDialogoSesion = true
                        },
                        onDelete = {
                            onSesionesChange(sesiones.filterIndexed { i, _ -> i != index })
                        }
                    )
                }
            }
        }
    }

    if (mostrarDialogoSesion) {
        SesionDialog(
            sesion = sesionEditando,
            onDismiss = {
                mostrarDialogoSesion = false
                sesionEditando = null
            },
            onConfirm = { nuevaSesion ->
                onSesionesChange(
                    if (sesionEditando != null) {
                        sesiones.map { if (it.id == sesionEditando?.id) nuevaSesion else it }
                    } else {
                        sesiones + nuevaSesion
                    }
                )
                mostrarDialogoSesion = false
                sesionEditando = null
            }
        )
    }
}

@Composable
fun SesionCard(
    sesion: SesionEntrenamiento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sesion.nombre,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sesion.dia} • ${sesion.duracionMinutos} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar sesión")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar sesión")
                    }
                }
            }

            Text(
                text = "Ejercicios: ${sesion.ejercicios.size}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}