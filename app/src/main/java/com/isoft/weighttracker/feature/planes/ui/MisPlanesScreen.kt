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
        if (isLoading && planesEntrenamiento.isEmpty()) {
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

                item {
                    ResumenPlanesActivos(
                        planesEntrenamiento = planesEntrenamiento
                    )
                }

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
                            navController = navController,
                            onActivar = { planesViewModel.activarPlanEntrenamiento(plan.id) },
                            onDesactivar = { planesViewModel.desactivarPlanEntrenamiento(plan.id) }
                        )
                    }
                } else {
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
                                Text("📋", style = MaterialTheme.typography.headlineLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No tienes planes disponibles",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Solicita un plan a tu entrenador",
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
    planesEntrenamiento: List<PlanEntrenamiento>
) {
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
                // Nutrición: mensaje de no disponible
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "❌",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "🥗 Nutrición",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "No disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }

                // Entrenamiento
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

// ✅ MANTENIDA: PlanEntrenamientoCard (sin cambios)
@Composable
private fun PlanEntrenamientoCard(
    plan: PlanEntrenamiento,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit,
    navController: NavController
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val esActivo = plan.estado == EstadoPlan.ACTIVO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActivo) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "💪 ${plan.nombrePlan}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "por ${plan.nombreProfesional}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (plan.estado) {
                        EstadoPlan.ACTIVO -> MaterialTheme.colorScheme.secondary
                        EstadoPlan.INACTIVO -> MaterialTheme.colorScheme.outline
                        EstadoPlan.FINALIZADO -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (plan.estado) {
                            EstadoPlan.ACTIVO -> "ACTIVO"
                            EstadoPlan.INACTIVO -> "INACTIVO"
                            EstadoPlan.FINALIZADO -> "FINALIZADO"
                            else -> plan.estado.name
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (plan.estado) {
                            EstadoPlan.ACTIVO -> MaterialTheme.colorScheme.onSecondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("🎯 Objetivo: ${plan.objetivo}", style = MaterialTheme.typography.bodyMedium)
            Text("📅 Duración: ${plan.duracionSemanas} semanas", style = MaterialTheme.typography.bodyMedium)
            Text("📆 Frecuencia: ${plan.frecuenciaSemanal}", style = MaterialTheme.typography.bodyMedium)

            if (plan.sesiones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🗓️ ${plan.sesiones.size} sesiones incluidas",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            plan.fechaActivacion?.let { fecha ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "✅ Activo desde: ${dateFormat.format(Date(fecha))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plan.estado == EstadoPlan.ACTIVO) {
                    OutlinedButton(
                        onClick = onDesactivar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desactivar")
                    }
                } else {
                    Button(
                        onClick = onActivar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar")
                    }
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate("verPlanEntrenamiento/${plan.id}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Ver Plan")
                }
            }
        }
    }
}