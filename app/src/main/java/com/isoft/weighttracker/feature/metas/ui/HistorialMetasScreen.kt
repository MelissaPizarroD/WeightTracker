package com.isoft.weighttracker.feature.metas.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.isoft.weighttracker.core.notifications.MetaNotificationHelper
import com.isoft.weighttracker.feature.metas.model.Meta
import com.isoft.weighttracker.feature.metas.viewmodel.MetasViewModel
import com.isoft.weighttracker.feature.metas.viewmodel.ProgresoMeta
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MetaReactivacion(
    val meta: Meta,
    val requiereNuevaFecha: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialMetasScreen(
    navController: NavController,
    metasViewModel: MetasViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val metas by metasViewModel.metas.collectAsState()
    val metaActiva by metasViewModel.metaActiva.collectAsState()
    val progreso by metasViewModel.progreso.collectAsState()
    val alerta by metasViewModel.alerta.collectAsState()
    val eventoMeta by metasViewModel.eventoMeta.collectAsState()

    var metaAEliminar by remember { mutableStateOf<Meta?>(null) }
    var mostrarMetaActiva by remember { mutableStateOf(true) }
    var mostrarHistorial by remember { mutableStateOf(true) }
    var metaReactivacion by remember { mutableStateOf<MetaReactivacion?>(null) }
    var metaAExtender by remember { mutableStateOf<Meta?>(null) }

    val formatoFecha = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        metasViewModel.cargarMetas()
    }

    LaunchedEffect(alerta) {
        alerta?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                metasViewModel.clearAlerta()
            }
        }
    }

    LaunchedEffect(eventoMeta) {
        eventoMeta?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)

                if (it.contains("cumplida", ignoreCase = true)) {
                    MetaNotificationHelper.enviarNotificacionMeta(
                        context,
                        "🎯 ¡Meta cumplida!",
                        "Felicidades, lo lograste 💪"
                    )
                }

                if (it.contains("venció", ignoreCase = true) || it.contains("vencida", ignoreCase = true)) {
                    MetaNotificationHelper.enviarNotificacionMeta(
                        context,
                        "⏰ Meta vencida",
                        "Puedes reactivarla con una nueva fecha"
                    )
                }

                metasViewModel.clearEventoMeta()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Metas") },
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
            FloatingActionButton(onClick = { navController.navigate("registrarMeta") }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva meta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val metaActivaSnapshot = metaActiva

            if (metaActivaSnapshot != null) {
                MetaActivaSection(
                    meta = metaActivaSnapshot,
                    progreso = progreso,
                    mostrarMetaActiva = mostrarMetaActiva,
                    formatoFecha = formatoFecha,
                    onToggleVisibility = { mostrarMetaActiva = !mostrarMetaActiva },
                    onMarcarCumplida = {
                        scope.launch {
                            metasViewModel.marcarMetaComoCumplida(metaActivaSnapshot.id ?: "")
                        }
                    },
                    onDetenerMeta = {
                        scope.launch {
                            metasViewModel.detenerMeta(metaActivaSnapshot.id ?: "")
                        }
                    },
                    onRegistrarPeso = {
                        navController.navigate("registroAntropometrico")
                    },
                    onExtenderFecha = { metaAExtender = metaActivaSnapshot }
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Actualmente no tienes ninguna meta activa.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toca ➕ para crear una nueva meta y comenzar a avanzar 💪",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HistorialSection(
                metas = metas,
                mostrarHistorial = mostrarHistorial,
                formatoFecha = formatoFecha,
                onToggleVisibility = { mostrarHistorial = !mostrarHistorial },
                onEliminarMeta = { metaAEliminar = it },
                onReactivarMeta = { meta ->
                    val ahora = System.currentTimeMillis()
                    if (meta.fechaLimite < ahora) {
                        metaReactivacion = MetaReactivacion(meta, true)
                    } else {
                        metasViewModel.reactivarMeta(meta.id ?: "")
                    }
                }
            )
        }

        metaAEliminar?.let { meta ->
            DeleteConfirmationDialog(
                meta = meta,
                onConfirm = {
                    scope.launch {
                        val eliminado = metasViewModel.eliminarMetaInternal(meta.id ?: "")
                        metaAEliminar = null
                        if (eliminado) {
                            snackbarHostState.showSnackbar("Meta eliminada ✔️")
                        } else {
                            snackbarHostState.showSnackbar("Error al eliminar meta")
                        }
                    }
                },
                onDismiss = { metaAEliminar = null }
            )
        }

        metaReactivacion?.takeIf { it.requiereNuevaFecha }?.let { target ->
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59, 59)
                    val nuevaFecha = calendar.timeInMillis
                    scope.launch {
                        metasViewModel.actualizarFechaLimite(target.meta.id ?: "", nuevaFecha)
                        metasViewModel.reactivarMeta(target.meta.id ?: "")
                    }
                    metaReactivacion = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() + 86400000
                setOnCancelListener { metaReactivacion = null }
            }.show()
        }

        metaAExtender?.let { target ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = target.fechaLimite
            }

            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59, 59)
                    val nuevaFecha = calendar.timeInMillis
                    scope.launch {
                        metasViewModel.actualizarFechaLimite(target.id ?: "", nuevaFecha)
                        metasViewModel.cargarMetas()
                    }
                    metaAExtender = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() + 86400000
                setOnCancelListener { metaAExtender = null }
            }.show()
        }
    }
}

