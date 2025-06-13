package com.isoft.weighttracker.feature.planes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.model.EstadoSolicitud
import com.isoft.weighttracker.feature.planes.model.TipoPlan
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.feature.asociar.viewmodel.AsociarProfesionalViewModel
import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.shared.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarPlanScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel(),
    asociarViewModel: AsociarProfesionalViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val solicitudesPendientes by planesViewModel.solicitudesPendientes.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    // USAR EL VIEWMODEL CORRECTO para profesionales asociados
    val profesionalesAsociados by asociarViewModel.asociados.collectAsState()

    // Estados básicos del formulario
    var tipoPlanSeleccionado by remember { mutableStateOf(TipoPlan.ENTRENAMIENTO) }
    var descripcion by remember { mutableStateOf("") }
    var mostrandoFormulario by remember { mutableStateOf(false) }

    // Estados para campos específicos de NUTRICIÓN
    var objetivoNutricionSeleccionado by remember { mutableStateOf("") }
    var nivelActividadSeleccionado by remember { mutableStateOf("") }
    var restriccionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var restriccionesOtras by remember { mutableStateOf("") }
    var restriccionesMedicas by remember { mutableStateOf("") } // NUEVO CAMPO

    // Estados para campos específicos de ENTRENAMIENTO
    var objetivoEntrenamientoSeleccionado by remember { mutableStateOf("") }
    var experienciaPreviaSeleccionada by remember { mutableStateOf("") }
    var disponibilidadSemanalSeleccionada by remember { mutableStateOf("") }
    var equipamientoSeleccionado by remember { mutableStateOf(emptySet<String>()) }

    // Estados para especificaciones de equipamiento
    var especificacionGimnasio by remember { mutableStateOf("") }
    var especificacionPesas by remember { mutableStateOf("") }
    var especificacionCardio by remember { mutableStateOf("") }
    var especificacionAccesorios by remember { mutableStateOf("") }
    var especificacionOtros by remember { mutableStateOf("") }

    // Estado para diálogo de confirmación
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    // Obtener profesionales asociados usando el ViewModel correcto
    val tieneEntrenador = profesionalesAsociados.containsKey("entrenador")
    val tieneNutricionista = profesionalesAsociados.containsKey("nutricionista")

    // Opciones para dropdowns NUTRICIÓN
    val opcionesObjetivoNutricion = listOf(
        "PERDER_PESO" to "Perder peso",
        "MANTENER_PESO" to "Mantener peso actual",
        "SUBIR_PESO" to "Subir de peso/masa muscular",
        "MEJORAR_COMPOSICION" to "Mejorar composición corporal",
        "CONTROL_MEDICO" to "Control médico específico"
    )

    val opcionesNivelActividad = listOf(
        "SEDENTARIO" to "Sedentario (trabajo de oficina, poco ejercicio)",
        "LIGERO" to "Ligero (ejercicio 1-3 días/semana)",
        "MODERADO" to "Moderado (ejercicio 3-5 días/semana)",
        "INTENSO" to "Intenso (ejercicio 6-7 días/semana)"
    )

    val opcionesRestricciones = listOf(
        "SIN_LACTOSA" to "Sin lactosa",
        "SIN_GLUTEN" to "Sin gluten",
        "VEGETARIANO" to "Vegetariano",
        "VEGANO" to "Vegano",
        "RESTRICCIONES_MEDICAS" to "Restricciones médicas",
        "OTRAS" to "Otras alergias/intolerancias"
    )

    // Opciones para dropdowns ENTRENAMIENTO
    val opcionesObjetivoEntrenamiento = listOf(
        "PERDER_GRASA" to "Perder grasa corporal",
        "GANAR_MUSCULO" to "Ganar masa muscular",
        "FUERZA" to "Aumentar fuerza",
        "RESISTENCIA" to "Mejorar resistencia",
        "TONIFICAR" to "Tonificar y definir",
        "REHABILITACION" to "Rehabilitación/terapéutico"
    )

    val opcionesExperienciaPrevia = listOf(
        "PRINCIPIANTE" to "Principiante (menos de 6 meses)",
        "INTERMEDIO" to "Intermedio (6 meses - 2 años)",
        "AVANZADO" to "Avanzado (más de 2 años)"
    )

    val opcionesDisponibilidadSemanal = listOf(
        "DOS_DIAS" to "2 días por semana",
        "TRES_DIAS" to "3 días por semana",
        "CUATRO_DIAS" to "4 días por semana",
        "CINCO_DIAS" to "5 días por semana",
        "SEIS_DIAS" to "6 días por semana"
    )

    val opcionesEquipamiento = listOf(
        "GIMNASIO" to "Gimnasio/Centro deportivo",
        "PESAS_CASA" to "Pesas en casa",
        "EQUIPAMIENTO_CARDIO" to "Equipamiento cardiovascular",
        "ACCESORIOS" to "Accesorios deportivos",
        "PESO_CORPORAL" to "Solo peso corporal",
        "OTROS" to "Otros equipos"
    )

    // Función para limpiar formulario
    val limpiarFormulario = {
        mostrandoFormulario = false
        descripcion = ""
        // Limpiar campos de nutrición
        objetivoNutricionSeleccionado = ""
        nivelActividadSeleccionado = ""
        restriccionesSeleccionadas = emptySet()
        restriccionesOtras = ""
        restriccionesMedicas = "" // NUEVO CAMPO
        // Limpiar campos de entrenamiento
        objetivoEntrenamientoSeleccionado = ""
        experienciaPreviaSeleccionada = ""
        disponibilidadSemanalSeleccionada = ""
        equipamientoSeleccionado = emptySet()
        especificacionGimnasio = ""
        especificacionPesas = ""
        especificacionCardio = ""
        especificacionAccesorios = ""
        especificacionOtros = ""
    }

    // Función para obtener placeholder dinámico
    val getPlaceholderDescripcion = {
        when (tipoPlanSeleccionado) {
            TipoPlan.NUTRICION -> when (objetivoNutricionSeleccionado) {
                "PERDER_PESO" -> "Ej: Quiero perder 8kg en 4 meses, como fuera de casa frecuentemente, no me gusta desayunar..."
                "SUBIR_PESO" -> "Ej: Quiero ganar masa muscular, entreno 4 días/semana, tengo dificultades para comer mucho..."
                "MANTENER_PESO" -> "Ej: Quiero mantener mi peso actual pero mejorar mis hábitos alimentarios..."
                "MEJORAR_COMPOSICION" -> "Ej: Quiero reducir grasa corporal y ganar músculo, hago pesas 3 veces por semana..."
                "CONTROL_MEDICO" -> "Ej: Tengo diabetes/hipertensión y necesito un plan alimentario específico..."
                else -> "Ej: Necesito un plan alimentario personalizado..."
            }
            TipoPlan.ENTRENAMIENTO -> when (objetivoEntrenamientoSeleccionado) {
                "PERDER_GRASA" -> "Ej: Quiero perder grasa abdominal, prefiero entrenamientos de alta intensidad..."
                "GANAR_MUSCULO" -> "Ej: Quiero ganar masa muscular en brazos y pecho, tengo experiencia con pesas..."
                "FUERZA" -> "Ej: Quiero mejorar mi fuerza en sentadillas y press de banca..."
                "RESISTENCIA" -> "Ej: Quiero preparar una carrera de 10K, entreno 3 veces por semana..."
                "TONIFICAR" -> "Ej: Quiero tonificar todo el cuerpo, prefiero ejercicios variados..."
                "REHABILITACION" -> "Ej: Tengo una lesión en la rodilla, necesito ejercicios de rehabilitación..."
                else -> "Ej: Quiero un plan para mejorar mi condición física..."
            }
        }
    }

    // UI Principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Formulario de solicitud
            if (mostrandoFormulario) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "📝 Nueva Solicitud",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Selector de tipo de plan
                            Text(
                                "Tipo de Plan:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (tieneEntrenador) {
                                    FilterChip(
                                        onClick = { tipoPlanSeleccionado = TipoPlan.ENTRENAMIENTO },
                                        label = { Text("💪 Entrenamiento") },
                                        selected = tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO
                                    )
                                }

                                if (tieneNutricionista) {
                                    FilterChip(
                                        onClick = { tipoPlanSeleccionado = TipoPlan.NUTRICION },
                                        label = { Text("🥗 Nutrición") },
                                        selected = tipoPlanSeleccionado == TipoPlan.NUTRICION
                                    )
                                }
                            }

                            // CAMPOS ESPECÍFICOS PARA NUTRICIÓN
                            if (tipoPlanSeleccionado == TipoPlan.NUTRICION) {
                                Divider()
                                Text(
                                    "🥗 Información Nutricional",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                // Objetivo principal nutricional
                                Text(
                                    "🎯 Objetivo principal:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                var expandedObjetivoNutricion by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedObjetivoNutricion,
                                    onExpandedChange = { expandedObjetivoNutricion = !expandedObjetivoNutricion }
                                ) {
                                    OutlinedTextField(
                                        value = opcionesObjetivoNutricion.find { it.first == objetivoNutricionSeleccionado }?.second ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Seleccionar objetivo") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedObjetivoNutricion)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        enabled = !isLoading
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedObjetivoNutricion,
                                        onDismissRequest = { expandedObjetivoNutricion = false }
                                    ) {
                                        opcionesObjetivoNutricion.forEach { (valor, texto) ->
                                            DropdownMenuItem(
                                                text = { Text(texto) },
                                                onClick = {
                                                    objetivoNutricionSeleccionado = valor
                                                    expandedObjetivoNutricion = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Nivel de actividad física
                                Text(
                                    "🏃 Nivel de actividad física:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                var expandedActividad by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedActividad,
                                    onExpandedChange = { expandedActividad = !expandedActividad }
                                ) {
                                    OutlinedTextField(
                                        value = opcionesNivelActividad.find { it.first == nivelActividadSeleccionado }?.second ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Seleccionar nivel de actividad") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActividad)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        enabled = !isLoading
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedActividad,
                                        onDismissRequest = { expandedActividad = false }
                                    ) {
                                        opcionesNivelActividad.forEach { (valor, texto) ->
                                            DropdownMenuItem(
                                                text = { Text(texto) },
                                                onClick = {
                                                    nivelActividadSeleccionado = valor
                                                    expandedActividad = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Restricciones alimentarias CON CHECKBOXES
                                Text(
                                    "🚫 Restricciones alimentarias:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        opcionesRestricciones.forEach { (valor, texto) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = restriccionesSeleccionadas.contains(valor),
                                                    onCheckedChange = { checked ->
                                                        restriccionesSeleccionadas = if (checked) {
                                                            restriccionesSeleccionadas + valor
                                                        } else {
                                                            restriccionesSeleccionadas - valor
                                                        }
                                                    },
                                                    enabled = !isLoading
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    texto,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        if (restriccionesSeleccionadas.isEmpty()) {
                                            Text(
                                                "Ninguna restricción seleccionada",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 40.dp)
                                            )
                                        }
                                    }
                                }

                                // Campo de texto para "Otras" restricciones
                                if (restriccionesSeleccionadas.contains("OTRAS")) {
                                    OutlinedTextField(
                                        value = restriccionesOtras,
                                        onValueChange = { restriccionesOtras = it },
                                        label = { Text("Especificar alergias/intolerancias") },
                                        placeholder = { Text("Ej: Alérgico a frutos secos, intolerante al kiwi...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2,
                                        enabled = !isLoading
                                    )
                                }

                                // Campo de texto para "Restricciones médicas"
                                if (restriccionesSeleccionadas.contains("RESTRICCIONES_MEDICAS")) {
                                    OutlinedTextField(
                                        value = restriccionesMedicas,
                                        onValueChange = { restriccionesMedicas = it },
                                        label = { Text("Especificar restricciones médicas") },
                                        placeholder = { Text("Ej: Diabetes, hipertensión, problemas renales, medicamentos...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2,
                                        enabled = !isLoading
                                    )
                                }
                            }

                            // CAMPOS ESPECÍFICOS PARA ENTRENAMIENTO
                            else if (tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO) {
                                Divider()
                                Text(
                                    "💪 Información de Entrenamiento",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                // Objetivo del entrenamiento
                                Text(
                                    "🎯 Objetivo del entrenamiento:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                var expandedObjetivoEntrenamiento by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedObjetivoEntrenamiento,
                                    onExpandedChange = { expandedObjetivoEntrenamiento = !expandedObjetivoEntrenamiento }
                                ) {
                                    OutlinedTextField(
                                        value = opcionesObjetivoEntrenamiento.find { it.first == objetivoEntrenamientoSeleccionado }?.second ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Seleccionar objetivo") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedObjetivoEntrenamiento)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        enabled = !isLoading
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedObjetivoEntrenamiento,
                                        onDismissRequest = { expandedObjetivoEntrenamiento = false }
                                    ) {
                                        opcionesObjetivoEntrenamiento.forEach { (valor, texto) ->
                                            DropdownMenuItem(
                                                text = { Text(texto) },
                                                onClick = {
                                                    objetivoEntrenamientoSeleccionado = valor
                                                    expandedObjetivoEntrenamiento = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Experiencia previa
                                Text(
                                    "📈 Experiencia previa:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                var expandedExperiencia by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedExperiencia,
                                    onExpandedChange = { expandedExperiencia = !expandedExperiencia }
                                ) {
                                    OutlinedTextField(
                                        value = opcionesExperienciaPrevia.find { it.first == experienciaPreviaSeleccionada }?.second ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Seleccionar experiencia") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExperiencia)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        enabled = !isLoading
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedExperiencia,
                                        onDismissRequest = { expandedExperiencia = false }
                                    ) {
                                        opcionesExperienciaPrevia.forEach { (valor, texto) ->
                                            DropdownMenuItem(
                                                text = { Text(texto) },
                                                onClick = {
                                                    experienciaPreviaSeleccionada = valor
                                                    expandedExperiencia = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Disponibilidad semanal
                                Text(
                                    "📅 Disponibilidad semanal:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                var expandedDisponibilidad by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedDisponibilidad,
                                    onExpandedChange = { expandedDisponibilidad = !expandedDisponibilidad }
                                ) {
                                    OutlinedTextField(
                                        value = opcionesDisponibilidadSemanal.find { it.first == disponibilidadSemanalSeleccionada }?.second ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Días disponibles") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDisponibilidad)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        enabled = !isLoading
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedDisponibilidad,
                                        onDismissRequest = { expandedDisponibilidad = false }
                                    ) {
                                        opcionesDisponibilidadSemanal.forEach { (valor, texto) ->
                                            DropdownMenuItem(
                                                text = { Text(texto) },
                                                onClick = {
                                                    disponibilidadSemanalSeleccionada = valor
                                                    expandedDisponibilidad = false
                                                }
                                            )
                                        }
                                    }
                                }


// Equipamiento disponible CON CHECKBOXES Y ESPECIFICACIONES
                                Text(
                                    "🏋️ Equipamiento disponible:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        opcionesEquipamiento.forEach { (valor, texto) ->
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = equipamientoSeleccionado.contains(valor),
                                                        onCheckedChange = { checked ->
                                                            equipamientoSeleccionado = if (checked) {
                                                                equipamientoSeleccionado + valor
                                                            } else {
                                                                equipamientoSeleccionado - valor
                                                            }
                                                        },
                                                        enabled = !isLoading
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        texto,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                // Campos de especificación que aparecen al seleccionar
                                                if (equipamientoSeleccionado.contains(valor)) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    when (valor) {
                                                        "GIMNASIO" -> OutlinedTextField(
                                                            value = especificacionGimnasio,
                                                            onValueChange = { especificacionGimnasio = it },
                                                            label = { Text("Especificar gimnasio/centro") },
                                                            placeholder = { Text("Ej: Smartfit, gimnasio del barrio, club deportivo...") },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 32.dp),
                                                            enabled = !isLoading,
                                                            singleLine = true
                                                        )
                                                        "PESAS_CASA" -> OutlinedTextField(
                                                            value = especificacionPesas,
                                                            onValueChange = { especificacionPesas = it },
                                                            label = { Text("Especificar pesas disponibles") },
                                                            placeholder = { Text("Ej: Mancuernas 5-20kg, barra olímpica, discos...") },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 32.dp),
                                                            enabled = !isLoading,
                                                            singleLine = true
                                                        )
                                                        "EQUIPAMIENTO_CARDIO" -> OutlinedTextField(
                                                            value = especificacionCardio,
                                                            onValueChange = { especificacionCardio = it },
                                                            label = { Text("Especificar equipos cardio") },
                                                            placeholder = { Text("Ej: Cinta, bicicleta estática, elíptica...") },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 32.dp),
                                                            enabled = !isLoading,
                                                            singleLine = true
                                                        )
                                                        "ACCESORIOS" -> OutlinedTextField(
                                                            value = especificacionAccesorios,
                                                            onValueChange = { especificacionAccesorios = it },
                                                            label = { Text("Especificar accesorios") },
                                                            placeholder = { Text("Ej: Bandas elásticas, TRX, pelotas medicinales...") },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 32.dp),
                                                            enabled = !isLoading,
                                                            singleLine = true
                                                        )
                                                        "OTROS" -> OutlinedTextField(
                                                            value = especificacionOtros,
                                                            onValueChange = { especificacionOtros = it },
                                                            label = { Text("Especificar otros equipos") },
                                                            placeholder = { Text("Ej: Kettlebells, barra de dominadas...") },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 32.dp),
                                                            enabled = !isLoading,
                                                            singleLine = true
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (equipamientoSeleccionado.isEmpty()) {
                                            Text(
                                                "Ningún equipamiento seleccionado",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 40.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Divider antes del campo de descripción
                            if (tipoPlanSeleccionado == TipoPlan.NUTRICION || tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO) {
                                Divider()
                            }

                            // Campo de descripción
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                label = { Text("Descripción adicional") },
                                placeholder = {
                                    Text(getPlaceholderDescripcion())
                                },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                singleLine = false,
                                enabled = !isLoading
                            )

                            // Botones de acción
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = limpiarFormulario,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text("Cancelar")
                                }

                                Button(
                                    onClick = {
                                        val profesionalId = when (tipoPlanSeleccionado) {
                                            TipoPlan.ENTRENAMIENTO -> profesionalesAsociados["entrenador"]?.uid
                                            TipoPlan.NUTRICION -> profesionalesAsociados["nutricionista"]?.uid
                                        }

                                        // Validaciones específicas según tipo de plan
                                        if (profesionalId != null && descripcion.isNotBlank()) {
                                            val esNutricion = tipoPlanSeleccionado == TipoPlan.NUTRICION
                                            val esEntrenamiento = tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO

                                            // Validaciones para NUTRICIÓN
                                            if (esNutricion) {
                                                if (objetivoNutricionSeleccionado.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor selecciona tu objetivo nutricional",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                if (nivelActividadSeleccionado.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor selecciona tu nivel de actividad física",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                if (restriccionesSeleccionadas.contains("OTRAS") && restriccionesOtras.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor especifica tus alergias/intolerancias",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                if (restriccionesSeleccionadas.contains("RESTRICCIONES_MEDICAS") && restriccionesMedicas.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor especifica tus restricciones médicas",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }
                                            }

                                            // Validaciones para ENTRENAMIENTO
                                            if (esEntrenamiento) {
                                                if (objetivoEntrenamientoSeleccionado.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor selecciona tu objetivo de entrenamiento",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                if (experienciaPreviaSeleccionada.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor indica tu experiencia previa",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                if (disponibilidadSemanalSeleccionada.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "⚠️ Por favor selecciona tu disponibilidad semanal",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                // Validar especificaciones de equipamiento
                                                val equipamientosConEspecificacion = listOf("GIMNASIO", "PESAS_CASA", "EQUIPAMIENTO_CARDIO", "ACCESORIOS", "OTROS")
                                                for (equipo in equipamientosConEspecificacion) {
                                                    if (equipamientoSeleccionado.contains(equipo)) {
                                                        val especificacion = when (equipo) {
                                                            "GIMNASIO" -> especificacionGimnasio
                                                            "PESAS_CASA" -> especificacionPesas
                                                            "EQUIPAMIENTO_CARDIO" -> especificacionCardio
                                                            "ACCESORIOS" -> especificacionAccesorios
                                                            "OTROS" -> especificacionOtros
                                                            else -> ""
                                                        }
                                                        if (especificacion.isBlank()) {
                                                            val tipoEquipo = when (equipo) {
                                                                "GIMNASIO" -> "gimnasio/centro deportivo"
                                                                "PESAS_CASA" -> "pesas disponibles en casa"
                                                                "EQUIPAMIENTO_CARDIO" -> "equipos cardiovasculares"
                                                                "ACCESORIOS" -> "accesorios deportivos"
                                                                "OTROS" -> "otros equipos"
                                                                else -> "equipamiento"
                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                "⚠️ Por favor especifica tu $tipoEquipo",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            return@Button
                                                        }
                                                    }
                                                }
                                            }

                                            // Mostrar diálogo de confirmación
                                            mostrarDialogoConfirmacion = true
                                        } else {
                                            if (profesionalId == null) {
                                                Toast.makeText(
                                                    context,
                                                    "⚠️ Error: No se encontró el profesional asociado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "⚠️ Por favor completa la descripción",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enviar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Botón para nueva solicitud (si no está mostrando el formulario)
            if (tieneEntrenador || tieneNutricionista) {
                item {
                    if (!mostrandoFormulario) {
                        Button(
                            onClick = { mostrandoFormulario = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solicitar Nuevo Plan")
                        }
                    }
                }
            }

            // Mensaje si no tiene profesionales asociados
            if (!tieneEntrenador && !tieneNutricionista) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ No tienes profesionales asociados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Para solicitar planes debes tener al menos un entrenador o nutricionista asociado a tu cuenta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else {
                // Lista de solicitudes existentes
                item {
                    Text(
                        "📋 Mis Solicitudes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (solicitudesPendientes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "📝 No tienes solicitudes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crea tu primera solicitud de plan usando el botón de arriba",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(solicitudesPendientes) { solicitud ->
                        SolicitudCard(solicitud = solicitud)
                    }
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = {
                Text(
                    "🤔 ¿Enviar solicitud?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val tipoTexto = when (tipoPlanSeleccionado) {
                    TipoPlan.NUTRICION -> "plan nutricional"
                    TipoPlan.ENTRENAMIENTO -> "plan de entrenamiento"
                }

                val profesionalNombre = when (tipoPlanSeleccionado) {
                    TipoPlan.NUTRICION -> profesionalesAsociados["nutricionista"]?.name ?: "tu nutricionista"
                    TipoPlan.ENTRENAMIENTO -> profesionalesAsociados["entrenador"]?.name ?: "tu entrenador"
                }

                Column {
                    Text("¿Estás seguro de que quieres enviar esta solicitud de $tipoTexto a $profesionalNombre?")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Una vez enviada, el profesional recibirá toda la información que proporcionaste y podrá comenzar a trabajar en tu plan personalizado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        val profesionalId = when (tipoPlanSeleccionado) {
                            TipoPlan.ENTRENAMIENTO -> profesionalesAsociados["entrenador"]?.uid
                            TipoPlan.NUTRICION -> profesionalesAsociados["nutricionista"]?.uid
                        }

                        val esNutricion = tipoPlanSeleccionado == TipoPlan.NUTRICION
                        val esEntrenamiento = tipoPlanSeleccionado == TipoPlan.ENTRENAMIENTO

                        // Crear lista de equipamiento con especificaciones
                        val equipamientoCompleto = mutableListOf<String>()
                        if (esEntrenamiento) {
                            equipamientoSeleccionado.forEach { equipo ->
                                val especificacion = when (equipo) {
                                    "GIMNASIO" -> if (especificacionGimnasio.isNotBlank()) "$equipo: $especificacionGimnasio" else equipo
                                    "PESAS_CASA" -> if (especificacionPesas.isNotBlank()) "$equipo: $especificacionPesas" else equipo
                                    "EQUIPAMIENTO_CARDIO" -> if (especificacionCardio.isNotBlank()) "$equipo: $especificacionCardio" else equipo
                                    "ACCESORIOS" -> if (especificacionAccesorios.isNotBlank()) "$equipo: $especificacionAccesorios" else equipo
                                    "OTROS" -> if (especificacionOtros.isNotBlank()) "$equipo: $especificacionOtros" else equipo
                                    else -> equipo
                                }
                                equipamientoCompleto.add(especificacion)
                            }
                        }

                        planesViewModel.enviarSolicitudPlan(
                            profesionalId = profesionalId!!,
                            tipoPlan = tipoPlanSeleccionado,
                            descripcion = descripcion,
                            // USAR FIREBASE AUTH COMO RESPALDO
                            nombreUsuario = currentUser?.name?.takeIf { it.isNotBlank() }
                                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName
                                ?: "Usuario",
                            emailUsuario = currentUser?.email?.takeIf { it.isNotBlank() }
                                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                                ?: "email@example.com",
                            // PARÁMETROS DE NUTRICIÓN
                            objetivoNutricion = if (esNutricion) objetivoNutricionSeleccionado else "",
                            nivelActividad = if (esNutricion) nivelActividadSeleccionado else "",
                            restricciones = if (esNutricion) restriccionesSeleccionadas.filter { it != "OTRAS" && it != "RESTRICCIONES_MEDICAS" }.toList() else emptyList(),
                            restriccionesOtras = if (esNutricion) restriccionesOtras else "",
                            restriccionesMedicas = if (esNutricion) restriccionesMedicas else "",
                            // PARÁMETROS DE ENTRENAMIENTO
                            objetivoEntrenamiento = if (esEntrenamiento) objetivoEntrenamientoSeleccionado else "",
                            experienciaPrevia = if (esEntrenamiento) experienciaPreviaSeleccionada else "",
                            disponibilidadSemanal = if (esEntrenamiento) disponibilidadSemanalSeleccionada else "",
                            equipamientoDisponible = equipamientoCompleto
                        )
                        limpiarFormulario()
                    }
                ) {
                    Text("✅ Sí, enviar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacion = false }
                ) {
                    Text("❌ Cancelar")
                }
            }
        )
    }

    // Mostrar mensajes
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            planesViewModel.limpiarMensaje()
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        // Cargar usuario PRIMERO y esperar un poco
        userViewModel.loadUser()
        kotlinx.coroutines.delay(500) // Dar tiempo a que se cargue

        // Luego cargar lo demás
        planesViewModel.cargarSolicitudesUsuario()
        asociarViewModel.cargarProfesionalesAsociados()
    }
}

// COMPONENTE PARA MOSTRAR CADA SOLICITUD
@Composable
private fun SolicitudCard(solicitud: SolicitudPlan) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (solicitud.estado) {
                EstadoSolicitud.PENDIENTE -> when (solicitud.tipoPlan) {
                    TipoPlan.ENTRENAMIENTO -> MaterialTheme.colorScheme.secondaryContainer
                    TipoPlan.NUTRICION -> MaterialTheme.colorScheme.tertiaryContainer
                }
                EstadoSolicitud.COMPLETADA -> MaterialTheme.colorScheme.primaryContainer
                EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when (solicitud.tipoPlan) {
                            TipoPlan.ENTRENAMIENTO -> "💪 Plan de Entrenamiento"
                            TipoPlan.NUTRICION -> "🥗 Plan de Nutrición"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        dateFormat.format(Date(solicitud.fechaSolicitud)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            when (solicitud.estado) {
                                EstadoSolicitud.PENDIENTE -> "⏳ Pendiente"
                                EstadoSolicitud.COMPLETADA -> "✅ Completada"
                                EstadoSolicitud.EN_PROGRESO -> "🔄 En Progreso"
                                EstadoSolicitud.RECHAZADA -> "❌ Rechazada"
                            }
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (solicitud.estado) {
                            EstadoSolicitud.COMPLETADA -> MaterialTheme.colorScheme.primary
                            EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        },
                        labelColor = when (solicitud.estado) {
                            EstadoSolicitud.COMPLETADA -> MaterialTheme.colorScheme.onPrimary
                            EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                solicitud.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            // Mostrar observaciones si existen
            if (solicitud.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Observaciones: ${solicitud.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ NUEVO: Mostrar motivo de rechazo si fue rechazada
            if (solicitud.estado == EstadoSolicitud.RECHAZADA && !solicitud.motivoRechazo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "❌ Motivo del rechazo:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            solicitud.motivoRechazo!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        // Mostrar fecha de rechazo si está disponible
                        solicitud.fechaRechazo?.let { fechaRechazo ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Rechazada el: ${dateFormat.format(Date(fechaRechazo))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Mostrar mensaje de plan completado
            if (solicitud.estado == EstadoSolicitud.COMPLETADA && solicitud.planCreado != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "✨ ¡Tu plan ha sido creado!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tu plan está disponible en la sección 'Mis Planes'. ¡Comienza a seguirlo para alcanzar tus objetivos!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Mostrar fecha de completado si está disponible
                        solicitud.fechaCompletada?.let { fechaCompletada ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Completada el: ${dateFormat.format(Date(fechaCompletada))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}