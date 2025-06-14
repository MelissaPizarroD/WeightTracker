package com.isoft.weighttracker.feature.planes.ui.persona

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.model.PlanEntrenamiento
import com.isoft.weighttracker.feature.planes.model.SesionEntrenamiento
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerPlanEntrenamientoScreen(
    navController: NavController,
    planId: String,
    planesViewModel: PlanesViewModel = viewModel()
) {
    val plan by planesViewModel.planActual.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()

    LaunchedEffect(planId) {
        planesViewModel.cargarPlanEntrenamiento(planId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ver Plan de Entrenamiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            plan?.let {
                PlanEntrenamientoDetail(plan = it)
            }
        }
    }
}

@Composable
fun PlanEntrenamientoDetail(plan: PlanEntrenamiento) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📋 ${plan.nombrePlan}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("👤 Profesional: ${plan.nombreProfesional}")
        Text("🎯 Objetivo: ${plan.objetivo}")
        Text("⏱️ Duración: ${plan.duracionSemanas} semanas")
        Text("📆 Frecuencia semanal: ${plan.frecuenciaSemanal}")

        if (plan.fechaActivacion != null) {
            val fecha = SimpleDateFormat("dd/MM/yyyy").format(plan.fechaActivacion)
            Text("✅ Activado el: $fecha")
        }

        Divider()

        Text("🗓️ Sesiones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        plan.sesiones.forEachIndexed { index, sesion ->
            SesionCard(index + 1, sesion)
        }
    }
}

@Composable
fun SesionCard(numeroSesion: Int, sesion: SesionEntrenamiento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🗓️ Sesión $numeroSesion: ${sesion.nombre}", fontWeight = FontWeight.Bold)
            Text("📍 Tipo: ${sesion.tipoSesion}")
            Text("📅 Día: ${sesion.dia}")
            Text("⏱️ Duración: ${sesion.duracionMinutos} minutos")

            Spacer(Modifier.height(8.dp))
            Text("🏋️ Ejercicios de esta sesión:", fontWeight = FontWeight.SemiBold)

            sesion.ejercicios.forEachIndexed { i, ej ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("🔸 ${ej.nombre}", fontWeight = FontWeight.Medium)
                    Text("🎯 Músculo: ${ej.musculoTrabajado}")
                    Text("📦 Series: ${ej.series} | 🔁 Reps: ${ej.repeticiones}")
                    Text("⏸️ Descanso: ${ej.descanso} min")
                    if (ej.observaciones.isNotBlank()) {
                        Text("📝 Notas: ${ej.observaciones}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (i < sesion.ejercicios.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

