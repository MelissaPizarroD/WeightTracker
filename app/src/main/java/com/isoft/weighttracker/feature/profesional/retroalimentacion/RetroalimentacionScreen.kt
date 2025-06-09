package com.isoft.weighttracker.feature.profesional.retroalimentacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.feature.reporteAvance.model.Retroalimentacion
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroalimentacionScreen(
    navController: NavController,
    reporteId: String,
    usuarioId: String,
    profesionalViewModel: com.isoft.weighttracker.feature.profesional.viewmodel.ProfesionalViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel() // ✅ AGREGADO: Para obtener datos del profesional
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val reporte by profesionalViewModel.reporteActual.collectAsState()
    val isLoading by profesionalViewModel.isLoading.collectAsState()
    val error by profesionalViewModel.error.collectAsState()

    // ✅ AGREGADO: Estados para datos del profesional
    val currentUser by userViewModel.currentUser.collectAsState()
    val profesionalProfile by userViewModel.profesionalProfile.collectAsState()

    var textoRetroalimentacion by remember { mutableStateOf("") }
    var enviandoRetroalimentacion by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ AGREGADO: Cargar datos del profesional al inicio
    LaunchedEffect(Unit) {
        profesionalViewModel.cargarReportePorId(reporteId, usuarioId)
        userViewModel.loadUser()
        userViewModel.loadProfesionalProfile()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            profesionalViewModel.limpiarError()
            enviandoRetroalimentacion = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Retroalimentación") },
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
                )
            )
        },
        floatingActionButton = {
            if (reporte != null && textoRetroalimentacion.isNotBlank() && textoRetroalimentacion.length <= 500) {
                FloatingActionButton(
                    onClick = {
                        if (!enviandoRetroalimentacion) {
                            enviandoRetroalimentacion = true

                            // ✅ MEJORADO: Crear retroalimentación con datos del profesional
                            val nuevaRetroalimentacion = Retroalimentacion(
                                fecha = System.currentTimeMillis(),
                                idProfesional = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                contenido = textoRetroalimentacion.trim(),
                                // ✅ NUEVOS CAMPOS con datos del profesional
                                nombreProfesional = currentUser?.name ?: "Profesional",
                                rolProfesional = currentUser?.role ?: "profesional",
                                emailProfesional = currentUser?.email ?: ""
                            )

                            scope.launch {
                                profesionalViewModel.agregarRetroalimentacion(reporteId, usuarioId, nuevaRetroalimentacion)
                                snackbarHostState.showSnackbar("Retroalimentación enviada exitosamente ✅")
                                // Recargar el reporte para mostrar la nueva retroalimentación
                                profesionalViewModel.cargarReportePorId(reporteId, usuarioId)
                                enviandoRetroalimentacion = false
                                textoRetroalimentacion = "" // Limpiar el campo
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (enviandoRetroalimentacion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando reporte...")
                    }
                }
            }

            reporte == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se pudo cargar el reporte",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ✅ AGREGADO: Mostrar quién está escribiendo la retroalimentación
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                when (currentUser?.role?.lowercase()) {
                                    "nutricionista" -> "🥗"
                                    "entrenador" -> "💪"
                                    "medico" -> "👨‍⚕️"
                                    else -> "👨‍💼"
                                },
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Retroalimentando como:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "${currentUser?.name ?: "Profesional"} • ${currentUser?.role?.replaceFirstChar { it.uppercase() } ?: "Profesional"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Vista completa del reporte para profesionales
                    DetalleReporteParaProfesional(reporte = reporte!!)

                    // Retroalimentaciones anteriores
                    if (reporte!!.retroalimentaciones.isNotEmpty()) {
                        RetroalimentacionesAnteriores(retroalimentaciones = reporte!!.retroalimentaciones)
                    }

                    // Campo para nueva retroalimentación
                    NuevaRetroalimentacionCard(
                        texto = textoRetroalimentacion,
                        onTextoChange = { textoRetroalimentacion = it },
                        enviando = enviandoRetroalimentacion,
                        rolProfesional = currentUser?.role // ✅ AGREGADO: Pasar el rol
                    )

                    // Plantillas sugeridas por rol
                    if (textoRetroalimentacion.isBlank()) {
                        PlantillasRetroalimentacion(
                            rolProfesional = currentUser?.role ?: "", // ✅ AGREGADO: Pasar el rol
                            onPlantillaSeleccionada = { plantilla ->
                                textoRetroalimentacion = plantilla
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
                }
            }
        }
    }
}

@Composable
private fun DetalleReporteParaProfesional(
    reporte: com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaInicio = sdf.format(Date(reporte.fechaInicio))
    val fechaFin = sdf.format(Date(reporte.fechaFin))

    Column(
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
                    MetricaCardProfesional(
                        titulo = "Calorías Quemadas",
                        valor = reporte.caloriasQuemadas.toString(),
                        icono = "🔥",
                        color = Color(0xFFFF5722)
                    )
                    MetricaCardProfesional(
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
                        DatoAntropometricoProfesional("Peso", "${ant.peso} kg", "⚖️")
                        DatoAntropometricoProfesional("Grasa", "${ant.porcentajeGrasa}%", "📊")
                        DatoAntropometricoProfesional("Cintura", "${ant.cintura} cm", "📐")
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
    }
}

@Composable
private fun MetricaCardProfesional(
    titulo: String,
    valor: String,
    icono: String,
    color: Color
) {
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
private fun DatoAntropometricoProfesional(
    titulo: String,
    valor: String,
    icono: String
) {
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

@Composable
private fun RetroalimentacionesAnteriores(
    retroalimentaciones: List<Retroalimentacion>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "💬 Retroalimentaciones Anteriores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            retroalimentaciones.sortedByDescending { it.fecha }.forEach { retro ->
                val fecha = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(Date(retro.fecha))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ✅ MEJORADO: Mostrar información del profesional
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (retro.rolProfesional.lowercase()) {
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
                                        retro.nombreProfesional.ifBlank { "Profesional" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        retro.rolProfesional.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                fecha,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            retro.contenido,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NuevaRetroalimentacionCard(
    texto: String,
    onTextoChange: (String) -> Unit,
    enviando: Boolean,
    rolProfesional: String? = null // ✅ AGREGADO
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "✍️ Nueva Retroalimentación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Escribe tu retroalimentación como ${rolProfesional?.replaceFirstChar { it.uppercase() } ?: "profesional"}...") },
                placeholder = {
                    Text(
                        when (rolProfesional?.lowercase()) {
                            "nutricionista" -> "Ejemplo: \"Excelente control nutricional esta semana. Te recomiendo aumentar el consumo de proteínas...\""
                            "entrenador" -> "Ejemplo: \"Gran progreso en los entrenamientos. Considera aumentar la intensidad cardiovascular...\""
                            else -> "Ejemplo: \"Excelente progreso esta semana. Te recomiendo...\""
                        }
                    )
                },
                enabled = !enviando,
                maxLines = 5,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${texto.length}/500 caracteres",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (texto.length > 500) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (texto.isBlank()) {
                    Text(
                        "Escribe algo para enviar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (texto.length > 500) {
                    Text(
                        "Demasiado largo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }

            if (enviando) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Enviando retroalimentación...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ✅ MEJORADO: Plantillas específicas por rol del profesional
@Composable
private fun PlantillasRetroalimentacion(
    rolProfesional: String = "", // ✅ AGREGADO
    onPlantillaSeleccionada: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "💡 Plantillas Sugeridas para ${rolProfesional.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ MEJORADO: Plantillas específicas por rol
            val plantillas = when (rolProfesional.lowercase()) {
                "nutricionista" -> listOf(
                    "Excelente control nutricional esta semana. Continúa con la alimentación balanceada y mantén la hidratación.",
                    "Veo mejoras en tu composición corporal. Te recomiendo aumentar el consumo de proteínas magras.",
                    "Tus datos antropométricos muestran una tendencia positiva. Sigue enfocándote en las porciones adecuadas.",
                    "El progreso hacia tu meta nutricional es constante. Considera incluir más vegetales de hoja verde.",
                    "Buen trabajo con el control calórico. Ahora sería bueno incorporar más alimentos ricos en fibra."
                )
                "entrenador" -> listOf(
                    "Excelente progreso en tus entrenamientos esta semana. Continúa con la constancia en tu rutina de ejercicios.",
                    "Veo mejoras en tus métricas de actividad física. Te recomiendo aumentar gradualmente la intensidad cardiovascular.",
                    "Tus datos de pasos y calorías quemadas muestran dedicación. Sigue enfocándote en la técnica de los ejercicios.",
                    "El progreso hacia tu meta de acondicionamiento es constante. Considera agregar ejercicios de fuerza.",
                    "Excelente trabajo con la actividad diaria. Ahora sería bueno incorporar más ejercicios de flexibilidad."
                )
                else -> listOf(
                    "Excelente progreso esta semana. Continúa con el buen trabajo y mantén la constancia en tu rutina.",
                    "Veo mejoras en tus métricas de salud. Te recomiendo mantener los hábitos saludables actuales.",
                    "Tus datos muestran una tendencia positiva. Sigue enfocándote en un estilo de vida equilibrado.",
                    "El progreso hacia tu meta es constante. Considera ajustar gradualmente tus objetivos semanales.",
                    "Buen trabajo con el autocuidado. Continúa monitoreando tu progreso regularmente."
                )
            }

            plantillas.forEach { plantilla ->
                TextButton(
                    onClick = { onPlantillaSeleccionada(plantilla) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        plantilla,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}