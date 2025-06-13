package com.isoft.weighttracker.feature.planes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.isoft.weighttracker.feature.planes.model.EstadoSolicitud
import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.feature.planes.model.TipoPlan
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesProfesionalScreen(
    navController: NavController,
    planesViewModel: PlanesViewModel = viewModel()
) {
    val solicitudes by planesViewModel.solicitudesProfesional.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    var solicitudExpandida by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoRechazo by remember { mutableStateOf<SolicitudPlan?>(null) }

    // Cargar solicitudes al iniciar
    LaunchedEffect(Unit) {
        planesViewModel.cargarSolicitudesProfesional()
    }

    // Mostrar mensaje si existe
    mensaje?.let { msg ->
        LaunchedEffect(msg) {
            // Aquí podrías mostrar un Snackbar si quieres
            planesViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📋 Solicitudes de Planes",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { planesViewModel.cargarSolicitudesProfesional() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (solicitudes.isEmpty()) {
                        item {
                            EmptyStateCard()
                        }
                    } else {
                        // Agrupar por estado
                        val pendientes = solicitudes.filter { it.estado == EstadoSolicitud.PENDIENTE }
                        val otras = solicitudes.filter { it.estado != EstadoSolicitud.PENDIENTE }

                        if (pendientes.isNotEmpty()) {
                            item {
                                Text(
                                    "⏳ Solicitudes Pendientes (${pendientes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(pendientes) { solicitud ->
                                SolicitudProfesionalCard(
                                    solicitud = solicitud,
                                    expandida = solicitudExpandida == solicitud.id,
                                    onToggleExpansion = {
                                        solicitudExpandida = if (solicitudExpandida == solicitud.id) null else solicitud.id
                                    },
                                    onAceptar = {
                                        val solicitudJson = URLEncoder.encode(Gson().toJson(solicitud), StandardCharsets.UTF_8.toString())
                                        val ruta = when (solicitud.tipoPlan) {
                                            TipoPlan.NUTRICION -> "crearPlanNutricional/$solicitudJson"
                                            TipoPlan.ENTRENAMIENTO -> "crearPlanEntrenamiento/$solicitudJson"
                                        }
                                        navController.navigate(ruta)
                                    },
                                    onRechazar = {
                                        mostrarDialogoRechazo = solicitud
                                    }
                                )
                            }
                        }

                        if (otras.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "📝 Historial",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(otras) { solicitud ->
                                SolicitudHistorialCard(solicitud = solicitud)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de rechazo
    mostrarDialogoRechazo?.let { solicitud ->
        DialogoRechazoSolicitud(
            solicitud = solicitud,
            onConfirmar = { motivo ->
                planesViewModel.rechazarSolicitud(solicitud.id, motivo)
                mostrarDialogoRechazo = null
            },
            onCancelar = {
                mostrarDialogoRechazo = null
            }
        )
    }
}

@Composable
private fun SolicitudProfesionalCard(
    solicitud: SolicitudPlan,
    expandida: Boolean,
    onToggleExpansion: () -> Unit,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con info básica
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del tipo de plan
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (solicitud.tipoPlan) {
                        TipoPlan.NUTRICION -> MaterialTheme.colorScheme.tertiary
                        TipoPlan.ENTRENAMIENTO -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            when (solicitud.tipoPlan) {
                                TipoPlan.NUTRICION -> "🥗"
                                TipoPlan.ENTRENAMIENTO -> "💪"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        solicitud.nombreUsuario,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (solicitud.tipoPlan) {
                            TipoPlan.NUTRICION -> "Plan Nutricional"
                            TipoPlan.ENTRENAMIENTO -> "Plan de Entrenamiento"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatearFecha(solicitud.fechaSolicitud),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onToggleExpansion) {
                    Icon(
                        if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expandida) "Contraer" else "Expandir"
                    )
                }
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Detalles de la solicitud
                DetalleSolicitud(solicitud)

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onAceptar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear Plan")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleSolicitud(solicitud: SolicitudPlan) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Información del usuario
        InfoSection(
            titulo = "👤 Usuario",
            contenido = {
                InfoItem("Nombre:", solicitud.nombreUsuario)
                InfoItem("Email:", solicitud.emailUsuario)
            }
        )

        when (solicitud.tipoPlan) {
            TipoPlan.NUTRICION -> {
                InfoSection(
                    titulo = "🥗 Detalles Nutricionales",
                    contenido = {
                        if (solicitud.objetivoNutricion.isNotEmpty()) {
                            InfoItem("Objetivo:", solicitud.getObjetivoNutricionTexto())
                        }
                        if (solicitud.nivelActividad.isNotEmpty()) {
                            InfoItem("Nivel de actividad:", getNivelActividadTexto(solicitud.nivelActividad))
                        }
                        if (solicitud.restricciones.isNotEmpty()) {
                            InfoItem("Restricciones:", solicitud.restricciones.joinToString(", ") { getRestriccionTexto(it) })
                        }
                        if (solicitud.restriccionesOtras.isNotEmpty()) {
                            InfoItem("Alergias/Intolerancias:", solicitud.restriccionesOtras)
                        }
                        if (solicitud.restriccionesMedicas.isNotEmpty()) {
                            InfoItem("Restricciones médicas:", solicitud.restriccionesMedicas)
                        }
                    }
                )
            }
            TipoPlan.ENTRENAMIENTO -> {
                InfoSection(
                    titulo = "💪 Detalles de Entrenamiento",
                    contenido = {
                        if (solicitud.objetivoEntrenamiento.isNotEmpty()) {
                            InfoItem("Objetivo:", solicitud.getObjetivoEntrenamientoTexto())
                        }
                        if (solicitud.experienciaPrevia.isNotEmpty()) {
                            InfoItem("Experiencia:", solicitud.getExperienciaPreviaTexto())
                        }
                        if (solicitud.disponibilidadSemanal.isNotEmpty()) {
                            InfoItem("Disponibilidad:", solicitud.getDisponibilidadSemanalTexto())
                        }
                        if (solicitud.equipamientoDisponible.isNotEmpty()) {
                            InfoItem("Equipamiento:", solicitud.getEquipamientoTexto())
                        }
                    }
                )
            }
        }

        if (solicitud.descripcion.isNotEmpty()) {
            InfoSection(
                titulo = "📝 Descripción adicional",
                contenido = {
                    Text(
                        solicitud.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun InfoSection(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            contenido()
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SolicitudHistorialCard(solicitud: SolicitudPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (solicitud.estado) {
                EstadoSolicitud.COMPLETADA -> MaterialTheme.colorScheme.surfaceVariant
                EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (solicitud.estado) {
                    EstadoSolicitud.COMPLETADA -> Color(0xFF4CAF50)
                    EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (solicitud.estado) {
                            EstadoSolicitud.COMPLETADA -> Icons.Default.Check
                            EstadoSolicitud.RECHAZADA -> Icons.Default.Close
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    solicitud.nombreUsuario,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (solicitud.tipoPlan) {
                        TipoPlan.NUTRICION -> "Plan Nutricional"
                        TipoPlan.ENTRENAMIENTO -> "Plan de Entrenamiento"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    when (solicitud.estado) {
                        EstadoSolicitud.COMPLETADA -> "✅ Completada"
                        EstadoSolicitud.RECHAZADA -> "❌ Rechazada"
                        else -> "ℹ️ ${solicitud.estado.name}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (solicitud.estado) {
                        EstadoSolicitud.COMPLETADA -> Color(0xFF4CAF50)
                        EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                formatearFecha(solicitud.fechaSolicitud),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📋",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No tienes solicitudes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Cuando los usuarios te soliciten planes, aparecerán aquí para que puedas revisarlos y crear los planes correspondientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogoRechazoSolicitud(
    solicitud: SolicitudPlan,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var motivoRechazo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                "❌ Rechazar Solicitud",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "¿Estás seguro de que quieres rechazar la solicitud de ${solicitud.nombreUsuario}?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = motivoRechazo,
                    onValueChange = { motivoRechazo = it },
                    label = { Text("Motivo del rechazo *") },
                    placeholder = { Text("Explica por qué no puedes crear este plan...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "El usuario verá este motivo en su solicitud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (motivoRechazo.trim().isNotEmpty()) {
                        onConfirmar(motivoRechazo.trim())
                    }
                },
                enabled = motivoRechazo.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

// Funciones helper
private fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Funciones helper para evitar que Firebase las mapee como campos
private fun getNivelActividadTexto(nivel: String): String {
    return when (nivel) {
        "SEDENTARIO" -> "Sedentario (poco o ningún ejercicio)"
        "LIGERO" -> "Ligero (ejercicio ligero 1-3 días/semana)"
        "MODERADO" -> "Moderado (ejercicio moderado 3-5 días/semana)"
        "INTENSO" -> "Intenso (ejercicio intenso 6-7 días/semana)"
        else -> nivel
    }
}

private fun getRestriccionTexto(restriccion: String): String {
    return when (restriccion) {
        "SIN_LACTOSA" -> "Sin lactosa"
        "SIN_GLUTEN" -> "Sin gluten"
        "VEGETARIANO" -> "Vegetariano"
        "VEGANO" -> "Vegano"
        "RESTRICCIONES_MEDICAS" -> "Restricciones médicas"
        else -> restriccion
    }
}