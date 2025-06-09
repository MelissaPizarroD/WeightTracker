package com.isoft.weighttracker.feature.reporteAvance.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.notifications.ReportesNotificationHelper
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.feature.metas.viewmodel.MetasViewModel
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarReporteScreen(
    navController: NavController,
    viewModel: ReporteAvanceViewModel = viewModel(),
    antropometriaVM: AntropometriaViewModel = viewModel(),
    actividadVM: ActividadFisicaViewModel = viewModel(),
    metasVM: MetasViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel() // ✅ AGREGADO: Para obtener datos del usuario
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var tipoReporteSeleccionado by remember { mutableStateOf(TipoReporte.SEMANAL) }
    var isLoading by remember { mutableStateOf(false) }

    // Observar estados
    val antropometria by antropometriaVM.registros.collectAsState()
    val actividades by actividadVM.actividades.collectAsState()
    val historialPasos by actividadVM.historialPasos.collectAsState()
    val metaActiva by metasVM.metaActiva.collectAsState()
    val progreso by metasVM.progreso.collectAsState()
    val estadoGuardado by viewModel.estadoGuardado.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState() // ✅ AGREGADO

    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        antropometriaVM.cargarRegistros()
        actividadVM.cargarActividades()
        actividadVM.cargarHistorialPasos()
        metasVM.cargarMetaActiva()
        metasVM.cargarProgreso()
        userViewModel.loadUser() // ✅ AGREGADO: Cargar datos del usuario
    }

    // Calcular fechas según el tipo de reporte
    val (fechaInicio, fechaFin) = remember(tipoReporteSeleccionado) {
        val ahora = System.currentTimeMillis()
        val dias = when (tipoReporteSeleccionado) {
            TipoReporte.DIARIO -> 1
            TipoReporte.SEMANAL -> 7
            TipoReporte.QUINCENAL -> 15
            TipoReporte.MENSUAL -> 30
        }
        val inicio = ahora - (dias * 24 * 60 * 60 * 1000L)
        Pair(inicio, ahora)
    }

    // Filtrar datos según el rango de fechas
    val actividadesEnRango = remember(actividades, fechaInicio, fechaFin) {
        actividades.filter { it.fecha in fechaInicio..fechaFin }
    }

    val pasosEnRango = remember(historialPasos, fechaInicio, fechaFin) {
        historialPasos.filter { it.fecha in fechaInicio..fechaFin }
    }

    val caloriasQuemadas = actividadesEnRango.sumOf { it.caloriasQuemadas }
    val totalPasos = pasosEnRango.sumOf { it.pasos }

    // Antropometría más reciente en el rango
    val antropometriaReciente = antropometria
        .filter { it.fecha in fechaInicio..fechaFin }
        .maxByOrNull { it.fecha }

    // 🎯 FUNCIÓN PARA BUSCAR PROFESIONALES ASOCIADOS Y NOTIFICARLOS
    suspend fun notificarProfesionalesAsociados(reporteId: String, tipoReporte: TipoReporte) {
        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val uid = auth.currentUser?.uid ?: return

            // Buscar profesionales asociados al usuario
            val profesionalesSnapshot = db.collection("profesional_usuario_relations")
                .whereEqualTo("usuarioId", uid)
                .whereEqualTo("estado", "activo")
                .get()
                .await()

            val profesionalesAsociados = profesionalesSnapshot.documents.size

            // Notificar cada profesional asociado
            profesionalesSnapshot.documents.forEach { document ->
                val profesionalId = document.getString("profesionalId")
                if (profesionalId != null) {
                    // Obtener datos del profesional
                    val profesionalDoc = db.collection("users")
                        .document(profesionalId)
                        .get()
                        .await()

                    if (profesionalDoc.exists()) {
                        // 🔔 Crear notificación para el profesional
                        ReportesNotificationHelper.notificarProfesionalNuevoReporte(
                            context = context,
                            nombreUsuario = currentUser?.name ?: "Usuario",
                            tipoReporte = tipoReporte,
                            reporteId = reporteId
                        )
                    }
                }
            }

            // 📊 Notificar al usuario que su reporte fue creado
            ReportesNotificationHelper.notificarReporteCreado(
                context = context,
                tipoReporte = tipoReporte,
                reporteId = reporteId,
                profesionalesAsociados = profesionalesAsociados
            )
        } catch (e: Exception) {
            // En caso de error, solo notificar la creación del reporte
            ReportesNotificationHelper.notificarReporteCreado(
                context = context,
                tipoReporte = tipoReporte,
                reporteId = reporteId,
                profesionalesAsociados = 0
            )
        }
    }

    // Manejo de estados con notificaciones
    LaunchedEffect(estadoGuardado) {
        when (estadoGuardado) {
            true -> {
                // 🎯 CREAR Y ENVIAR NOTIFICACIONES
                val nuevoReporte = ReporteAvance(
                    fechaCreacion = System.currentTimeMillis(),
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    tipoReporte = tipoReporteSeleccionado,
                    antropometria = antropometriaReciente?.let { listOf(it) } ?: emptyList(),
                    metaActiva = metaActiva,
                    progresoMeta = progreso,
                    caloriasConsumidas = 0,
                    caloriasQuemadas = caloriasQuemadas,
                    pasosTotales = totalPasos
                )

                // Notificar a profesionales asociados
                scope.launch {
                    notificarProfesionalesAsociados(nuevoReporte.id, tipoReporteSeleccionado)
                }

                Toast.makeText(context, "Reporte guardado exitosamente ✅", Toast.LENGTH_SHORT).show()
                viewModel.limpiarEstadoGuardado()
                navController.popBackStack()
            }
            false -> {
                Toast.makeText(context, "Error al guardar el reporte ❌", Toast.LENGTH_SHORT).show()
                viewModel.limpiarEstadoGuardado()
                isLoading = false
            }
            null -> { /* No hacer nada */ }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarError()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reporte de Avance") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de tipo de reporte
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Tipo de Reporte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TipoReporte.values().forEach { tipo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (tipoReporteSeleccionado == tipo),
                                    onClick = { tipoReporteSeleccionado = tipo }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (tipoReporteSeleccionado == tipo),
                                onClick = { tipoReporteSeleccionado = tipo }
                            )
                            Text(
                                text = when (tipo) {
                                    TipoReporte.DIARIO -> "Diario (último día)"
                                    TipoReporte.SEMANAL -> "Semanal (últimos 7 días)"
                                    TipoReporte.QUINCENAL -> "Quincenal (últimos 15 días)"
                                    TipoReporte.MENSUAL -> "Mensual (últimos 30 días)"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Periodo del reporte
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            Text(
                "Periodo: ${sdf.format(Date(fechaInicio))} - ${sdf.format(Date(fechaFin))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Resumen de datos
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Resumen de Datos", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("🔥 Calorías quemadas: ${if (caloriasQuemadas > 0) caloriasQuemadas else "No disponible"}")
                    Text("🚶‍♂️ Pasos totales: ${if (totalPasos > 0) "%,d".format(totalPasos) else "No disponible"}")
                    Text("🎯 Meta activa: ${metaActiva?.objetivo ?: "Sin meta activa"}")
                    Text("📏 Antropometría: ${if (antropometriaReciente != null) "Peso: ${antropometriaReciente.peso}kg" else "No disponible"}")
                    Text("📊 Progreso meta: ${progreso?.porcentajeProgreso?.let { "%.1f%%".format(it) } ?: "Sin progreso"}")
                }
            }

            // 🔔 NUEVA SECCIÓN: Información sobre notificaciones
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔔 Notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Se te notificará cuando el reporte se guarde exitosamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "• Los profesionales asociados recibirán una notificación del nuevo reporte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "• Recibirás notificaciones cuando agreguen retroalimentaciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Advertencia si no hay datos suficientes
            val hayDatos = listOfNotNull(
                antropometriaReciente,
                metaActiva,
                progreso
            ).isNotEmpty() || caloriasQuemadas > 0 || totalPasos > 0

            if (!hayDatos) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ Datos Limitados",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "No hay datos suficientes para generar un reporte completo. El reporte se creará con la información disponible.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón para guardar con indicador de notificaciones
            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        val nuevoReporte = ReporteAvance(
                            fechaCreacion = System.currentTimeMillis(),
                            fechaInicio = fechaInicio,
                            fechaFin = fechaFin,
                            tipoReporte = tipoReporteSeleccionado,
                            antropometria = antropometriaReciente?.let { listOf(it) } ?: emptyList(),
                            metaActiva = metaActiva,
                            progresoMeta = progreso,
                            caloriasConsumidas = 0, // Puedes implementar esto más tarde
                            caloriasQuemadas = caloriasQuemadas,
                            pasosTotales = totalPasos
                        )

                        viewModel.guardarReporte(nuevoReporte)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando y notificando...")
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Guardar Reporte")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔔", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}