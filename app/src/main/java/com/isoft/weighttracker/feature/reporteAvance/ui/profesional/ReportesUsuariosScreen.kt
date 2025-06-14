package com.isoft.weighttracker.feature.reporteAvance.ui.profesional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Person
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
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.profesional.viewmodel.ProfesionalViewModel
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.shared.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesUsuarioScreen(
    navController: NavController,
    usuario: User,
    onBack: () -> Unit,
    profesionalViewModel: ProfesionalViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val reportes by profesionalViewModel.reportesUsuario.collectAsState()
    val isLoading by profesionalViewModel.isLoading.collectAsState()
    val error by profesionalViewModel.error.collectAsState()

    // ✅ FIX: Extraer valores específicos para evitar smart cast issues
    val currentUser by userViewModel.currentUser.collectAsState()
    val profesionalName = currentUser?.name ?: "Profesional"
    val profesionalRole = currentUser?.role ?: "profesional"
    val profesionalEmail = currentUser?.email ?: ""

    // Función para verificar si este profesional específico ya retroalimentó
    fun yaRetroalimentoProfesional(reporte: ReporteAvance, profesionalId: String): Boolean {
        return reporte.retroalimentaciones.any { it.idProfesional == profesionalId }
    }

    // Filtrar reportes pendientes por profesional específico
    val profesionalId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val reportesPendientes = reportes.filter { reporte ->
        !yaRetroalimentoProfesional(reporte, profesionalId)
    }
    val reportesRevisados = reportes.filter { reporte ->
        yaRetroalimentoProfesional(reporte, profesionalId)
    }

    LaunchedEffect(usuario.uid) {
        profesionalViewModel.cargarReportesDeUsuario(usuario.uid)
        userViewModel.loadUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reportes de ${usuario.name}")
                        Text(
                            usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Información del usuario con estadísticas por profesional específico
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Reportes del usuario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                "${reportesPendientes.size} pendientes para ti • ${reportesRevisados.size} ya revisados por ti",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // ✅ FIX: Usar las variables extraídas en lugar de currentUser directamente
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (profesionalRole.lowercase()) {
                                "nutricionista" -> "🥗"
                                "entrenador" -> "💪"
                                "medico" -> "👨‍⚕️"
                                else -> "👨‍💼"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Revisando como: $profesionalName (${profesionalRole.replaceFirstChar { it.uppercase() }})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando reportes...")
                        }
                    }
                }

                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Error al cargar reportes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                reportes.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Este usuario no tiene reportes aún",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Los reportes aparecerán aquí cuando el usuario los genere",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                reportesPendientes.isEmpty() && reportesRevisados.isNotEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "¡Todos los reportes están revisados!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "No tienes reportes pendientes de retroalimentación",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Reportes ya revisados por ti:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(reportesRevisados) { reporte ->
                            ReporteCardProfesional(
                                reporte = reporte,
                                usuario = usuario,
                                profesionalId = profesionalId,
                                onClick = {
                                    navController.navigate("retroalimentacion/${reporte.id}/${usuario.uid}")
                                }
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (reportesPendientes.isNotEmpty()) {
                            item {
                                Text(
                                    "⏳ Pendientes de tu retroalimentación:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722)
                                )
                            }

                            items(reportesPendientes) { reporte ->
                                ReporteCardProfesional(
                                    reporte = reporte,
                                    usuario = usuario,
                                    profesionalId = profesionalId,
                                    onClick = {
                                        navController.navigate("retroalimentacion/${reporte.id}/${usuario.uid}")
                                    }
                                )
                            }

                            if (reportesRevisados.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "✅ Ya revisados por ti:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }

                        items(reportesRevisados) { reporte ->
                            ReporteCardProfesional(
                                reporte = reporte,
                                usuario = usuario,
                                profesionalId = profesionalId,
                                onClick = {
                                    navController.navigate("retroalimentacion/${reporte.id}/${usuario.uid}")
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
private fun ReporteCardProfesional(
    reporte: ReporteAvance,
    usuario: User,
    profesionalId: String,
    onClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaInicio = sdf.format(Date(reporte.fechaInicio))
    val fechaFin = sdf.format(Date(reporte.fechaFin))

    val yaRetroalimenteEste = reporte.retroalimentaciones.any { it.idProfesional == profesionalId }
    val totalRetroalimentaciones = reporte.retroalimentaciones.size
    val misRetroalimentaciones = reporte.retroalimentaciones.count { it.idProfesional == profesionalId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (yaRetroalimenteEste)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📅 $fechaInicio - $fechaFin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tipo: ${reporte.tipoReporte.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = if (yaRetroalimenteEste) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (yaRetroalimenteEste) "✅ Ya revisado" else "⏳ Pendiente",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    if (totalRetroalimentaciones > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (misRetroalimentaciones > 0) {
                                "💬 $misRetroalimentaciones tuyas de $totalRetroalimentaciones total"
                            } else {
                                "💬 $totalRetroalimentaciones de otros profesionales"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricaReporteProfesional("🔥", "${reporte.caloriasQuemadas} cal")
                MetricaReporteProfesional("🚶‍♂️", "${"%,d".format(reporte.pasosTotales)} pasos")
                reporte.progresoMeta?.let { progreso ->
                    MetricaReporteProfesional("🎯", "%.1f%%".format(progreso.porcentajeProgreso))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (yaRetroalimenteEste) {
                    "✨ Toca para revisar o agregar más comentarios"
                } else {
                    "👆 Toca para agregar tu retroalimentación"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (yaRetroalimenteEste)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.primary,
                fontWeight = if (yaRetroalimenteEste) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MetricaReporteProfesional(icono: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icono, style = MaterialTheme.typography.titleMedium)
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}