@Composable
private fun MetaActivaSection(
    meta: Meta,
    progreso: ProgresoMeta?,
    mostrarMetaActiva: Boolean,
    formatoFecha: SimpleDateFormat,
    onToggleVisibility: () -> Unit,
    onMarcarCumplida: () -> Unit,
    onDetenerMeta: () -> Unit,
    onRegistrarPeso: () -> Unit,
    onExtenderFecha: () -> Unit
) {
    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (meta.cumplida)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else if (progreso?.enProgreso == true)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (meta.cumplida) Icons.Default.CheckCircle else Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = if (meta.cumplida)
                        MaterialTheme.colorScheme.primary
                    else if (progreso?.enProgreso == true)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (meta.cumplida) "Meta Cumplida ✅" else "Meta Activa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (mostrarMetaActiva) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarMetaActiva) {
                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Text("🎯 Objetivo: ${meta.objetivo.replaceFirstChar { it.uppercase() }}")
                    Text("⚖️ ${meta.pesoInicial} kg → ${meta.pesoObjetivo} kg")
                    Text("📅 Hasta: ${formatoFecha.format(Date(meta.fechaLimite))}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (progreso != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progreso: ${progreso.porcentajeProgreso.toInt()}%")
                            Text("Faltan: ${String.format("%.1f kg", progreso.pesoRestante)}")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { progreso.porcentajeProgreso / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )

                        UmbralMetaInfo()

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Peso actual: ${progreso.pesoActual} kg")
                            Text("Días restantes: ${progreso.diasRestantes}")
                        }
                    }
                } else {
                    Text("📉 Aún no hay registros de progreso.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRegistrarPeso,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar peso")
                    }
                }

                if (!meta.cumplida) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (progreso != null && !progreso.enProgreso && progreso.pesoRestante <= 0.5f) {
                            Button(
                                onClick = onMarcarCumplida,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("¡Cumplida!")
                            }
                        }

                        OutlinedButton(
                            onClick = onDetenerMeta,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Detener")
                        }

                        OutlinedButton(
                            onClick = onExtenderFecha,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Extender fecha")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialSection(
    metas: List<Meta>,
    mostrarHistorial: Boolean,
    formatoFecha: SimpleDateFormat,
    onToggleVisibility: () -> Unit,
    onEliminarMeta: (Meta) -> Unit,
    onReactivarMeta: (Meta) -> Unit
) {
    val metasHistorial = metas.filter { it.id != null }

    Card(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Historial de metas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                if (metasHistorial.isNotEmpty()) {
                    Badge { Text("${metasHistorial.size}") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    if (mostrarHistorial) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (mostrarHistorial) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(metasHistorial) { meta ->
                        MetaItem(
                            meta = meta,
                            formatoFecha = formatoFecha,
                            onDelete = onEliminarMeta,
                            onReactivar = onReactivarMeta
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaItem(
    meta: Meta,
    formatoFecha: SimpleDateFormat,
    onDelete: (Meta) -> Unit,
    onReactivar: (Meta) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val vencida = !meta.activa && !meta.cumplida && meta.fechaLimite < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🎯 ${meta.objetivo.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inicio: ${formatoFecha.format(Date(meta.fechaInicio))}")
                Text("Límite: ${formatoFecha.format(Date(meta.fechaLimite))}")
                Text(
                    "Estado: ${
                        when {
                            meta.cumplida -> "✅ Cumplida"
                            vencida -> "🕓 Vencida"
                            !meta.activa -> "⏸️ Detenida"
                            else -> "⏳ En progreso"
                        }
                    }"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (!meta.activa && !meta.cumplida) {
                        OutlinedButton(onClick = { onReactivar(meta) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reactivar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(onClick = { onDelete(meta) }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    meta: Meta,
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
        title = { Text("¿Eliminar meta?") },
        text = {
            Text("¿Seguro que deseas eliminar esta meta? Esta acción no se puede deshacer.")
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
fun UmbralMetaInfo() {
    AssistChip(
        onClick = { /* podrías mostrar un dialog con más detalles si quieres */ },
        label = {
            Text("✔️ Tu meta se cumple si estás a ±0.5 kg del objetivo")
        },
        leadingIcon = {
            Icon(Icons.Default.Info, contentDescription = null)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.padding(top = 8.dp)
    )
}