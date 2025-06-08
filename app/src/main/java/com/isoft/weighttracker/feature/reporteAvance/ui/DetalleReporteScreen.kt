package com.isoft.weighttracker.feature.reporteAvance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReporteScreen(
    navController: NavController,
    reporteId: String,
    viewModel: ReporteAvanceViewModel = viewModel()
) {
    val reporte by viewModel.reporteActual.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarReportePorId(reporteId)
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (reporte != null) {
                FloatingActionButton(
                    onClick = { navController.navigate("retroalimentacion/${reporte!!.id}") },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = "Retroalimentar")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                reporte != null -> {
                    DetalleReporteContent(
                        reporte = reporte!!,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando reporte...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetalleReporteContent(reporte: ReporteAvance, modifier: Modifier = Modifier) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaInicio = sdf.format(Date(reporte.fechaInicio))
    val fechaFin = sdf.format(Date(reporte.fechaFin))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con información básica
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📆 $fechaInicio - $fechaFin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            reporte.tipoReporte.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Reporte generado el ${sdf.format(Date(reporte.fechaCreacion))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Métricas principales
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📊 Métricas Principales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricaCard(
                        titulo = "Calorías Quemadas",
                        valor = reporte.caloriasQuemadas.toString(),
                        icono = "🔥",
                        color = Color(0xFFFF5722)
                    )
                    MetricaCard(
                        titulo = "Pasos Totales",
                        valor = "%,d".format(reporte.pasosTotales),
                        icono = "🚶‍♂️",
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Datos Antropométricos
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📏 Datos Antropométricos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reporte.antropometria.isNotEmpty()) {
                    val ant = reporte.antropometria.first()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DatoAntropometrico("Peso", "${ant.peso} kg", "⚖️")
                        DatoAntropometrico("Grasa", "${ant.porcentajeGrasa}%", "📊")
                        DatoAntropometrico("Cintura", "${ant.cintura} cm", "📐")
                    }
                } else {
                    Text(
                        "Sin datos antropométricos registrados en este periodo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Meta y Progreso
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🎯 Meta y Progreso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                reporte.metaActiva?.let { meta ->
                    Text("• Objetivo: ${meta.objetivo}")
                    reporte.progresoMeta?.let { progreso ->
                        Spacer(modifier = Modifier.height(8.dp))

                        // Barra de progreso
                        val progresoFloat = (progreso.porcentajeProgreso / 100f).coerceIn(0f, 1f)
                        Text("Avance: ${"%.1f".format(progreso.porcentajeProgreso)}%")
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progresoFloat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = when {
                                progreso.porcentajeProgreso >= 80 -> Color(0xFF4CAF50)
                                progreso.porcentajeProgreso >= 50 -> Color(0xFFFF9800)
                                else -> Color(0xFFf44336)
                            }
                        )
                    } ?: Text("• Sin datos de progreso disponibles")
                } ?: Text("No hay meta activa registrada para este periodo.")
            }
        }

        // Análisis de Calorías
        if (reporte.caloriasConsumidas > 0 || reporte.caloriasQuemadas > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔥 Análisis de Calorías",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val balance = reporte.caloriasConsumidas - reporte.caloriasQuemadas

                    Text("• Consumidas: ${reporte.caloriasConsumidas}")
                    Text("• Quemadas: ${reporte.caloriasQuemadas}")
                    Text(
                        "• Balance: ${if (balance > 0) "+" else ""}$balance",
                        color = when {
                            balance > 200 -> Color(0xFFf44336)
                            balance < -200 -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Retroalimentaciones
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🗣️ Retroalimentaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reporte.retroalimentaciones.isEmpty()) {
                    Text(
                        "No hay retroalimentación aún. Un profesional puede agregar comentarios sobre tu progreso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reporte.retroalimentaciones.forEach { retro ->
                            val fecha = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                .format(Date(retro.fecha))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "📅 $fecha",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        retro.contenido,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricaCard(titulo: String, valor: String, icono: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            icono,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DatoAntropometrico(titulo: String, valor: String, icono: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            icono,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}