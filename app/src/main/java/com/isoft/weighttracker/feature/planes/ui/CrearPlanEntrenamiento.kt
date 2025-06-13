package com.isoft.weighttracker.feature.planes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.planes.model.*
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.feature.planes.utils.PlanEntrenamientoUtils
import com.isoft.weighttracker.feature.planes.utils.EjercicioMapper
import com.isoft.weighttracker.feature.planes.utils.AntropometriaUtils
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

// Modelos simplificados para el formulario
data class SesionEntrenamiento(
    var dia: String = "",
    var nombre: String = "",
    var tipoSesion: String = "FUERZA", // FUERZA, CARDIO, MIXTO
    var gruposMusculares: List<String> = emptyList(),
    var ejercicios: List<EjercicioSimple> = emptyList(),
    var duracionMinutos: Int = 60,
    var notas: String = ""
)

data class EjercicioSimple(
    var nombre: String = "",
    var series: String = "",
    var repeticiones: String = "",
    var peso: String = "",
    var descanso: String = "",
    var notas: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlanEntrenamientoScreen(
    navController: NavController,
    solicitud: SolicitudPlan,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    // ✅ NUEVOS ESTADOS: Datos del usuario para mostrar información completa
    var personaProfile by remember { mutableStateOf<PersonaProfile?>(null) }
    var antropometriaReciente by remember { mutableStateOf<Antropometria?>(null) }
    var cargandoDatosUsuario by remember { mutableStateOf(true) }

    // Estados del formulario simplificado
    var nombrePlan by remember { mutableStateOf("Plan de ${solicitud.nombreUsuario}") }
    var objetivo by remember { mutableStateOf(solicitud.objetivoEntrenamiento) }
    var duracionSemanas by remember { mutableStateOf("8") }
    var frecuenciaSemanal by remember { mutableStateOf(solicitud.disponibilidadSemanal) }
    var observacionesGenerales by remember { mutableStateOf("") }

    // Lista de sesiones de entrenamiento
    var sesiones by remember { mutableStateOf(listOf<SesionEntrenamiento>()) }

    // Estado para mostrar diálogo de confirmación
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    // ✅ CARGAR DATOS DEL USUARIO al iniciar
    LaunchedEffect(solicitud.usuarioId) {
        cargandoDatosUsuario = true
        try {
            val userRepository = UserRepository()

            // Cargar perfil de la persona
            personaProfile = userRepository.getPersonaProfileByUserId(solicitud.usuarioId)

            // Cargar antropometría más reciente
            antropometriaReciente = userRepository.getAntropometriaRecienteByUserId(solicitud.usuarioId)

        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cargandoDatosUsuario = false
        }
    }

    // Generar sesiones iniciales basadas en la frecuencia
    LaunchedEffect(frecuenciaSemanal) {
        if (sesiones.isEmpty()) {
            sesiones = PlanEntrenamientoUtils.generarSesionesIniciales(frecuenciaSemanal)
        }
    }

    // Mostrar mensaje
    mensaje?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            planesViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Plan de Entrenamiento") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del usuario (con datos completos)
            InfoUsuarioCard(
                solicitud = solicitud,
                personaProfile = personaProfile,
                antropometriaReciente = antropometriaReciente,
                cargandoDatos = cargandoDatosUsuario
            )

            // Información básica del plan
            InformacionBasicaPlan(
                nombrePlan = nombrePlan,
                onNombrePlanChange = { nombrePlan = it },
                objetivo = objetivo,
                onObjetivoChange = { objetivo = it },
                duracionSemanas = duracionSemanas,
                onDuracionSemanasChange = { duracionSemanas = it },
                frecuenciaSemanal = frecuenciaSemanal,
                onFrecuenciaSemanalChange = {
                    frecuenciaSemanal = it
                    sesiones = PlanEntrenamientoUtils.generarSesionesIniciales(it)
                }
            )

            // Sesiones de entrenamiento
            SesionesEntrenamientoSection(
                sesiones = sesiones,
                onSesionesChange = { sesiones = it }
            )

            // Observaciones generales
            ObservacionesGeneralesCard(
                observaciones = observacionesGenerales,
                onObservacionesChange = { observacionesGenerales = it }
            )

            // Botón crear plan
            Button(
                onClick = { mostrarDialogoConfirmacion = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nombrePlan.isNotBlank() && sesiones.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Plan de Entrenamiento")
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = {
                Text(
                    "💪 Confirmar Plan de Entrenamiento",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("¿Estás seguro de enviar este plan de entrenamiento a:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "👤 ${solicitud.nombreUsuario}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📋 Plan: $nombrePlan")
                    Text("⏱️ Duración: $duracionSemanas semanas")
                    Text("📅 Frecuencia: ${PlanEntrenamientoUtils.getFrecuenciaTexto(frecuenciaSemanal)}")
                    Text("🏋️ Sesiones: ${sesiones.size}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false

                        // Crear el plan con la nueva estructura
                        val planEntrenamiento = PlanEntrenamiento(
                            usuarioId = solicitud.usuarioId,
                            profesionalId = currentUser?.uid ?: "",
                            nombreProfesional = currentUser?.name ?: "",
                            // ✅ NUEVOS CAMPOS
                            nombrePlan = nombrePlan,
                            objetivo = objetivo,
                            duracionSemanas = duracionSemanas.toIntOrNull() ?: 8,
                            frecuenciaSemanal = frecuenciaSemanal,
                            nivelDificultad = PlanEntrenamientoUtils.determinarNivelDificultad(solicitud.experienciaPrevia),
                            tipoPrograma = "PERSONALIZADO",
                            sesiones = EjercicioMapper.convertirASesionesReales(sesiones),
                            equipamientoNecesario = PlanEntrenamientoUtils.extraerEquipamientoNecesario(sesiones, solicitud),
                            lugarRealizacion = PlanEntrenamientoUtils.determinarLugarRealizacion(solicitud.equipamientoDisponible),
                            adaptaciones = PlanEntrenamientoUtils.extraerAdaptaciones(solicitud),
                            progresion = PlanEntrenamientoUtils.generarProgresion(objetivo, duracionSemanas),
                            observacionesGenerales = observacionesGenerales,
                            // ✅ CAMPOS DEPRECATED para compatibilidad
                            tipoEjercicio = PlanEntrenamientoUtils.mapearObjetivoATipoEjercicio(objetivo),
                            materialesSugeridos = PlanEntrenamientoUtils.extraerMaterialesDeEjercicios(sesiones),
                            frecuencia = PlanEntrenamientoUtils.getFrecuenciaTexto(frecuenciaSemanal),
                            dificultad = PlanEntrenamientoUtils.determinarNivelDificultad(solicitud.experienciaPrevia),
                            duracionEstimada = PlanEntrenamientoUtils.calcularDuracionPromedio(sesiones) * 60,
                            ejercicios = EjercicioMapper.convertirSesionesAEjerciciosLegacy(sesiones),
                            observaciones = observacionesGenerales
                        )

                        planesViewModel.crearPlanEntrenamiento(solicitud.id, planEntrenamiento)
                        navController.popBackStack()
                    }
                ) {
                    Text("✅ Sí, crear plan")
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
}

@Composable
private fun InfoUsuarioCard(
    solicitud: SolicitudPlan,
    personaProfile: PersonaProfile?,
    antropometriaReciente: Antropometria?,
    cargandoDatos: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "👤 Información del Usuario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Información básica de la solicitud
            Text("📝 Nombre: ${solicitud.nombreUsuario}")
            Text("📧 Email: ${solicitud.emailUsuario}")
            Text("🎯 Objetivo: ${solicitud.getObjetivoEntrenamientoTexto()}")
            Text("💪 Experiencia: ${solicitud.getExperienciaPreviaTexto()}")
            Text("📅 Disponibilidad: ${solicitud.getDisponibilidadSemanalTexto()}")

            if (solicitud.equipamientoDisponible.isNotEmpty()) {
                Text("🏋️ Equipamiento: ${solicitud.getEquipamientoTexto()}")
            }

            if (solicitud.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💬 Descripción: ${solicitud.descripcion}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ DATOS FÍSICOS Y MÉDICOS COMPLETOS
            if (cargandoDatos) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cargando datos físicos...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Text(
                    "📊 Datos Físicos y Médicos:",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )

                personaProfile?.let { profile ->
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("🎂 Edad: ${profile.edad} años")
                    Text("⚧ Sexo: ${profile.sexo.replaceFirstChar { it.uppercase() }}")
                    Text("📏 Estatura: ${profile.estatura} cm")

                    // Mostrar IMC si tenemos antropometría
                    antropometriaReciente?.let { antro ->
                        val imc = AntropometriaUtils.calcularIMCConCm(antro.peso, profile.estatura)
                        Text("⚖️ Peso actual: ${antro.peso} kg")
                        Text("📈 IMC: ${"%.1f".format(imc)} (${AntropometriaUtils.clasificarIMC(imc)})")

                        if (antro.porcentajeGrasa > 0) {
                            val clasificacionGrasa = AntropometriaUtils.clasificarPorcentajeGrasa(
                                antro.porcentajeGrasa,
                                profile.sexo,
                                profile.edad
                            )
                            Text("🔥 Grasa corporal: ${"%.1f".format(antro.porcentajeGrasa)}% ($clasificacionGrasa)")
                        }

                        if (antro.fecha > 0) {
                            val diasAtras = AntropometriaUtils.diasTranscurridos(antro.fecha)
                            val esReciente = AntropometriaUtils.sonDatosRecientes(antro.fecha)
                            Text(
                                "📅 Última medición: hace $diasAtras días${if (esReciente) " ✅" else " ⚠️"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (esReciente)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        // ✅ VERIFICAR SI REQUIERE SUPERVISIÓN MÉDICA
                        val (requiereSupervision, razonSupervision) = AntropometriaUtils.requiereSupervisionMedica(
                            imc, profile.edad, profile.antecedentesMedicos
                        )

                        if (requiereSupervision) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        "⚠️ ATENCIÓN MÉDICA",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        razonSupervision,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    } ?: Text(
                        "⚠️ Sin datos antropométricos recientes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    // ✅ ANTECEDENTES MÉDICOS - CRÍTICO PARA ENTRENAMIENTO
                    if (profile.antecedentesMedicos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "🏥 Antecedentes Médicos:",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    profile.antecedentesMedicos,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "✅ Sin antecedentes médicos reportados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } ?: Text(
                    "❌ No se pudieron cargar los datos del perfil",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InformacionBasicaPlan(
    nombrePlan: String,
    onNombrePlanChange: (String) -> Unit,
    objetivo: String,
    onObjetivoChange: (String) -> Unit,
    duracionSemanas: String,
    onDuracionSemanasChange: (String) -> Unit,
    frecuenciaSemanal: String,
    onFrecuenciaSemanalChange: (String) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📋 Información Básica del Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del plan
            OutlinedTextField(
                value = nombrePlan,
                onValueChange = onNombrePlanChange,
                label = { Text("Nombre del plan") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Objetivo
            val objetivos = listOf(
                "PERDER_GRASA" to "Perder grasa corporal",
                "GANAR_MUSCULO" to "Ganar masa muscular",
                "FUERZA" to "Aumentar fuerza",
                "RESISTENCIA" to "Mejorar resistencia",
                "TONIFICAR" to "Tonificar y definir",
                "REHABILITACION" to "Rehabilitación"
            )

            var expandedObjetivo by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedObjetivo,
                onExpandedChange = { expandedObjetivo = !expandedObjetivo }
            ) {
                OutlinedTextField(
                    value = objetivos.find { it.first == objetivo }?.second ?: "Seleccionar objetivo",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Objetivo principal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedObjetivo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedObjetivo,
                    onDismissRequest = { expandedObjetivo = false }
                ) {
                    objetivos.forEach { (valor, texto) ->
                        DropdownMenuItem(
                            text = { Text(texto) },
                            onClick = {
                                onObjetivoChange(valor)
                                expandedObjetivo = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Duración
                OutlinedTextField(
                    value = duracionSemanas,
                    onValueChange = onDuracionSemanasChange,
                    label = { Text("Semanas") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Frecuencia
                val frecuencias = listOf(
                    "DOS_DIAS" to "2 días/semana",
                    "TRES_DIAS" to "3 días/semana",
                    "CUATRO_DIAS" to "4 días/semana",
                    "CINCO_DIAS" to "5 días/semana",
                    "SEIS_DIAS" to "6 días/semana"
                )

                var expandedFrecuencia by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedFrecuencia,
                    onExpandedChange = { expandedFrecuencia = !expandedFrecuencia },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = frecuencias.find { it.first == frecuenciaSemanal }?.second ?: "Frecuencia",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFrecuencia,
                        onDismissRequest = { expandedFrecuencia = false }
                    ) {
                        frecuencias.forEach { (valor, texto) ->
                            DropdownMenuItem(
                                text = { Text(texto) },
                                onClick = {
                                    onFrecuenciaSemanalChange(valor)
                                    expandedFrecuencia = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SesionesEntrenamientoSection(
    sesiones: List<SesionEntrenamiento>,
    onSesionesChange: (List<SesionEntrenamiento>) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🏋️ Sesiones de Entrenamiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val nuevaSesion = SesionEntrenamiento(
                            dia = "Día ${sesiones.size + 1}",
                            nombre = "Sesión ${sesiones.size + 1}"
                        )
                        onSesionesChange(sesiones + nuevaSesion)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar sesión")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sesiones.isEmpty()) {
                Text(
                    "No hay sesiones creadas. Toca + para agregar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sesiones.forEachIndexed { index, sesion ->
                    SesionCard(
                        sesion = sesion,
                        onSesionChange = { sesionActualizada ->
                            val nuevasSesiones = sesiones.toMutableList()
                            nuevasSesiones[index] = sesionActualizada
                            onSesionesChange(nuevasSesiones)
                        },
                        onEliminar = {
                            onSesionesChange(sesiones.toMutableList().apply { removeAt(index) })
                        }
                    )
                    if (index < sesiones.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SesionCard(
    sesion: SesionEntrenamiento,
    onSesionChange: (SesionEntrenamiento) -> Unit,
    onEliminar: () -> Unit
) {
    var expandida by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header de la sesión
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sesion.nombre.ifEmpty { "Sesión sin nombre" },
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${sesion.dia} • ${sesion.tipoSesion} • ${sesion.ejercicios.size} ejercicios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = { expandida = !expandida }) {
                        Icon(
                            if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expandida) "Contraer" else "Expandir"
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(12.dp))

                // Formulario de la sesión
                SesionForm(
                    sesion = sesion,
                    onSesionChange = onSesionChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SesionForm(
    sesion: SesionEntrenamiento,
    onSesionChange: (SesionEntrenamiento) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Nombre y día
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = sesion.nombre,
                onValueChange = { onSesionChange(sesion.copy(nombre = it)) },
                label = { Text("Nombre sesión") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = sesion.dia,
                onValueChange = { onSesionChange(sesion.copy(dia = it)) },
                label = { Text("Día") },
                modifier = Modifier.weight(1f)
            )
        }

        // Tipo de sesión
        val tiposSesion = listOf("FUERZA", "CARDIO", "MIXTO")
        var expandedTipo by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo }
        ) {
            OutlinedTextField(
                value = sesion.tipoSesion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de sesión") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                tiposSesion.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            onSesionChange(sesion.copy(tipoSesion = tipo))
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        // Lista de ejercicios
        Text(
            "Ejercicios:",
            fontWeight = FontWeight.Medium
        )

        sesion.ejercicios.forEachIndexed { index, ejercicio ->
            EjercicioSimpleCard(
                ejercicio = ejercicio,
                onEjercicioChange = { ejercicioActualizado ->
                    val nuevosEjercicios = sesion.ejercicios.toMutableList()
                    nuevosEjercicios[index] = ejercicioActualizado
                    onSesionChange(sesion.copy(ejercicios = nuevosEjercicios))
                },
                onEliminar = {
                    val nuevosEjercicios = sesion.ejercicios.toMutableList()
                    nuevosEjercicios.removeAt(index)
                    onSesionChange(sesion.copy(ejercicios = nuevosEjercicios))
                }
            )
        }

        // Botón agregar ejercicio
        TextButton(
            onClick = {
                val nuevoEjercicio = EjercicioSimple()
                onSesionChange(sesion.copy(ejercicios = sesion.ejercicios + nuevoEjercicio))
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Agregar ejercicio")
        }

        // Notas de la sesión
        OutlinedTextField(
            value = sesion.notas,
            onValueChange = { onSesionChange(sesion.copy(notas = it)) },
            label = { Text("Notas de la sesión") },
            placeholder = { Text("Calentamiento, enfoque específico, progresión...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )
    }
}

@Composable
private fun EjercicioSimpleCard(
    ejercicio: EjercicioSimple,
    onEjercicioChange: (EjercicioSimple) -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = ejercicio.nombre,
                    onValueChange = { onEjercicioChange(ejercicio.copy(nombre = it)) },
                    label = { Text("Ejercicio") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ejercicio.series,
                    onValueChange = { onEjercicioChange(ejercicio.copy(series = it)) },
                    label = { Text("Series") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = ejercicio.repeticiones,
                    onValueChange = { onEjercicioChange(ejercicio.copy(repeticiones = it)) },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = ejercicio.peso,
                    onValueChange = { onEjercicioChange(ejercicio.copy(peso = it)) },
                    label = { Text("Peso") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ejercicio.descanso,
                    onValueChange = { onEjercicioChange(ejercicio.copy(descanso = it)) },
                    label = { Text("Descanso") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = ejercicio.notas,
                    onValueChange = { onEjercicioChange(ejercicio.copy(notas = it)) },
                    label = { Text("Notas") },
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun ObservacionesGeneralesCard(
    observaciones: String,
    onObservacionesChange: (String) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📝 Observaciones Generales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = observaciones,
                onValueChange = onObservacionesChange,
                label = { Text("Observaciones del plan") },
                placeholder = { Text("Progresión, recomendaciones especiales, adaptaciones...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }
    }
}

// ===== FIN DEL ARCHIVO =====
// Todas las funciones helper se han movido a:
// - PlanEntrenamientoUtils.kt
// - EjercicioMapper.kt
// - AntropometriaUtils.kt