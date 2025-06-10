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
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.feature.metas.viewmodel.MetasViewModel
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import com.isoft.weighttracker.feature.reporteAvance.viewmodel.ReporteAvanceViewModel
import com.isoft.weighttracker.shared.UserViewModel // ✅ NUEVO: Import del UserViewModel
import kotlinx.coroutines.launch
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
    userViewModel: UserViewModel = viewModel() // ✅ NUEVO: Agregar UserViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var tipoReporteSeleccionado by remember { mutableStateOf(TipoReporte.SEMANAL) }
    var isLoading by remember { mutableStateOf(false) }
    var datosCompletos by remember { mutableStateOf(false) }

    // Observar estados
    val antropometria by antropometriaVM.registros.collectAsState()
    val actividades by actividadVM.actividades.collectAsState()
    val historialPasos by actividadVM.historialPasos.collectAsState()
    val metaActiva by metasVM.metaActiva.collectAsState()
    val progreso by metasVM.progreso.collectAsState()
    val estadoGuardado by viewModel.estadoGuardado.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ NUEVO: Obtener perfil del usuario para el sexo
    val personaProfile by userViewModel.personaProfile.collectAsState()

    // ✅ FUNCIÓN PARA CARGAR TODOS LOS DATOS
    val cargarTodosLosDatos = {
        scope.launch {
            try {
                isLoading = true
                datosCompletos = false

                println("🚀 === INICIANDO CARGA COMPLETA DE DATOS ===")

                // Cargar todos los datos de forma secuencial y esperar a que terminen
                println("📥 Cargando antropometría...")
                antropometriaVM.cargarRegistros()

                println("📥 Cargando actividades...")
                actividadVM.cargarActividades()

                println("📥 Cargando historial de pasos...")
                actividadVM.cargarHistorialPasos()

                println("📥 Cargando perfil de usuario...")
                // ✅ NUEVO: Cargar perfil del usuario
                userViewModel.loadPersonaProfile()

                println("📥 Cargando meta activa...")
                // ✅ IMPORTANTE: Cargar la meta activa y esperar
                metasVM.cargarMetaActiva()

                // ✅ CRUCIAL: Pequeña pausa para asegurar que la meta se cargue antes del progreso
                kotlinx.coroutines.delay(1000) // Aumentamos el delay

                println("📥 Verificando meta activa cargada...")
                val metaVerificacion = metaActiva
                println("🔍 Meta activa después de cargar: $metaVerificacion")
                println("🔍 Meta objetivo: ${metaVerificacion?.objetivo}")
                println("🔍 Meta ID: ${metaVerificacion?.id}")

                println("📥 Cargando progreso...")
                // ✅ IMPORTANTE: Cargar el progreso después de la meta
                metasVM.cargarProgreso()

                // ✅ NUEVA: Pequeña pausa final para asegurar que todos los datos estén disponibles
                kotlinx.coroutines.delay(500)

                println("📥 Verificando progreso cargado...")
                val progresoVerificacion = progreso
                println("🔍 Progreso después de cargar: $progresoVerificacion")
                println("🔍 Porcentaje progreso: ${progresoVerificacion?.porcentajeProgreso}")

                datosCompletos = true
                isLoading = false
                println("✅ === CARGA COMPLETA FINALIZADA ===")
            } catch (e: Exception) {
                isLoading = false
                println("❌ Error en carga completa: ${e.message}")
                Toast.makeText(context, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ MEJORADO: Cargar datos al inicio y cuando cambie el tipo de reporte
    LaunchedEffect(Unit) {
        cargarTodosLosDatos()
    }

    // ✅ NUEVO: Recargar datos cuando cambie el tipo de reporte
    LaunchedEffect(tipoReporteSeleccionado) {
        if (datosCompletos) {
            cargarTodosLosDatos()
        }
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

    // Manejo de estados
    LaunchedEffect(estadoGuardado) {
        when (estadoGuardado) {
            true -> {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // ✅ NUEVO: Botón para recargar datos manualmente
                    IconButton(
                        onClick = { cargarTodosLosDatos() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("🔄", style = MaterialTheme.typography.titleMedium)
                        }
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
            // ✅ NUEVO: Indicador de carga de datos
            if (isLoading || !datosCompletos) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cargando datos para el reporte...")
                    }
                }
            }

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
                                    onClick = {
                                        if (!isLoading) {
                                            tipoReporteSeleccionado = tipo
                                        }
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (tipoReporteSeleccionado == tipo),
                                onClick = {
                                    if (!isLoading) {
                                        tipoReporteSeleccionado = tipo
                                    }
                                },
                                enabled = !isLoading
                            )
                            Text(
                                text = when (tipo) {
                                    TipoReporte.DIARIO -> "Diario (último día)"
                                    TipoReporte.SEMANAL -> "Semanal (últimos 7 días)"
                                    TipoReporte.QUINCENAL -> "Quincenal (últimos 15 días)"
                                    TipoReporte.MENSUAL -> "Mensual (últimos 30 días)"
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                color = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
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

            // ✅ MEJORADO: Resumen de datos con estado de carga
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊 Resumen de Datos", style = MaterialTheme.typography.titleMedium)

                        // ✅ NUEVO: Indicador de estado de los datos
                        if (datosCompletos && !isLoading) {
                            Text("✅", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("🔥 Calorías quemadas: ${if (caloriasQuemadas > 0) caloriasQuemadas else "No disponible"}")
                    Text("🚶‍♂️ Pasos totales: ${if (totalPasos > 0) "%,d".format(totalPasos) else "No disponible"}")

                    // ✅ MEJORADO: Mostrar información más detallada de la meta
                    metaActiva?.let { meta ->
                        Text("🎯 Meta activa: ${meta.objetivo} (${meta.pesoInicial}kg → ${meta.pesoObjetivo}kg)")
                        // ✅ NUEVO: Mostrar progreso si está disponible
                        progreso?.let { prog ->
                            Text("📊 Progreso meta: ${"%.1f".format(prog.porcentajeProgreso)}% (${prog.pesoActual}kg actual)")
                        } ?: Text("📊 Progreso meta: Calculando...")
                    } ?: run {
                        Text("🎯 Meta activa: Sin meta activa")
                        Text("📊 Progreso meta: Sin progreso")
                    }

                    Text("📏 Antropometría: ${if (antropometriaReciente != null) "Peso: ${antropometriaReciente.peso}kg" else "No disponible"}")
                }
            }

            // ✅ MEJORADO: Advertencia si no hay datos suficientes
            val metaActivaLocal = metaActiva
            val progresoLocal = progreso
            val hayDatos = listOfNotNull(
                antropometriaReciente,
                metaActivaLocal,
                progresoLocal
            ).isNotEmpty() || caloriasQuemadas > 0 || totalPasos > 0

            if (!hayDatos && datosCompletos) {
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

            // ✅ MEJORADO: Botón para guardar con validación mejorada
            Button(
                onClick = {
                    if (!isLoading && datosCompletos) {
                        isLoading = true

                        // ✅ NUEVA: Capturar valores locales para evitar problemas de smart cast
                        val metaActivaLocal = metaActiva
                        val progresoLocal = progreso

                        // ✅ NUEVO: Debug extensivo antes de crear el reporte
                        println("🔍 === DEBUG ANTES DE CREAR REPORTE ===")
                        println("🔍 Meta activa local: $metaActivaLocal")
                        println("🔍 Meta objetivo: ${metaActivaLocal?.objetivo}")
                        println("🔍 Meta ID: ${metaActivaLocal?.id}")
                        println("🔍 Meta activa flag: ${metaActivaLocal?.activa}")
                        println("🔍 Meta cumplida flag: ${metaActivaLocal?.cumplida}")
                        println("🔍 Progreso local: $progresoLocal")
                        println("🔍 Porcentaje progreso: ${progresoLocal?.porcentajeProgreso}")
                        println("🔍 Peso actual: ${progresoLocal?.pesoActual}")
                        println("🔍 === FIN DEBUG ANTES DE CREAR REPORTE ===")

                        // ✅ NUEVA: Validación adicional antes de crear el reporte
                        val reporteValido = metaActivaLocal != null || caloriasQuemadas > 0 || totalPasos > 0 || antropometriaReciente != null

                        if (!reporteValido) {
                            Toast.makeText(context, "No hay datos suficientes para crear el reporte", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@Button
                        }

                        val nuevoReporte = ReporteAvance(
                            fechaCreacion = System.currentTimeMillis(),
                            fechaInicio = fechaInicio,
                            fechaFin = fechaFin,
                            tipoReporte = tipoReporteSeleccionado,
                            antropometria = antropometriaReciente?.let { listOf(it) } ?: emptyList(),
                            metaActiva = metaActivaLocal, // ✅ Ahora debería tener la meta activa
                            progresoMeta = progresoLocal, // ✅ Ahora debería tener el progreso
                            caloriasConsumidas = 0, // Puedes implementar esto más tarde
                            caloriasQuemadas = caloriasQuemadas,
                            pasosTotales = totalPasos,
                            sexoUsuario = personaProfile?.sexo ?: "" // ✅ NUEVO: Incluir sexo del usuario
                        )

                        // ✅ NUEVO: Log para debug
                        println("🔍 DEBUG - Guardando reporte:")
                        println("   Meta activa: ${nuevoReporte.metaActiva?.objetivo}")
                        println("   Progreso: ${nuevoReporte.progresoMeta?.porcentajeProgreso}")
                        println("   Antropometría: ${nuevoReporte.antropometria.size} registros")

                        viewModel.guardarReporte(nuevoReporte)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && datosCompletos
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    when {
                        isLoading -> "Guardando..."
                        !datosCompletos -> "Cargando datos..."
                        else -> "Guardar Reporte"
                    }
                )
            }
        }
    }
}