package com.isoft.weighttracker.feature.DatosPersonales.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosPersonalesScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val personaProfile by userViewModel.personaProfile.collectAsState()

    var edad by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var estatura by remember { mutableStateOf("") }
    var antecedentes by remember { mutableStateOf("") }
    var modoEdicion by remember { mutableStateOf(true) }
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadPersonaProfile()
    }

    LaunchedEffect(personaProfile) {
        personaProfile?.let {
            edad = it.edad.toString()
            sexo = it.sexo
            estatura = it.estatura.toString()
            antecedentes = it.antecedentesMedicos
            modoEdicion = false
        }
    }

    val edadInt = edad.toIntOrNull()
    val estaturaFloat = estatura.toFloatOrNull()
    val isFormValid = edadInt != null && edadInt in 18..120 &&
            sexo.lowercase() in listOf("masculino", "femenino") &&
            estaturaFloat != null && estaturaFloat in 50f..250f &&
            antecedentes.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos personales") },
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
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it.filter { c -> c.isDigit() } },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = showErrors && (edadInt == null || edadInt !in 18..120),
                supportingText = {
                    if (showErrors && (edadInt == null || edadInt !in 18..120))
                        Text("Edad válida entre 18 y 120")
                }
            )

            Text(
                "Sexo",
                style = MaterialTheme.typography.titleSmall,
                color = if (modoEdicion) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (modoEdicion) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("masculino", "femenino").forEach { opcion ->
                        FilterChip(
                            selected = sexo == opcion,
                            onClick = { sexo = opcion },
                            label = { Text(opcion.replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (opcion == "masculino") Icons.Default.Male else Icons.Default.Female,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (showErrors && sexo.lowercase() !in listOf("masculino", "femenino")) {
                    Text(
                        text = "Selecciona una opción",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = sexo.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = estatura,
                onValueChange = { estatura = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Estatura (cm)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = showErrors && (estaturaFloat == null || estaturaFloat !in 50f..250f),
                supportingText = {
                    if (showErrors && (estaturaFloat == null || estaturaFloat !in 50f..250f))
                        Text("Debe estar entre 50 y 250 cm")
                }
            )

            OutlinedTextField(
                value = antecedentes,
                onValueChange = { antecedentes = it },
                label = { Text("Antecedentes médicos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                enabled = modoEdicion,
                maxLines = 5,
                isError = showErrors && antecedentes.isBlank(),
                supportingText = {
                    if (showErrors && antecedentes.isBlank())
                        Text("No puede estar vacío")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (modoEdicion) {
                Button(
                    onClick = {
                        if (!isFormValid) {
                            showErrors = true
                            return@Button
                        }

                        isSaving = true
                        scope.launch {
                            val nuevoPerfil = PersonaProfile(
                                edad = edadInt!!,
                                sexo = sexo.trim().lowercase(),
                                estatura = estaturaFloat!!,
                                antecedentesMedicos = antecedentes.trim()
                            )
                            userViewModel.updatePersonaProfile(nuevoPerfil) {
                                userViewModel.loadPersonaProfile()
                                Toast.makeText(context, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                                isSaving = false
                                modoEdicion = false
                                navController.popBackStack()
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
                        text = if (isSaving) "Guardando..." else "Guardar cambios",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { modoEdicion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar datos")
                }
            }
        }
    }
}