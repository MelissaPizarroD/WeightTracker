package com.isoft.weighttracker.feature.planes.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.isoft.weighttracker.feature.planes.model.ComidaDiaria
import com.isoft.weighttracker.feature.planes.model.PlanNutricional
import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlanNutricionalScreen(
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

    // Estados para el plan nutricional
    var frecuencia by remember { mutableStateOf("Lunes a Sábado") }
    var repeticion by remember { mutableStateOf("diaria") }
    var alimentosNoPermitidos by remember { mutableStateOf("") }
    var bebidasNoPermitidas by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Estados para las comidas
    var desayuno by remember { mutableStateOf(ComidaDiaria(nombre = "Desayuno")) }
    var mediaMañana by remember { mutableStateOf(ComidaDiaria(nombre = "Media Mañana")) }
    var almuerzo by remember { mutableStateOf(ComidaDiaria(nombre = "Almuerzo")) }
    var mediaTarde by remember { mutableStateOf(ComidaDiaria(nombre = "Media Tarde")) }
    var cena by remember { mutableStateOf(ComidaDiaria(nombre = "Cena")) }

    // ✅ NUEVOS: Estados para datos del usuario
    var personaProfile by remember { mutableStateOf<PersonaProfile?>(null) }
    var antropometriaReciente by remember { mutableStateOf<Antropometria?>(null) }

    // ✅ AGREGADO: Cargar usuario profesional
    LaunchedEffect(Unit) {
        userViewModel.loadUser() // ← FIX PARA QUE APAREZCA EL PROFESIONAL
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
                Log.e("CrearPlanNutricional", "Error cargando datos del usuario", e)
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
                title = { Text("Crear Plan Nutricional") },
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

                        // ✅ ANTECEDENTES MÉDICOS - Lo más importante
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
                        listOf("Lunes a Viernes", "Lunes a Sábado", "Lunes a Domingo").forEach { opcion ->
                            FilterChip(
                                onClick = { frecuencia = opcion },
                                label = { Text(opcion) },
                                selected = frecuencia == opcion
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Repetición
                    Text(
                        "Repetición:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("diaria", "cada 2 dias", "cada 3 dias").forEach { opcion ->
                            FilterChip(
                                onClick = { repeticion = opcion },
                                label = { Text(opcion) },
                                selected = repeticion == opcion
                            )
                        }
                    }
                }
            }

            // Comidas del día
            Text(
                "🍽️ Plan de Comidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ComidaCard(
                comida = desayuno,
                onComidaChanged = { desayuno = it }
            )

            ComidaCard(
                comida = mediaMañana,
                onComidaChanged = { mediaMañana = it }
            )

            ComidaCard(
                comida = almuerzo,
                onComidaChanged = { almuerzo = it }
            )

            ComidaCard(
                comida = mediaTarde,
                onComidaChanged = { mediaTarde = it }
            )

            ComidaCard(
                comida = cena,
                onComidaChanged = { cena = it }
            )

            // Restricciones
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🚫 Restricciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = alimentosNoPermitidos,
                        onValueChange = { alimentosNoPermitidos = it },
                        label = { Text("Alimentos no permitidos") },
                        placeholder = { Text("Ej: lácteos, derivados de la leche, harinas procesadas") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bebidasNoPermitidas,
                        onValueChange = { bebidasNoPermitidas = it },
                        label = { Text("Bebidas no permitidas") },
                        placeholder = { Text("Ej: gaseosas, tés dulces, bebidas azucaradas") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
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
                        placeholder = { Text("Recomendaciones especiales, notas importantes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }

            // ✅ BOTÓN ACTUALIZADO: Con debugging y validación del profesional
            Button(
                onClick = {
                    // ✅ DEBUG: Verificar datos del profesional
                    Log.d("CrearPlan", "=== DEBUG DATOS PROFESIONAL ===")
                    Log.d("CrearPlan", "currentUser: $currentUser")
                    Log.d("CrearPlan", "currentUser?.uid: ${currentUser?.uid}")
                    Log.d("CrearPlan", "currentUser?.name: ${currentUser?.name}")

                    val profesionalId = currentUser?.uid ?: ""
                    val nombreProfesional = currentUser?.name ?: ""

                    Log.d("CrearPlan", "profesionalId final: '$profesionalId'")
                    Log.d("CrearPlan", "nombreProfesional final: '$nombreProfesional'")

                    if (profesionalId.isEmpty()) {
                        Log.e("CrearPlan", "❌ PROBLEMA: profesionalId está vacío!")
                        Toast.makeText(context, "Error: No se pudo identificar al profesional", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val plan = PlanNutricional(
                        usuarioId = solicitud.usuarioId,
                        profesionalId = profesionalId,
                        nombreProfesional = nombreProfesional,
                        frecuencia = frecuencia,
                        repeticion = repeticion,
                        desayuno = desayuno,
                        mediaMañana = mediaMañana,
                        almuerzo = almuerzo,
                        mediaTarde = mediaTarde,
                        cena = cena,
                        alimentosNoPermitidos = alimentosNoPermitidos,
                        bebidasNoPermitidas = bebidasNoPermitidas,
                        observaciones = observaciones
                    )

                    Log.d("CrearPlan", "Plan nutricional creado con profesionalId: '${plan.profesionalId}' y nombre: '${plan.nombreProfesional}'")

                    planesViewModel.crearPlanNutricional(solicitud.id, plan)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && desayuno.contenido.isNotBlank()
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
                Text("Crear Plan Nutricional")
            }
        }
    }
}

@Composable
private fun ComidaCard(
    comida: ComidaDiaria,
    onComidaChanged: (ComidaDiaria) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                comida.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = comida.horaSugerida,
                    onValueChange = { onComidaChanged(comida.copy(horaSugerida = it)) },
                    label = { Text("Hora") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = comida.porciones,
                    onValueChange = { onComidaChanged(comida.copy(porciones = it)) },
                    label = { Text("Porciones") },
                    placeholder = { Text("1 taza") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = comida.contenido,
                onValueChange = { onComidaChanged(comida.copy(contenido = it)) },
                label = { Text("Contenido de la comida") },
                placeholder = {
                    Text(
                        when (comida.nombre) {
                            "Desayuno" -> "Ej: Avena con frutas, yogur natural, café sin azúcar"
                            "Media Mañana" -> "Ej: Fruta fresca, frutos secos"
                            "Almuerzo" -> "Ej: Pollo a la plancha, arroz integral, ensalada verde"
                            "Media Tarde" -> "Ej: Té verde, galletas integrales"
                            "Cena" -> "Ej: Pescado al vapor, verduras al horno"
                            else -> "Describe el contenido de esta comida"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}