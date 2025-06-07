package com.isoft.weighttracker.feature.actividadfisica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.viewmodel.ActividadFisicaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarActividadFisicaScreen(
    navController: NavController,
    actividadExistente: ActividadFisica? = null,
    viewModel: ActividadFisicaViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tipo by remember { mutableStateOf(actividadExistente?.tipo ?: "") }
    var duracion by remember { mutableStateOf(actividadExistente?.duracionMin?.toString() ?: "") }
    var calorias by remember { mutableStateOf(actividadExistente?.caloriasQuemadas?.toString() ?: "") }

    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isFormValid = tipo.isNotBlank() &&
            duracion.toIntOrNull()?.let { it > 0 } == true &&
            calorias.toIntOrNull()?.let { it > 0 } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (actividadExistente != null) "Editar Actividad" else "Registrar Actividad")
                },
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
            // 🏃 Tipo de actividad
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo de actividad") },
                isError = showErrors && tipo.isBlank(),
                supportingText = {
                    if (showErrors && tipo.isBlank()) Text("Este campo es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ⏱️ Duración
            OutlinedTextField(
                value = duracion,
                onValueChange = { duracion = it.filter { c -> c.isDigit() } },
                label = { Text("Duración (minutos)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = showErrors && (duracion.toIntOrNull()?.let { it > 0 } != true),
                supportingText = {
                    if (showErrors && duracion.toIntOrNull()?.let { it > 0 } != true)
                        Text("Debe ser mayor a 0")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔥 Calorías
            OutlinedTextField(
                value = calorias,
                onValueChange = { calorias = it.filter { c -> c.isDigit() } },
                label = { Text("Calorías quemadas") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = showErrors && (calorias.toIntOrNull()?.let { it > 0 } != true),
                supportingText = {
                    if (showErrors && calorias.toIntOrNull()?.let { it > 0 } != true)
                        Text("Debe ser mayor a 0")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // ✅ Botón guardar con loading style
            Button(
                onClick = {
                    if (!isFormValid || isSaving) {
                        showErrors = true
                        return@Button
                    }

                    isSaving = true

                    val nuevaActividad = ActividadFisica(
                        id = actividadExistente?.id,
                        tipo = tipo,
                        duracionMin = duracion.toInt(),
                        caloriasQuemadas = calorias.toInt(),
                        fecha = actividadExistente?.fecha ?: System.currentTimeMillis()
                    )

                    if (actividadExistente != null) {
                        viewModel.actualizarActividad(nuevaActividad) {
                            navController.popBackStack()
                            scope.launch {
                                snackbarHostState.showSnackbar("Actividad actualizada ✅")
                            }
                            isSaving = false
                        }
                    } else {
                        viewModel.registrarNuevaActividad(nuevaActividad) {
                            navController.popBackStack()
                            scope.launch {
                                snackbarHostState.showSnackbar("Actividad registrada 💪")
                            }
                            isSaving = false
                        }
                    }
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

                Text(
                    text = if (isSaving) "Guardando..." else if (actividadExistente != null) "Actualizar" else "Guardar",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}