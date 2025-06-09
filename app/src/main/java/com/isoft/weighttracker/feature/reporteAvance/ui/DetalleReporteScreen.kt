package com.isoft.weighttracker.feature.reporteAvance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.Retroalimentacion
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    // Estado para listener en tiempo real específico del reporte
    var reporteListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var reporteEnTiempoReal by remember { mutableStateOf<ReporteAvance?>(null) }

    // Configurar listener para este reporte específico
    LaunchedEffect(reporteId) {
        viewModel.cargarReportePorId(reporteId)

        // Configurar listener en tiempo real para el reporte específico
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            reporteListener?.remove() // Limpiar listener anterior

            reporteListener = db.collection("users")
                .document(uid)
                .collection("reportes_avance")
                .document(reporteId)
                .addSnapshotListener { snapshot, listenerError ->
                    if (listenerError != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error al actualizar reporte en tiempo real")
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val reporteActualizado = snapshot.toObject(ReporteAvance::class.java)
                            reporteEnTiempoReal = reporteActualizado

                            // Si hay nuevas retroalimentaciones, mostrar notificación
                            val retroalimentacionesAntes = reporte?.retroalimentaciones?.size ?: 0
                            val retroalimentacionesAhora = reporteActualizado?.retroalimentaciones?.size ?: 0

                            if (retroalimentacionesAhora > retroalimentacionesAntes && retroalimentacionesAntes > 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "✨ Nueva retroalimentación agregada",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al procesar actualización del reporte")
                            }
                        }
                    }
                }
        }
    }

    // Limpiar listener al salir
    DisposableEffect(Unit) {
        onDispose {
            reporteListener?.remove()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    // Usar el reporte en tiempo real si está disponible, sino el del viewmodel
    val reporteActual = reporteEnTiempoReal ?: reporte

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detalle del Reporte")
                        if (reporteActual?.retroalimentaciones?.isNotEmpty() == true) {
                            Text(
                                "Con ${reporteActual.retroalimentaciones.size} retroalimentación(es)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
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
                    // Botón de actualización manual
                    IconButton(onClick = {
                        viewModel.cargarReportePorId(reporteId)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                reporteActual != null -> {
                    DetalleReporteContent(
                        reporte = reporteActual!!,
                        modifier = Modifier.padding(16.dp),
                        enTiempoReal = reporteEnTiempoReal != null
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
fun DetalleReporteContent(
    reporte: ReporteAvance,
    modifier: Modifier = Modifier,
    enTiempoReal: Boolean = false
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaInicio = sdf.format(Date(reporte.fechaInicio))
    val fechaFin = sdf.format(Date(reporte.fechaFin))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner si está actualizándose en tiempo real
        if (enTiempoReal) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔄", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Este reporte se actualiza automáticamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mostrar número de retroalimentaciones si las hay
                        if (reporte.retroalimentaciones.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "💬 ${reporte.retroalimentaciones.size}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

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

        // ✅ MEJORADO: Retroalimentaciones con datos del profesional
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🗣️ Retroalimentaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (reporte.retroalimentaciones.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "${reporte.retroalimentaciones.size} comentarios",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (reporte.retroalimentaciones.isEmpty()) {
                    Text(
                        "No hay retroalimentación aún. Un profesional puede agregar comentarios sobre tu progreso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reporte.retroalimentaciones.sortedByDescending { it.fecha }.forEach { retro ->
                            RetroalimentacionCard(retroalimentacion = retro)
                        }
                    }
                }
            }
        }
    }
}

// ✅ NUEVO: Componente específico para mostrar retroalimentaciones con datos del profesional
@Composable
private fun RetroalimentacionCard(retroalimentacion: Retroalimentacion) {
    val fecha = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        .format(Date(retroalimentacion.fecha))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ MEJORADO: Mostrar nombre y rol del profesional
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono según el rol
                    Text(
                        when (retroalimentacion.rolProfesional.lowercase()) {
                            "nutricionista" -> "🥗"
                            "entrenador" -> "💪"
                            "medico" -> "👨‍⚕️"
                            else -> "👨‍💼"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            retroalimentacion.nombreProfesional.ifBlank { "Profesional" },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            retroalimentacion.rolProfesional.replaceFirstChar { it.uppercase() }.ifBlank { "Profesional" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                retroalimentacion.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
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