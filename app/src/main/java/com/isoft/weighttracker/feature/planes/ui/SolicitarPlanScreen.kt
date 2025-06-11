package com.isoft.weighttracker.feature.planes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.model.EstadoSolicitud
import com.isoft.weighttracker.feature.planes.model.TipoPlan
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarPlanScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val solicitudesPendientes by planesViewModel.solicitudesPendientes.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    // Estados para el formulario
    var tipoPlanSeleccionado by remember { mutableStateOf(TipoPlan.ENTRENAMIENTO) }
    var descripcion by remember { mutableStateOf("") }
    var mostrandoFormulario by remember { mutableStateOf(false) }

    // Obtener profesionales asociados del usuario
    val profesionales = currentUser?.profesionales ?: emptyMap()
    val tieneEntrenador = profesionales.containsKey("entrenador")
    val tieneNutricionista = profesionales.containsKey("nutricionista")

    LaunchedEffect(Unit) {
        userViewModel.loadUser()
        planesViewModel.cargarSolicitudesUsuario()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            planesViewModel.limpiarMensaje()
            if (it.contains("exitosamente") || it.contains("correctamente")) {
                mostrandoFormulario = false
                descripcion = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Plan") },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información sobre profesionales asociados
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "👥 Profesionales Asociados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (tieneEntrenador) {
                            Text(
                                "💪 Entrenador: Asociado",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                "💪 Entrenador: No asociado",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        if (tieneNutricionista) {
                            Text(
                                "🥗 Nutricionista: Asociado",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                "🥗 Nutricionista: No asociado",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        if (!tieneEntrenador && !tieneNutricionista) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Debes asociar profesionales antes de solicitar planes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Botón para nueva solicitud
            if (tieneEntrenador || tieneNutricionista) {
                item {
                    if (!mostrandoFormulario) {
                        Button(
                            onClick = { mostrandoFormulario = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solicitar Nuevo Plan")
                        }
                    }
                }
            }

            // Formulario de solicitud
            if (mostrandoFormulario) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "📝 Nueva Solicitud",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Selector de tipo de plan
                            Text(
                                "Tipo de Plan:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (tieneEntrenador) {
                                    FilterChip(
                                        onClick = { tipoPlanSeleccionado = TipoPlan.ENTRENAMIENTO },
                                        label = { Text("💪 Entrenamiento") },
                                        selected = tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO
                                    )
                                }

                                if (tieneNutricionista) {
                                    FilterChip(
                                        onClick = { tipoPlanSeleccionado = TipoPlan.NUTRICION },
                                        label = { Text("🥗 Nutrición") },
                                        selected = tipoPlanSeleccionado == TipoPlan.NUTRICION
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Campo de descripción
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                label = { Text("Descripción de la solicitud") },
                                placeholder = {
                                    Text(
                                        when (tipoPlanSeleccionado) {
                                            TipoPlan.ENTRENAMIENTO -> "Ej: Quiero un plan para ganar masa muscular, entreno 3 veces por semana..."
                                            TipoPlan.NUTRICION -> "Ej: Necesito un plan para perder peso, soy vegetariano..."
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                singleLine = false,
                                enabled = !isLoading
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botones de acción
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        mostrandoFormulario = false
                                        descripcion = ""
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text("Cancelar")
                                }

                                Button(
                                    onClick = {
                                        val profesionalId = when (tipoPlanSeleccionado) {
                                            TipoPlan.ENTRENAMIENTO -> profesionales["entrenador"]
                                            TipoPlan.NUTRICION -> profesionales["nutricionista"]
                                        }

                                        if (profesionalId != null && descripcion.isNotBlank()) {
                                            planesViewModel.enviarSolicitudPlan(
                                                profesionalId = profesionalId,
                                                tipoPlan = tipoPlanSeleccionado,
                                                descripcion = descripcion,
                                                nombreUsuario = currentUser?.name ?: "",
                                                emailUsuario = currentUser?.email ?: ""
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading && descripcion.isNotBlank()
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Enviar Solicitud")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lista de solicitudes pendientes
            item {
                Text(
                    "📋 Mis Solicitudes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (solicitudesPendientes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "📭",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No tienes solicitudes activas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(solicitudesPendientes) { solicitud ->
                    SolicitudCard(solicitud = solicitud)
                }
            }
        }
    }
}

@Composable
private fun SolicitudCard(
    solicitud: com.isoft.weighttracker.feature.planes.model.SolicitudPlan
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (solicitud.estado) {
                EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.secondaryContainer
                EstadoSolicitud.COMPLETADA -> MaterialTheme.colorScheme.primaryContainer
                EstadoSolicitud.EN_PROGRESO -> MaterialTheme.colorScheme.tertiaryContainer
                EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${if (solicitud.tipoPlan == TipoPlan.ENTRENAMIENTO) "💪" else "🥗"} Plan de ${solicitud.tipoPlan.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        dateFormat.format(Date(solicitud.fechaSolicitud)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            when (solicitud.estado) {
                                EstadoSolicitud.PENDIENTE -> "⏳ Pendiente"
                                EstadoSolicitud.COMPLETADA -> "✅ Completada"
                                EstadoSolicitud.EN_PROGRESO -> "🔄 En Progreso"
                                EstadoSolicitud.RECHAZADA -> "❌ Rechazada"
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                solicitud.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            if (solicitud.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Observaciones: ${solicitud.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (solicitud.estado == EstadoSolicitud.COMPLETADA && solicitud.planCreado != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "✨ Tu plan ha sido creado y está disponible en la sección de planes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}