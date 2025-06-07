package com.isoft.weighttracker.feature.actividadfisica.ui

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.model.RegistroPasos
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import com.isoft.weighttracker.core.permissions.PermissionViewModel
import com.isoft.weighttracker.core.permissions.PermissionState
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialActividadFisicaScreen(
    navController: NavController,
    viewModel: ActividadFisicaViewModel = viewModel(),
    permissionViewModel: PermissionViewModel,
    requestPermission: (String) -> Unit
) {
    val context = LocalContext.current
    val actividades by viewModel.actividades.collectAsState()
    val historialPasos by viewModel.historialPasos.collectAsState()
    val pasosHoy by viewModel.pasos.collectAsState()
    val contadorActivo by viewModel.contadorPasosActivo.collectAsState()
    val sincronizado by viewModel.pasosSincronizados.collectAsState()
    val sensorDisponible by viewModel.sensorDisponible.collectAsState()
    val error by viewModel.error.collectAsState()
    val permissionState by permissionViewModel.activityRecognitionPermission.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var actividadAEliminar by remember { mutableStateOf<ActividadFisica?>(null) }
    var mostrarActividad by remember { mutableStateOf(true) }
    var mostrarPasos by remember { mutableStateOf(false) }
    var quiereActivarContador by remember { mutableStateOf(false) }

    // Reacción al cambio de permiso
    LaunchedEffect(permissionState) {
        if (quiereActivarContador && permissionState == PermissionState.GRANTED) {
            viewModel.toggleContadorPasos(true)
            quiereActivarContador = false
        }
    }

    // Manejar errores
    LaunchedEffect(error) {
        error?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarError()
        }
    }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        viewModel.cargarActividades()
        viewModel.cargarHistorialPasos()
        viewModel.prepararContadorSiEsNecesario()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividad Física y Pasos") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("registrarActividad") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar actividad")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Sección contador de pasos mejorada
            StepCounterSection(
                contadorActivo = contadorActivo,
                pasosHoy = pasosHoy,
                sincronizado = sincronizado,
                sensorDisponible = sensorDisponible,
                permissionState = permissionState,
                onToggleContador = { activo ->
                    if (activo) {
                        if (permissionState != PermissionState.GRANTED) {
                            requestPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                            quiereActivarContador = true
                            return@StepCounterSection
                        }
                        viewModel.toggleContadorPasos(true)
                    } else {
                        viewModel.toggleContadorPasos(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable: Actividad Física
            ActivitySection(
                mostrarActividad = mostrarActividad,
                actividades = actividades,
                onToggleVisibility = { mostrarActividad = !mostrarActividad },
                onEditActividad = { actividad ->
                    val json = URLEncoder.encode(Gson().toJson(actividad), "UTF-8")
                    navController.navigate("registrarActividad?actividad=$json")
                },
                onDeleteActividad = { actividadAEliminar = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable: Historial pasos
            StepsHistorySection(
                mostrarPasos = mostrarPasos,
                historialPasos = historialPasos,
                onToggleVisibility = { mostrarPasos = !mostrarPasos }
            )
        }

        // Confirmación de eliminación mejorada
        actividadAEliminar?.let { actividad ->
            DeleteConfirmationDialog(
                actividad = actividad,
                onConfirm = {
                    actividadAEliminar = null
                    scope.launch {
                        val eliminada = viewModel.eliminarActividad(actividad.id ?: "")
                        if (eliminada) {
                            val result = snackbarHostState.showSnackbar(
                                message = "Actividad eliminada",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.registrarNuevaActividad(actividad) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Actividad restaurada ✔️")
                                    }
                                }
                            }
                        } else {
                            snackbarHostState.showSnackbar("Error al eliminar actividad")
                        }
                    }
                },
                onDismiss = { actividadAEliminar = null }
            )
        }
    }
}

@Composable
private fun StepCounterSection(
    contadorActivo: Boolean,
    pasosHoy: Int,
    sincronizado: Boolean,
    sensorDisponible: Boolean,
    permissionState: PermissionState,
    onToggleContador: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (contadorActivo)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Switch contador pasos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = if (contadorActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Contador de pasos diario",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (contadorActivo) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = contadorActivo,
                    onCheckedChange = onToggleContador
                )
            }

            if (contadorActivo) {
                Spacer(modifier = Modifier.height(12.dp))

                // Contador de pasos destacado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "👟 Pasos hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "$pasosHoy",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Estado de sincronización
                    Column(horizontalAlignment = Alignment.End) {
                        if (sincronizado) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Sincronizado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Guardando...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Advertencias o información adicional
                if (!sensorDisponible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Este dispositivo no tiene sensor de pasos",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (pasosHoy == 0 && sincronizado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "¡Comienza a caminar para registrar pasos!",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySection(
    mostrarActividad: Boolean,
    actividades: List<ActividadFisica>,
    onToggleVisibility: () -> Unit,
    onEditActividad: (ActividadFisica) -> Unit,
    onDeleteActividad: (ActividadFisica) -> Unit
) {
    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Actividad física registrada", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (actividades.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("${actividades.size}")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (mostrarActividad) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarActividad) {
                if (actividades.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No hay registros de actividad física.", style = MaterialTheme.typography.bodyMedium)
                        Text("Toca ➕ para registrar una actividad", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(actividades.size) { index ->
                            val actividad = actividades[index]
                            ActividadFisicaItem(
                                actividad = actividad,
                                onEdit = onEditActividad,
                                onDelete = onDeleteActividad
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsHistorySection(
    mostrarPasos: Boolean,
    historialPasos: List<RegistroPasos>,
    onToggleVisibility: () -> Unit
) {
    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Historial de pasos diarios", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (historialPasos.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text("${historialPasos.size}")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (mostrarPasos) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarPasos) {
                if (historialPasos.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin historial de pasos",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Activa el contador y comienza a caminar 👟",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(historialPasos.size) { index ->
                            val registro = historialPasos[index]
                            RegistroPasosItem(registro)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    actividad: ActividadFisica,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("¿Eliminar actividad?") },
        text = {
            Text("¿Seguro que deseas eliminar el registro de '${actividad.tipo}'? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ActividadFisicaItem(
    actividad: ActividadFisica,
    onEdit: (ActividadFisica) -> Unit,
    onDelete: (ActividadFisica) -> Unit
) {
    val fechaStr = actividad.fecha.toFormattedDate()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🏃 ${actividad.tipo}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "⏱️ ${actividad.duracionMin} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "🔥 ${actividad.caloriasQuemadas} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "🕓 $fechaStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEdit(actividad) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(actividad) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun RegistroPasosItem(registro: RegistroPasos) {
    val fechaStr = registro.fecha.toFormattedDate("dd MMM yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    fechaStr,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Registro diario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${registro.pasos}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " pasos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Función de extensión para formatear fechas
fun Long.toFormattedDate(pattern: String = "dd MMM yyyy - HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}