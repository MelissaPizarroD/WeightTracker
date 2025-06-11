package com.isoft.weighttracker.feature.planes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.model.EstadoPlan
import com.isoft.weighttracker.feature.planes.model.PlanEntrenamiento
import com.isoft.weighttracker.feature.planes.model.PlanNutricional
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPlanesScreen(
    navController: NavController,
    planesViewModel: PlanesViewModel = viewModel()
) {
    val context = LocalContext.current
    val planesNutricion by planesViewModel.planesNutricion.collectAsState()
    val planesEntrenamiento by planesViewModel.planesEntrenamiento.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    LaunchedEffect(Unit) {
        planesViewModel.cargarPlanesUsuario()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            planesViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Planes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { planesViewModel.cargarPlanesUsuario() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading && planesNutricion.isEmpty() && planesEntrenamiento.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resumen de planes activos
                item {
                    ResumenPlanesActivos(
                        planesNutricion = planesNutricion,
                        planesEntrenamiento = planesEntrenamiento
                    )
                }

                // Planes de Nutrición
                if (planesNutricion.isNotEmpty()) {
                    item {
                        Text(
                            "🥗 Planes de Nutrición",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(planesNutricion) { plan ->
                        PlanNutricionalCard(
                            plan = plan,
                            onActivar = { planesViewModel.activarPlanNutricional(plan.id) },
                            onDesactivar = { planesViewModel.desactivarPlanNutricional(plan.id) }
                        )
                    }
                }

                // Planes de Entrenamiento
                if (planesEntrenamiento.isNotEmpty()) {
                    item {
                        Text(
                            "💪 Planes de Entrenamiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(planesEntrenamiento) { plan ->
                        PlanEntrenamientoCard(
                            plan = plan,
                            onActivar = { planesViewModel.activarPlanEntrenamiento(plan.id) },
                            onDesactivar = { planesViewModel.desactivarPlanEntrenamiento(plan.id) }
                        )
                    }
                }

                // Mensaje cuando no hay planes
                if (planesNutricion.isEmpty() && planesEntrenamiento.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "📋",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No tienes planes disponibles",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Solicita planes a tus profesionales asociados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenPlanesActivos(
    planesNutricion: List<PlanNutricional>,
    planesEntrenamiento: List<PlanEntrenamiento>
) {
    val planNutricionActivo = planesNutricion.find { it.estado == EstadoPlan.ACTIVO }
    val planEntrenamientoActivo = planesEntrenamiento.find { it.estado == EstadoPlan.ACTIVO }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "✨ Planes Activos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (planNutricionActivo != null) "✅" else "⭕",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "🥗 Nutrición",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (planNutricionActivo != null) {
                        Text(
                            "por ${planNutricionActivo.nombreProfesional}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (planEntrenamientoActivo != null) "✅" else "⭕",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "💪 Entrenamiento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (planEntrenamientoActivo != null) {
                        Text(
                            "por ${planEntrenamientoActivo.nombreProfesional}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanNutricionalCard(
    plan: PlanNutricional,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val esActivo = plan.estado == EstadoPlan.ACTIVO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActivo) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
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
                        "🥗 Plan Nutricional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Por: ${plan.nombreProfesional}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "Creado: ${dateFormat.format(Date(plan.fechaCreacion))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (esActivo) "✅ Activo" else "⭕ Inactivo"
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "📅 Frecuencia: ${plan.frecuencia}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "🔄 Repetición: ${plan.repeticion}",
                style = MaterialTheme.typography.bodySmall
            )

            if (plan.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Observaciones: ${plan.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (esActivo) {
                    OutlinedButton(
                        onClick = onDesactivar,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desactivar")
                    }
                } else {
                    Button(
                        onClick = onActivar
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanEntrenamientoCard(
    plan: PlanEntrenamiento,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val esActivo = plan.estado == EstadoPlan.ACTIVO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActivo) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
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
                        "💪 Plan de Entrenamiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Por: ${plan.nombreProfesional}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "Creado: ${dateFormat.format(Date(plan.fechaCreacion))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (esActivo) "✅ Activo" else "⭕ Inactivo"
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "🏋️ Tipo: ${plan.tipoEjercicio}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "📍 Lugar: ${plan.lugarRealizacion.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "📅 Frecuencia: ${plan.frecuencia}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "⭐ Dificultad: ${plan.dificultad}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "🎯 Ejercicios: ${plan.ejercicios.size}",
                style = MaterialTheme.typography.bodySmall
            )

            if (plan.duracionEstimada > 0) {
                val minutos = plan.duracionEstimada / 60
                Text(
                    "⏱️ Duración: $minutos min",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (plan.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Observaciones: ${plan.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (esActivo) {
                    OutlinedButton(
                        onClick = onDesactivar,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desactivar")
                    }
                } else {
                    Button(
                        onClick = onActivar
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar")
                    }
                }
            }
        }
    }
}