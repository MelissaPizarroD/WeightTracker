package com.isoft.weighttracker.feature.planes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.model.CategoriaAlimento
import com.isoft.weighttracker.feature.planes.model.ConsumoOcasional
import com.isoft.weighttracker.feature.planes.model.EstadoPlan
import com.isoft.weighttracker.feature.planes.model.PlanNutricional
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerPlanNutricionalScreen(
    navController: NavController,
    planId: String,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val planesNutricion by planesViewModel.planesNutricion.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()

    // Encontrar el plan por ID
    val plan = planesNutricion.find { it.id == planId }

    LaunchedEffect(Unit) {
        // Cargar planes si no están cargados
        if (planesNutricion.isEmpty()) {
            planesViewModel.cargarPlanesUsuario()
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Plan Nutricional") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    plan?.let { planActual ->
                        // Botón de activar/desactivar
                        if (planActual.estado == EstadoPlan.ACTIVO) {
                            IconButton(
                                onClick = {
                                    planesViewModel.desactivarPlanNutricional(planActual.id)
                                }
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Desactivar Plan")
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    planesViewModel.activarPlanNutricional(planActual.id)
                                }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Activar Plan")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading && plan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (plan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "😕 Plan no encontrado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Este plan podría haber sido eliminado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información del plan
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (plan.estado == EstadoPlan.ACTIVO) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📋 Información del Plan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

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

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("👨‍⚕️ Nutricionista: ${plan.nombreProfesional}")
                        Text("📅 Frecuencia: ${plan.frecuencia}")
                        Text("🔄 Repetición: ${plan.repeticion}")

                        plan.fechaActivacion?.let { fecha ->
                            Text("✅ Activo desde: ${dateFormat.format(Date(fecha))}")
                        }

                        plan.fechaDesactivacion?.let { fecha ->
                            Text("⏸️ Desactivado: ${dateFormat.format(Date(fecha))}")
                        }
                    }
                }

                // Grupos de alimentos activos
                Text(
                    "🍽️ Tu Plan Alimentario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Mostrar solo las categorías activas (SIN ACEITE)
                if (plan.patatasArrozPanPasta.activo) {
                    CategoriaAlimentoView(
                        titulo = "🥔 Patatas, arroz, pan, pasta integral",
                        categoria = plan.patatasArrozPanPasta,
                        tipoFrecuencia = "DIARIA"
                    )
                }

                if (plan.verdurasHortalizas.activo) {
                    CategoriaAlimentoView(
                        titulo = "🥗 Verduras y hortalizas",
                        categoria = plan.verdurasHortalizas,
                        tipoFrecuencia = "DIARIA"
                    )
                }

                if (plan.frutas.activo) {
                    CategoriaAlimentoView(
                        titulo = "🍎 Frutas",
                        categoria = plan.frutas,
                        tipoFrecuencia = "DIARIA"
                    )
                }

                if (plan.lecheDerivados.activo) {
                    CategoriaAlimentoView(
                        titulo = "🥛 Leche y derivados",
                        categoria = plan.lecheDerivados,
                        tipoFrecuencia = "DIARIA",
                        unidadMedida = "ml o g"
                    )
                }

                if (plan.pescados.activo) {
                    CategoriaAlimentoView(
                        titulo = "🐟 Pescados",
                        categoria = plan.pescados,
                        tipoFrecuencia = "SEMANAL"
                    )
                }

                if (plan.carnesMagrasAvesHuevos.activo) {
                    CategoriaAlimentoView(
                        titulo = "🍗 Carnes magras, aves y huevos",
                        categoria = plan.carnesMagrasAvesHuevos,
                        tipoFrecuencia = "SEMANAL"
                    )
                }

                if (plan.legumbres.activo) {
                    CategoriaAlimentoView(
                        titulo = "🫘 Legumbres",
                        categoria = plan.legumbres,
                        tipoFrecuencia = "SEMANAL"
                    )
                }

                if (plan.frutoSecos.activo) {
                    CategoriaAlimentoView(
                        titulo = "🥜 Frutos secos",
                        categoria = plan.frutoSecos,
                        tipoFrecuencia = "SEMANAL"
                    )
                }

                // Consumo ocasional
                if (plan.consumoOcasional.embutidosCarnesGrasas ||
                    plan.consumoOcasional.dulcesSnacksRefrescos ||
                    plan.consumoOcasional.mantequillaMargarinaBolleria ||
                    plan.consumoOcasional.observaciones.isNotEmpty()) {

                    ConsumoOcasionalView(consumoOcasional = plan.consumoOcasional)
                }

                // Observaciones generales
                if (plan.observacionesGenerales.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "📝 Observaciones Importantes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(plan.observacionesGenerales)
                        }
                    }
                }

                // Compatibilidad con observaciones del modelo antiguo
                // Nota: El campo 'observaciones' del modelo antiguo ya no existe
                // Solo usamos 'observacionesGenerales' del nuevo modelo
            }
        }
    }
}

@Composable
fun CategoriaAlimentoView(
    titulo: String,
    categoria: CategoriaAlimento,
    tipoFrecuencia: String,
    unidadMedida: String = "g"
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Frecuencia personalizada o estándar
            if (categoria.frecuenciaDiaria.isNotEmpty()) {
                Text("📊 Frecuencia: ${categoria.frecuenciaDiaria}")
            }

            // Tipo específico
            if (categoria.tipoespecifico.isNotEmpty()) {
                Text("🏷️ Tipo recomendado: ${categoria.tipoespecifico}")
            }

            // Raciones y peso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (tipoFrecuencia == "DIARIA" && categoria.racionesPorDia > 0) {
                    Text("🍽️ ${categoria.racionesPorDia} raciones/día")
                } else if (tipoFrecuencia == "SEMANAL" && categoria.racionesPorSemana > 0) {
                    Text("🍽️ ${categoria.racionesPorSemana} raciones/semana")
                }

                if (categoria.pesoPorRacion > 0) {
                    Text("⚖️ ${categoria.pesoPorRacion.let {
                        if (it == it.toInt().toDouble()) it.toInt().toString()
                        else String.format("%.1f", it)
                    }} $unidadMedida/ración")
                }
            }

            // Alternar con otros
            if (categoria.alternarConOtros) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🔄 Alternar con otros alimentos de esta categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Observaciones específicas
            if (categoria.observaciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📝 ${categoria.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConsumoOcasionalView(consumoOcasional: ConsumoOcasional) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "❌ Consumo Ocasional Permitido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (consumoOcasional.embutidosCarnesGrasas) {
                Text("✅ 🥓 Embutidos y carnes grasas")
            }

            if (consumoOcasional.dulcesSnacksRefrescos) {
                Text("✅ 🍭 Dulces, snacks, refrescos")
            }

            if (consumoOcasional.mantequillaMargarinaBolleria) {
                Text("✅ 🧈 Mantequilla, margarina y bollería")
            }

            if (consumoOcasional.observaciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📝 ${consumoOcasional.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}