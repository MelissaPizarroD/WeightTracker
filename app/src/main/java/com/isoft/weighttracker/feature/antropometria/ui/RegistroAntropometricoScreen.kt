package com.isoft.weighttracker.feature.antropometria.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.antropometria.viewmodel.AntropometriaViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAntropometricoScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    antropometriaViewModel: AntropometriaViewModel = viewModel(),
    registroExistente: Antropometria? = null
) {
    val personaState = userViewModel.personaProfile.collectAsState()
    val persona = personaState.value
    val alerta by antropometriaViewModel.alerta.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var peso by remember { mutableStateOf(registroExistente?.peso?.toString() ?: "") }
    var cintura by remember { mutableStateOf(registroExistente?.cintura?.toString() ?: "") }
    var cuello by remember { mutableStateOf(registroExistente?.cuello?.toString() ?: "") }
    var cadera by remember { mutableStateOf(registroExistente?.cadera?.toString() ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val isFormValid by remember(persona, peso, cintura, cuello, cadera) {
        derivedStateOf {
            persona != null &&
                    peso.isNotBlank() &&
                    cintura.isNotBlank() &&
                    cuello.isNotBlank() &&
                    (persona?.sexo?.lowercase() != "femenino" || cadera.isNotBlank())
        }
    }

    LaunchedEffect(Unit) {
        if (persona == null) userViewModel.loadPersonaProfile()
    }

    LaunchedEffect(alerta) {
        alerta?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                antropometriaViewModel.clearAlerta()
                isSaving = false // ✅ Reparar botón desactivado tras error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Antropométrico") },
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
        if (persona == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de información del perfil
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Edad: ${persona.edad} años")
                        Text("Sexo: ${persona.sexo}")
                        Text("Estatura: ${persona.estatura} cm")
                    }
                }
            }

            // Campos obligatorios
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Medidas actuales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cintura,
                        onValueChange = { cintura = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cintura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cuello,
                        onValueChange = { cuello = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cuello (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Campo adicional para mujeres
            if (persona.sexo.lowercase() == "femenino") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Medidas específicas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cadera,
                            onValueChange = { cadera = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Cadera (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de guardar
            Button(
                onClick = {
                    if (!isFormValid || isSaving) return@Button
                    isSaving = true

                    val pesoF = peso.toFloat()
                    val cinturaF = cintura.toFloat()
                    val cuelloF = cuello.toFloat()
                    val caderaF = if (cadera.isNotBlank()) cadera.toFloat() else null

                    scope.launch {
                        try {
                            val callback = {
                                navController.popBackStack()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Registro ${if (registroExistente != null) "actualizado" else "guardado"} ✔️")
                                }
                                isSaving = false
                            }

                            if (registroExistente != null) {
                                antropometriaViewModel.actualizarRegistro(
                                    registroExistente.copy(
                                        peso = pesoF,
                                        cintura = cinturaF,
                                        cuello = cuelloF,
                                        cadera = caderaF
                                    ),
                                    onSuccess = callback
                                )
                            } else {
                                antropometriaViewModel.guardarRegistroNuevo(
                                    peso = pesoF,
                                    cintura = cinturaF,
                                    cuello = cuelloF,
                                    cadera = caderaF,
                                    estatura = persona.estatura,
                                    sexo = persona.sexo,
                                    edad = persona.edad,
                                    onSuccess = callback
                                )
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error al guardar 😢")
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
                Text(if (registroExistente != null) "Actualizar" else "Guardar")
            }
        }
    }
}