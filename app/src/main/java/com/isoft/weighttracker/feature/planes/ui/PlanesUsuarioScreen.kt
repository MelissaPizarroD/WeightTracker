package com.isoft.weighttracker.feature.planes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.feature.planes.model.PlanEntrenamiento
import java.text.SimpleDateFormat
import java.util.*

fun formatFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesUsuarioScreen(
    navController: NavController,
    userId: String,
    userName: String,
    rol: String,
    viewModel: PlanesViewModel = viewModel()
) {
    val planes by viewModel.planesEntrenamiento.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        viewModel.cargarPlanesEntrenamientoDeUsuario(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes de $userName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (rol == "entrenador") {
                if (planes.isEmpty()) {
                    Text(
                        text = "❌ No hay planes creados para este usuario.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(planes) { plan ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("📋 ${plan.nombrePlan}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("🕒 Fecha creación: ${formatFecha(plan.fechaCreacion)}")
                                    Text("🎯 Objetivo: ${plan.objetivo}")
                                    Text("🗓️ Duración: ${plan.duracionSemanas} semanas, ${plan.frecuenciaSemanal}x/semana")
                                    Text("📅 Sesiones: ${plan.sesiones.size}")
                                    Spacer(modifier = Modifier.height(8.dp))

                                    plan.sesiones.forEach { sesion ->
                                        Text("🧠 ${sesion.nombre} (${sesion.dia})", fontWeight = FontWeight.SemiBold)
                                        sesion.ejercicios.forEach { ejercicio ->
                                            Text("• ${ejercicio.nombre} - ${ejercicio.series} x ${ejercicio.repeticiones}")
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "⚠️ Este módulo está disponible solo para entrenadores.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}