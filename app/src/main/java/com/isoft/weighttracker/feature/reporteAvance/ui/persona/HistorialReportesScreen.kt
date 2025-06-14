package com.isoft.weighttracker.feature.reporteAvance.ui.persona

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialReportesScreen(
    navController: NavController,
    viewModel: ReporteAvanceViewModel = viewModel()
) {
    val historial by viewModel.historial.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var filtroTipo by remember { mutableStateOf<TipoReporte?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Filtrar reportes según el tipo seleccionado
    val reportesFiltrados = remember(historial, filtroTipo) {
        if (filtroTipo == null) {
            historial
        } else {
            historial.filter { it.tipoReporte == filtroTipo }
        }
    }

    // Calcular estadísticas
    val reportesConRetro = reportesFiltrados.count { it.retroalimentaciones.isNotEmpty() }
    val totalRetroalimentaciones = reportesFiltrados.sumOf { it.retroalimentaciones.size }

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial()
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
                title = { Text("Historial de Reportes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        navController.navigate("graficasAnaliticas")
                    }) {
                        Icon(Icons.Default.Assessment, contentDescription = "Análisis Gráfico")
                    }

                    IconButton(
                        onClick = {
                            viewModel.actualizarHistorial()
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }

                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("registrarReporte") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo reporte")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Banner de retroalimentaciones
            if (reportesConRetro > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tienes $reportesConRetro reportes con $totalRetroalimentaciones retroalimentaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Panel de filtros
            if (mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filtrar reportes por tipo:", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                value = filtroTipo?.let { tipo ->
                                    when (tipo) {
                                        TipoReporte.DIARIO -> "Reportes Diarios"
                                        TipoReporte.SEMANAL -> "Reportes Semanales"
                                        TipoReporte.QUINCENAL -> "Reportes Quincenales"
                                        TipoReporte.MENSUAL -> "Reportes Mensuales"
                                    }
                                } ?: "Todos los Reportes",
                                onValueChange = {},
                                label = { Text("Tipo de Reporte") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los Reportes") },
                                    onClick = {
                                        filtroTipo = null
                                        expanded = false
                                    }
                                )

                                TipoReporte.values().forEach { tipo ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (tipo) {
                                                    TipoReporte.DIARIO -> "Reportes Diarios"
                                                    TipoReporte.SEMANAL -> "Reportes Semanales"
                                                    TipoReporte.QUINCENAL -> "Reportes Quincenales"
                                                    TipoReporte.MENSUAL -> "Reportes Mensuales"
                                                }
                                            )
                                        },
                                        onClick = {
                                            filtroTipo = tipo
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Contenido principal
            if (reportesFiltrados.isEmpty() && !isLoading) {
                // Estado vacío
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (filtroTipo == null) "No hay reportes registrados aún"
                                else "No hay reportes de tipo ${filtroTipo?.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Presiona ➕ para crear uno",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Gráfica de resumen si hay datos
                    if (reportesFiltrados.isNotEmpty()) {
                        item {
                            GraficaResumenCard(reportes = reportesFiltrados)
                        }
                    }

                    // Lista de reportes
                    items(reportesFiltrados) { reporte ->
                        ReporteCard(
                            reporte = reporte,
                            onClick = {
                                navController.navigate("detalleReporte/${reporte.id}")
                            },
                            tieneRetroalimentaciones = reporte.retroalimentaciones.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReporteCard(
    reporte: ReporteAvance,
    onClick: () -> Unit,
    tieneRetroalimentaciones: Boolean = false
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaInicio = sdf.format(Date(reporte.fechaInicio))
    val fechaFin = sdf.format(Date(reporte.fechaFin))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (tieneRetroalimentaciones)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📆 $fechaInicio - $fechaFin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de retroalimentaciones
                    if (tieneRetroalimentaciones) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "💬 ${reporte.retroalimentaciones.size}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            reporte.tipoReporte.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("🔥 Calorías: ${reporte.caloriasQuemadas}")
                    Text("🚶‍♂️ Pasos: ${"%,d".format(reporte.pasosTotales)}")
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("🎯 Meta: ${reporte.metaActiva?.objetivo ?: "Sin datos"}")
                    Text("✅ Progreso: ${reporte.progresoMeta?.porcentajeProgreso?.let { "%.1f%%".format(it) } ?: "Sin datos"}")
                }
            }

            if (reporte.antropometria.isNotEmpty()) {
                val ant = reporte.antropometria.first()
                Spacer(modifier = Modifier.height(4.dp))
                Text("📏 Peso: ${ant.peso}kg | Grasa: ${ant.porcentajeGrasa}%")
            }

            // Mostrar texto sobre retroalimentaciones
            if (tieneRetroalimentaciones) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "✨ Este reporte tiene ${reporte.retroalimentaciones.size} retroalimentación(es) de tu profesional - Toca para ver",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GraficaResumenCard(reportes: List<ReporteAvance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Resumen Analítico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Estadísticas básicas
            val totalCalorias = reportes.sumOf { it.caloriasQuemadas }
            val totalPasos = reportes.sumOf { it.pasosTotales }
            val promedioProgreso = reportes.mapNotNull { it.progresoMeta?.porcentajeProgreso }.average()
            val pesoActual = reportes.firstOrNull()?.antropometria?.firstOrNull()?.peso
            val pesoAnterior = reportes.lastOrNull()?.antropometria?.firstOrNull()?.peso
            val reportesConRetroalimentacion = reportes.count { it.retroalimentaciones.isNotEmpty() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    titulo = "Calorías Total",
                    valor = "%,d".format(totalCalorias),
                    icono = "🔥"
                )
                EstadisticaItem(
                    titulo = "Pasos Total",
                    valor = "%,d".format(totalPasos),
                    icono = "🚶‍♂️"
                )
                EstadisticaItem(
                    titulo = "Progreso Prom.",
                    valor = if (promedioProgreso.isNaN()) "N/A" else "%.1f%%".format(promedioProgreso),
                    icono = "📊"
                )
            }

            // Mostrar estadística de retroalimentaciones
            if (reportesConRetroalimentacion > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💬 $reportesConRetroalimentacion reportes con retroalimentación de profesionales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Cambio de peso si hay datos
            if (pesoActual != null && pesoAnterior != null && pesoActual != pesoAnterior) {
                Spacer(modifier = Modifier.height(8.dp))
                val diferencia = pesoActual - pesoAnterior
                val signo = if (diferencia > 0) "+" else ""
                Text(
                    "⚖️ Cambio de peso: $signo%.1f kg".format(diferencia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun EstadisticaItem(titulo: String, valor: String, icono: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            icono,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}