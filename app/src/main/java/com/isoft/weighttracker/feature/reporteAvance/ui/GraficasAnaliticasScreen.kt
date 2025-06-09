package com.isoft.weighttracker.feature.reporteAvance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import com.isoft.weighttracker.feature.reporteAvance.ui.components.*
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficasAnaliticasScreen(
    navController: NavController,
    viewModel: ReporteAvanceViewModel = viewModel()
) {
    val historial by viewModel.historial.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var filtroTipoReporte by remember { mutableStateOf<TipoReporte?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Filtrar reportes según el tipo seleccionado
    val reportesFiltrados = remember(historial, filtroTipoReporte) {
        if (filtroTipoReporte == null) {
            historial
        } else {
            historial.filter { it.tipoReporte == filtroTipoReporte }
        }
    }

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
                title = { Text("Análisis Gráfico") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Text(if (mostrarFiltros) "Ocultar Filtros" else "Filtros")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Panel de filtros
            if (mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Filtrar análisis por tipo de reporte:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dropdown Menu para filtros
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
                                value = filtroTipoReporte?.let { tipo ->
                                    when (tipo) {
                                        TipoReporte.DIARIO -> "Reportes Diarios"
                                        TipoReporte.SEMANAL -> "Reportes Semanales"
                                        TipoReporte.QUINCENAL -> "Reportes Quincenales"
                                        TipoReporte.MENSUAL -> "Reportes Mensuales"
                                    }
                                } ?: "Todos los Reportes",
                                onValueChange = {},
                                label = { Text("Tipo de Reporte") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los Reportes") },
                                    onClick = {
                                        filtroTipoReporte = null
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Analytics, contentDescription = null)
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
                                            filtroTipoReporte = tipo
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Text(
                                                when (tipo) {
                                                    TipoReporte.DIARIO -> "📅"
                                                    TipoReporte.SEMANAL -> "📊"
                                                    TipoReporte.QUINCENAL -> "📈"
                                                    TipoReporte.MENSUAL -> "📆"
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (filtroTipoReporte != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mostrando análisis de ${
                                    when (filtroTipoReporte) {
                                        TipoReporte.DIARIO -> "reportes diarios"
                                        TipoReporte.SEMANAL -> "reportes semanales"
                                        TipoReporte.QUINCENAL -> "reportes quincenales"
                                        TipoReporte.MENSUAL -> "reportes mensuales"
                                        null -> ""
                                    }
                                } (${reportesFiltrados.size} reportes)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando análisis...")
                        }
                    }
                }

                reportesFiltrados.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (filtroTipoReporte == null)
                                        "No hay reportes para analizar"
                                    else
                                        "No hay reportes de tipo ${filtroTipoReporte?.name}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crea algunos reportes para ver el análisis gráfico",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Resumen ejecutivo
                        item {
                            ResumenEjecutivo(reportes = reportesFiltrados)
                        }

                        // Gráfica de evolución de peso
                        if (reportesFiltrados.any { it.antropometria.isNotEmpty() }) {
                            item {
                                GraficaEvolucionPeso(
                                    reportes = reportesFiltrados,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Gráfica de actividad (barras)
                        item {
                            GraficaBarrasActividad(
                                reportes = reportesFiltrados,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Gráfica circular de progreso
                        if (reportesFiltrados.any { it.progresoMeta != null }) {
                            item {
                                GraficaCircularProgreso(
                                    reportes = reportesFiltrados,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Comparativa de reportes
                        if (reportesFiltrados.size >= 2) {
                            item {
                                GraficaComparativaReportes(
                                    reportes = reportesFiltrados,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Análisis de tendencias
                        if (reportesFiltrados.size >= 3) {
                            item {
                                AnalisisTendencias(
                                    reportes = reportesFiltrados,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Distribución de tipos de reporte
                        if (filtroTipoReporte == null) { // Solo mostrar cuando no hay filtro
                            item {
                                GraficaDistribucionTipos(
                                    reportes = reportesFiltrados,
                                    modifier = Modifier.fillMaxWidth()
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
private fun ResumenEjecutivo(
    reportes: List<com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val totalCalorias = reportes.sumOf { it.caloriasQuemadas }
    val totalPasos = reportes.sumOf { it.pasosTotales }
    val promedioProgreso = reportes.mapNotNull { it.progresoMeta?.porcentajeProgreso }.average()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Resumen Ejecutivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Análisis basado en ${reportes.size} reportes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricaResumen(
                    titulo = "Total Calorías",
                    valor = "%,d".format(totalCalorias),
                    icono = "🔥"
                )
                MetricaResumen(
                    titulo = "Total Pasos",
                    valor = "%,d".format(totalPasos),
                    icono = "🚶‍♂️"
                )
                if (!promedioProgreso.isNaN()) {
                    MetricaResumen(
                        titulo = "Progreso Prom.",
                        valor = "%.0f%%".format(promedioProgreso),
                        icono = "🎯"
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalisisTendencias(
    reportes: List<com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val tendenciasCalorias = calcularTendencia(reportes.map { it.caloriasQuemadas.toDouble() })
    val tendenciasPasos = calcularTendencia(reportes.map { it.pasosTotales.toDouble() })

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📈 Análisis de Tendencias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            TendenciaItem(
                titulo = "Calorías Quemadas",
                tendencia = tendenciasCalorias,
                icono = "🔥"
            )

            TendenciaItem(
                titulo = "Pasos Diarios",
                tendencia = tendenciasPasos,
                icono = "🚶‍♂️"
            )

            // Peso si hay datos
            val datosPeso = reportes.mapNotNull { it.antropometria.firstOrNull()?.peso?.toDouble() }
            if (datosPeso.size >= 3) {
                val tendenciaPeso = calcularTendencia(datosPeso)
                TendenciaItem(
                    titulo = "Peso Corporal",
                    tendencia = tendenciaPeso,
                    icono = "⚖️"
                )
            }
        }
    }
}

@Composable
private fun MetricaResumen(
    titulo: String,
    valor: String,
    icono: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            icono,
            style = MaterialTheme.typography.headlineMedium
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

@Composable
private fun TendenciaItem(
    titulo: String,
    tendencia: TendenciaResult,
    icono: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icono, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            color = when (tendencia.direccion) {
                TendenciaDireccion.SUBIENDO -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                TendenciaDireccion.BAJANDO -> Color(0xFFf44336).copy(alpha = 0.1f)
                TendenciaDireccion.ESTABLE -> Color(0xFF2196F3).copy(alpha = 0.1f)
            },
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (tendencia.direccion) {
                        TendenciaDireccion.SUBIENDO -> "↗"
                        TendenciaDireccion.BAJANDO -> "↘"
                        TendenciaDireccion.ESTABLE -> "→"
                    },
                    color = when (tendencia.direccion) {
                        TendenciaDireccion.SUBIENDO -> Color(0xFF4CAF50)
                        TendenciaDireccion.BAJANDO -> Color(0xFFf44336)
                        TendenciaDireccion.ESTABLE -> Color(0xFF2196F3)
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    tendencia.descripcion,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (tendencia.direccion) {
                        TendenciaDireccion.SUBIENDO -> Color(0xFF4CAF50)
                        TendenciaDireccion.BAJANDO -> Color(0xFFf44336)
                        TendenciaDireccion.ESTABLE -> Color(0xFF2196F3)
                    }
                )
            }
        }
    }
}

// Clases y funciones de utilidad para análisis de tendencias
data class TendenciaResult(
    val direccion: TendenciaDireccion,
    val pendiente: Double,
    val descripcion: String
)

enum class TendenciaDireccion {
    SUBIENDO, BAJANDO, ESTABLE
}

private fun calcularTendencia(datos: List<Double>): TendenciaResult {
    if (datos.size < 2) {
        return TendenciaResult(TendenciaDireccion.ESTABLE, 0.0, "Sin datos")
    }

    val n = datos.size
    val x = (0 until n).map { it.toDouble() }
    val y = datos

    // Regresión lineal simple
    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y) { xi, yi -> xi * yi }.sum()
    val sumX2 = x.map { it * it }.sum()

    val pendiente = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)

    val direccion = when {
        pendiente > 0.1 -> TendenciaDireccion.SUBIENDO
        pendiente < -0.1 -> TendenciaDireccion.BAJANDO
        else -> TendenciaDireccion.ESTABLE
    }

    val descripcion = when (direccion) {
        TendenciaDireccion.SUBIENDO -> "Tendencia al alza"
        TendenciaDireccion.BAJANDO -> "Tendencia a la baja"
        TendenciaDireccion.ESTABLE -> "Tendencia estable"
    }

    return TendenciaResult(direccion, pendiente, descripcion)
}