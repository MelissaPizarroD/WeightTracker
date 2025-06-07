package com.isoft.weighttracker.feature.metas.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.metas.viewmodel.MetasViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarMetaScreen(
    navController: NavController,
    metasViewModel: MetasViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val alerta by metasViewModel.alerta.collectAsState()

    var pesoObjetivo by remember { mutableStateOf("") }
    var objetivoSeleccionado by remember { mutableStateOf("bajar") }
    var fechaLimite by remember { mutableStateOf(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)) }
    var isSaving by remember { mutableStateOf(false) }

    var pesoActual by remember { mutableStateOf(0f) }
    var recomendacion by remember { mutableStateOf("") }
    var isLoadingPeso by remember { mutableStateOf(true) }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                fechaLimite = calendar.timeInMillis
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        }
    }

    val isFormValid by remember(pesoObjetivo, objetivoSeleccionado, fechaLimite, pesoActual) {
        derivedStateOf {
            pesoObjetivo.isNotBlank() &&
                    pesoObjetivo.toFloatOrNull() != null &&
                    pesoObjetivo.toFloatOrNull()!! > 0 &&
                    pesoActual > 0 &&
                    fechaLimite > System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val peso = metasViewModel.obtenerUltimoPeso()
            pesoActual = peso ?: 0f
        } catch (e: Exception) {
            pesoActual = 0f
        } finally {
            isLoadingPeso = false
        }
    }

    LaunchedEffect(pesoObjetivo, objetivoSeleccionado, fechaLimite, pesoActual) {
        if (pesoObjetivo.isNotBlank() && pesoActual > 0) {
            val objetivo = pesoObjetivo.toFloatOrNull()
            if (objetivo != null && objetivo > 0) {
                val diasDisponibles = maxOf(1, (fechaLimite - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))
                recomendacion = metasViewModel.calcularRecomendacionSemanal(pesoActual, objetivo, diasDisponibles)
            } else {
                recomendacion = ""
            }
        } else {
            recomendacion = ""
        }
    }

    // ✅ Rehabilitar botón si el ViewModel muestra error
    LaunchedEffect(alerta) {
        alerta?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(mensaje)
                metasViewModel.clearAlerta()
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Meta") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (isLoadingPeso) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cargando peso actual...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (pesoActual > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tu peso actual: ${String.format("%.1f", pesoActual)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sin registros antropométricos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Necesitas al menos un registro para crear una meta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Text("¿Cuál es tu objetivo?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)

            Column(Modifier.selectableGroup()) {
                listOf("bajar" to "📉 Bajar de peso", "subir" to "📈 Subir de peso", "mantener" to "⚖️ Mantener peso").forEach { (valor, etiqueta) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = (objetivoSeleccionado == valor), onClick = { objetivoSeleccionado = valor }, role = Role.RadioButton)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (objetivoSeleccionado == valor), onClick = null)
                        Text(text = etiqueta, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            OutlinedTextField(
                value = pesoObjetivo,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?$"))) {
                        pesoObjetivo = newValue
                    }
                },
                label = { Text("Peso objetivo (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (pesoActual > 0 && pesoObjetivo.isNotBlank()) {
                        val objetivo = pesoObjetivo.toFloatOrNull()
                        if (objetivo != null && objetivo > 0) {
                            val diferencia = abs(objetivo - pesoActual)
                            Text("Diferencia: ${String.format("%.1f", diferencia)} kg")
                        }
                    }
                },
                isError = pesoObjetivo.isNotBlank() && (pesoObjetivo.toFloatOrNull() == null || pesoObjetivo.toFloatOrNull()!! <= 0),
                enabled = pesoActual > 0
            )

            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth(),
                enabled = pesoActual > 0
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fecha límite: ${formatoFecha.format(Date(fechaLimite))}")
            }

            if (pesoActual > 0) {
                val diasDisponibles = maxOf(0, (fechaLimite - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))
                Text("Tiempo disponible: $diasDisponibles días (${String.format("%.1f", diasDisponibles / 7.0)} semanas)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (recomendacion.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = when {
                        recomendacion.contains("🟢") -> MaterialTheme.colorScheme.primaryContainer
                        recomendacion.contains("🟡") -> MaterialTheme.colorScheme.secondaryContainer
                        recomendacion.contains("🟠") -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Evaluación de la meta:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(recomendacion, style = MaterialTheme.typography.bodyMedium)
                        if (recomendacion.contains("🔴")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("💡 Tip: Se recomienda perder/ganar máximo 0.5-1 kg por semana para mantener la salud.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isFormValid || isSaving) return@Button

                    val pesoMeta = pesoObjetivo.toFloat()
                    val esValido = when (objetivoSeleccionado) {
                        "bajar" -> pesoMeta < pesoActual
                        "subir" -> pesoMeta > pesoActual
                        "mantener" -> abs(pesoMeta - pesoActual) <= 0.5f
                        else -> false
                    }

                    if (!esValido) {
                        scope.launch {
                            snackbarHostState.showSnackbar("❌ Tu peso objetivo no es coherente con tu objetivo seleccionado.")
                        }
                        return@Button
                    }

                    isSaving = true

                    metasViewModel.guardarNuevaMeta(
                        pesoObjetivo = pesoMeta,
                        fechaLimite = fechaLimite,
                        objetivo = objetivoSeleccionado,
                        onSuccess = {
                            isSaving = false
                            navController.popBackStack()
                        }
                    )
                },
                enabled = isFormValid && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Guardando..." else "Crear Meta", style = MaterialTheme.typography.labelLarge)
            }

            if (pesoActual > 0) {
                Text("💡 Puedes modificar o detener tu meta desde la pantalla principal en cualquier momento.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}