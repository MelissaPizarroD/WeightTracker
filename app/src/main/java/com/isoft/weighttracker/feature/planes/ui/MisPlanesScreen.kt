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
                            navController = navController, // ✅ AGREGADO: navController
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

// ✅ ACTUALIZADA: PlanNutricionalCard con navegación y nuevo modelo
@Composable
private fun PlanNutricionalCard(
    plan: PlanNutricional,
    navController: NavController,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "🥗 Plan Nutricional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "por ${plan.nombreProfesional}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Estado del plan
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (plan.estado) {
                        EstadoPlan.ACTIVO -> MaterialTheme.colorScheme.primary
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
                            EstadoPlan.ACTIVO -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del plan
            Text(
                "📅 Frecuencia: ${plan.frecuencia}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "🔄 Repetición: ${plan.repeticion}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ✅ NUEVO: Mostrar categorías activas en lugar de comidas (SIN ACEITE)
            val categoriasActivas = mutableListOf<String>()
            if (plan.patatasArrozPanPasta.activo) categoriasActivas.add("Cereales")
            if (plan.verdurasHortalizas.activo) categoriasActivas.add("Verduras")
            if (plan.frutas.activo) categoriasActivas.add("Frutas")
            if (plan.lecheDerivados.activo) categoriasActivas.add("Lácteos")
            if (plan.pescados.activo) categoriasActivas.add("Pescados")
            if (plan.carnesMagrasAvesHuevos.activo) categoriasActivas.add("Carnes/Huevos")
            if (plan.legumbres.activo) categoriasActivas.add("Legumbres")
            if (plan.frutoSecos.activo) categoriasActivas.add("Frutos secos")

            if (categoriasActivas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🍽️ Incluye: ${categoriasActivas.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // ✅ ACTUALIZADO: Mostrar observaciones generales
            if (plan.observacionesGenerales.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "📝 ${plan.observacionesGenerales}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Fechas
            plan.fechaActivacion?.let { fecha ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "✅ Activo desde: ${dateFormat.format(Date(fecha))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plan.estado == EstadoPlan.ACTIVO) {
                    OutlinedButton(
                        onClick = onDesactivar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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

                // ✅ NUEVO: Botón para ver detalles funcional
                OutlinedButton(
                    onClick = {
                        navController.navigate("verPlanNutricional/${plan.id}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (esActivo) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Ver Plan")
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "💪 Plan de Entrenamiento",
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

            Text(
                "🏋️ Tipo: ${plan.tipoEjercicio}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "📍 Lugar: ${plan.lugarRealizacion}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "📅 Frecuencia: ${plan.frecuencia}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (plan.ejercicios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "💪 ${plan.ejercicios.size} ejercicios incluidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (plan.observaciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "📝 ${plan.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActivo) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
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
                        // TODO: Implementar ver plan de entrenamiento
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