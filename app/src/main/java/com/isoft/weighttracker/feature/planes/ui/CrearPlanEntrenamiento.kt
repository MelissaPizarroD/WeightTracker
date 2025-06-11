package com.isoft.weighttracker.feature.planes.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import com.isoft.weighttracker.feature.planes.model.Ejercicio
import com.isoft.weighttracker.feature.planes.model.PlanEntrenamiento
import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlanEntrenamientoScreen(
    navController: NavController,
    solicitud: SolicitudPlan,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    // Estados para el plan de entrenamiento
    var tipoEjercicio by remember { mutableStateOf("Cardio") }
    var lugarRealizacion by remember { mutableStateOf("gimnasio") }
    var materialesSugeridos by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("3 veces por semana") }
    var dificultad by remember { mutableStateOf("Medio") }
    var duracionEstimada by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Lista de ejercicios
    var ejercicios by remember { mutableStateOf(listOf<Ejercicio>()) }

    // ✅ NUEVOS: Estados para datos del usuario
    var personaProfile by remember { mutableStateOf<PersonaProfile?>(null) }
    var antropometriaReciente by remember { mutableStateOf<Antropometria?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.loadUser() // ← ASEGURARSE QUE ESTO SE EJECUTE
    }

    // ✅ ACTUALIZADO: Cargar datos del usuario que solicitó el plan
    LaunchedEffect(solicitud.usuarioId) {
        scope.launch {
            try {
                val userRepo = UserRepository()
                // Cargar datos del usuario que solicitó el plan
                personaProfile = userRepo.getPersonaProfileByUserId(solicitud.usuarioId)
                antropometriaReciente = userRepo.getAntropometriaRecienteByUserId(solicitud.usuarioId)
            } catch (e: Exception) {
                Log.e("CrearPlanEntrenamiento", "Error cargando datos del usuario", e)
            }
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            planesViewModel.limpiarMensaje()
            if (it.contains("exitosamente")) {
                navController.navigateUp()
            }
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ ACTUALIZADA: Información del usuario CON antecedentes médicos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Información del Usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Datos básicos
                    Text(
                        "👤 Nombre: ${solicitud.nombreUsuario}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "📧 Email: ${solicitud.emailUsuario}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // ✅ DATOS MÉDICOS Y ANTROPOMÉTRICOS
                    personaProfile?.let { profile ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "📋 Datos Médicos y Físicos:",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            "🎂 Edad: ${profile.edad} años",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "⚧ Sexo: ${profile.sexo.replaceFirstChar { it.uppercase() }}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "📏 Estatura: ${profile.estatura} cm",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Antropometría reciente
                        antropometriaReciente?.let { antro ->
                            Text(
                                "⚖️ Peso actual: ${antro.peso} kg (IMC: ${"%.1f".format(antro.imc)})",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ✅ ANTECEDENTES MÉDICOS - MUY IMPORTANTE PARA ENTRENAMIENTO
                        Text(
                            "🏥 Antecedentes Médicos:",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = if (profile.antecedentesMedicos.isNotBlank()) {
                                    profile.antecedentesMedicos
                                } else {
                                    "Sin antecedentes médicos registrados"
                                },
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (profile.antecedentesMedicos.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }

                        // ✅ ADVERTENCIA ESPECIAL PARA ENTRENADORES
                        if (profile.antecedentesMedicos.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                )
                            ) {
                                Text(
                                    text = "⚠️ IMPORTANTE: Considera los antecedentes médicos al diseñar ejercicios. Consulta con médico si es necesario.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "💬 Solicitud:",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        solicitud.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Configuración del plan
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "⚙️ Configuración del Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tipo de ejercicio
                    Text(
                        "Tipo de Ejercicio:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cardio", "Fuerza", "Resistencia").forEach { tipo ->
                            FilterChip(
                                onClick = { tipoEjercicio = tipo },
                                label = { Text(tipo) },
                                selected = tipoEjercicio == tipo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lugar de realización
                    Text(
                        "Lugar de Realización:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("casa", "gimnasio").forEach { lugar ->
                            FilterChip(
                                onClick = { lugarRealizacion = lugar },
                                label = { Text(lugar.replaceFirstChar { it.uppercase() }) },
                                selected = lugarRealizacion == lugar
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Frecuencia
                    Text(
                        "Frecuencia:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("2 veces por semana", "3 veces por semana", "4 o mas veces").forEach { freq ->
                            FilterChip(
                                onClick = { frecuencia = freq },
                                label = { Text(freq) },
                                selected = frecuencia == freq
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dificultad
                    Text(
                        "Dificultad:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Facil", "Medio", "Dificil").forEach { nivel ->
                            FilterChip(
                                onClick = { dificultad = nivel },
                                label = { Text(nivel) },
                                selected = dificultad == nivel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Materiales y duración
                    OutlinedTextField(
                        value = materialesSugeridos,
                        onValueChange = { materialesSugeridos = it },
                        label = { Text("Materiales sugeridos") },
                        placeholder = { Text("Ej: Mancuernas, banda elástica, colchoneta") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = duracionEstimada,
                        onValueChange = { duracionEstimada = it },
                        label = { Text("Duración estimada (minutos)") },
                        placeholder = { Text("Ej: 45") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Lista de ejercicios
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "💪 Ejercicios del Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                ejercicios = ejercicios + Ejercicio()
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar ejercicio")
                        }
                    }

                    if (ejercicios.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay ejercicios agregados. Toca el botón + para agregar ejercicios.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ejercicios.forEachIndexed { index, ejercicio ->
                            Spacer(modifier = Modifier.height(16.dp))
                            EjercicioCard(
                                ejercicio = ejercicio,
                                onEjercicioChanged = { ejercicioActualizado ->
                                    ejercicios = ejercicios.toMutableList().apply {
                                        this[index] = ejercicioActualizado
                                    }
                                },
                                onEliminar = {
                                    ejercicios = ejercicios.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Observaciones
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📝 Observaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones adicionales") },
                        placeholder = { Text("Recomendaciones especiales, progresiones, notas importantes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }

            // Botón de crear plan
            Button(
                onClick = {
                    val duracionMinutos = duracionEstimada.toIntOrNull() ?: 0
                    val duracionSegundos = duracionMinutos * 60

                    // ✅ DEBUG: Verificar datos del profesional
                    Log.d("CrearPlan", "=== DEBUG DATOS PROFESIONAL ===")
                    Log.d("CrearPlan", "currentUser: $currentUser")
                    Log.d("CrearPlan", "currentUser?.uid: ${currentUser?.uid}")
                    Log.d("CrearPlan", "currentUser?.name: ${currentUser?.name}")
                    Log.d("CrearPlan", "currentUser?.email: ${currentUser?.email}")
                    Log.d("CrearPlan", "currentUser?.role: ${currentUser?.role}")

                    val profesionalId = currentUser?.uid ?: ""
                    val nombreProfesional = currentUser?.name ?: ""

                    Log.d("CrearPlan", "profesionalId final: '$profesionalId'")
                    Log.d("CrearPlan", "nombreProfesional final: '$nombreProfesional'")

                    if (profesionalId.isEmpty()) {
                        Log.e("CrearPlan", "❌ PROBLEMA: profesionalId está vacío!")
                        Toast.makeText(context, "Error: No se pudo identificar al profesional", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (nombreProfesional.isEmpty()) {
                        Log.w("CrearPlan", "⚠️ ADVERTENCIA: nombreProfesional está vacío!")
                    }

                    val plan = PlanEntrenamiento(
                        usuarioId = solicitud.usuarioId,
                        profesionalId = profesionalId,
                        nombreProfesional = nombreProfesional,
                        tipoEjercicio = tipoEjercicio,
                        lugarRealizacion = lugarRealizacion,
                        materialesSugeridos = materialesSugeridos,
                        frecuencia = frecuencia,
                        dificultad = dificultad,
                        duracionEstimada = duracionSegundos,
                        ejercicios = ejercicios,
                        observaciones = observaciones
                    )

                    Log.d("CrearPlan", "Plan creado con profesionalId: '${plan.profesionalId}' y nombre: '${plan.nombreProfesional}'")

                    planesViewModel.crearPlanEntrenamiento(solicitud.id, plan)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && ejercicios.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Plan de Entrenamiento")
            }
        }
    }
}

@Composable
private fun EjercicioCard(
    ejercicio: Ejercicio,
    onEjercicioChanged: (Ejercicio) -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ejercicio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar ejercicio",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ejercicio.nombreEjercicio,
                onValueChange = { onEjercicioChanged(ejercicio.copy(nombreEjercicio = it)) },
                label = { Text("Nombre del ejercicio") },
                placeholder = { Text("Ej: Press de banca, Sentadillas, Flexiones") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Músculo trabajado
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Músculo:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    listOf("Superior", "Intermedio", "Inferior").forEach { musculo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = ejercicio.musculoTrabajado == musculo,
                                onClick = { onEjercicioChanged(ejercicio.copy(musculoTrabajado = musculo)) }
                            )
                            Text(musculo, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Repeticiones
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Repeticiones:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    listOf("2x10", "3x12", "4x15", "1x20").forEach { rep ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = ejercicio.repeticiones == rep,
                                onClick = { onEjercicioChanged(ejercicio.copy(repeticiones = rep)) }
                            )
                            Text(rep, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ejercicio.pesoRecomendado.toString().takeIf { it != "0.0" } ?: "",
                    onValueChange = {
                        val peso = it.toFloatOrNull() ?: 0f
                        onEjercicioChanged(ejercicio.copy(pesoRecomendado = peso))
                    },
                    label = { Text("Peso (kg)") },
                    placeholder = { Text("15.5") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ejercicio.observaciones,
                    onValueChange = { onEjercicioChanged(ejercicio.copy(observaciones = it)) },
                    label = { Text("Observaciones") },
                    placeholder = { Text("Técnica, descanso...") },
                    modifier = Modifier.weight(2f),
                    maxLines = 2
                )
            }
        }
    }
}