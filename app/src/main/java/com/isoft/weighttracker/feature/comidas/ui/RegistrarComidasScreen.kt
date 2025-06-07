package com.isoft.weighttracker.feature.comidas.ui

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
import com.isoft.weighttracker.feature.comidas.model.Comida
import com.isoft.weighttracker.feature.comidas.viewmodel.ComidaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarComidasScreen(
    navController: NavController,
    comidaExistente: Comida? = null,
    viewModel: ComidaViewModel = viewModel()
) {
    var comida by remember { mutableStateOf(comidaExistente?.comida ?: "") }
    var calorias by remember { mutableStateOf(comidaExistente?.calorias?.toString() ?: "") }
    val opcionesComidaDelDia = listOf("Desayuno", "Media mañana", "Almuerzo", "Media tarde", "Cena")
    var comidaDelDia by remember { mutableStateOf(comidaExistente?.comidaDelDia ?: opcionesComidaDelDia.first()) }
    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isFormValid = comida.isNotBlank() && calorias.isNotBlank() && calorias.all { it.isDigit() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (comidaExistente != null) "Editar Comida" else "Registrar Comida")
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 🍽️ Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = comidaDelDia,
                            onValueChange = {},
                            label = { Text("Comida del día") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            opcionesComidaDelDia.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        comidaDelDia = opcion
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📝 ¿Qué comiste?
                    OutlinedTextField(
                        value = comida,
                        onValueChange = { comida = it },
                        label = { Text("¿Qué comiste?") },
                        isError = showErrors && comida.isBlank(),
                        supportingText = {
                            if (showErrors && comida.isBlank()) Text("Este campo es obligatorio")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔥 Calorías
                    OutlinedTextField(
                        value = calorias,
                        onValueChange = {
                            calorias = it.filter { c -> c.isDigit() }
                            if (showErrors) showErrors = false
                        },
                        label = { Text("Calorías") },
                        isError = showErrors && calorias.isBlank(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (showErrors && calorias.isBlank()) Text("Ingresa las calorías estimadas")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ✅ Botón guardar
            Button(
                onClick = {
                    if (!isFormValid || isSaving) {
                        showErrors = true
                        return@Button
                    }

                    isSaving = true

                    val nuevaComida = Comida(
                        id = comidaExistente?.id,
                        comida = comida,
                        comidaDelDia = comidaDelDia,
                        calorias = calorias.toInt(),
                        fecha = comidaExistente?.fecha ?: System.currentTimeMillis()
                    )

                    if (comidaExistente != null) {
                        viewModel.actualizarComida(nuevaComida) {
                            navController.popBackStack()
                            scope.launch {
                                snackbarHostState.showSnackbar("Comida actualizada ✅")
                            }
                            isSaving = false
                        }
                    } else {
                        viewModel.registrarNuevaComida(nuevaComida) {
                            navController.popBackStack()
                            scope.launch {
                                snackbarHostState.showSnackbar("Comida registrada 🎉")
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
                    text = if (isSaving) "Guardando..." else if (comidaExistente != null) "Actualizar" else "Guardar comida",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}