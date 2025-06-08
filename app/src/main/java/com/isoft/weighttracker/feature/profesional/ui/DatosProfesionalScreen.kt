package com.isoft.weighttracker.feature.profesional.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.core.model.ProfesionalProfile
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosProfesionalScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profesionalProfile by userViewModel.profesionalProfile.collectAsState()

    var especialidad by remember { mutableStateOf("") }
    var estudios by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var experiencia by remember { mutableStateOf("") }
    var modoEdicion by remember { mutableStateOf(true) }
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadProfesionalProfile()
    }

    LaunchedEffect(profesionalProfile) {
        profesionalProfile?.let {
            especialidad = it.especialidad
            estudios = it.estudios
            cedula = it.cedula
            experiencia = it.experiencia
            modoEdicion = false
        }
    }

    val isFormValid = especialidad.isNotBlank() &&
            estudios.isNotBlank() &&
            cedula.isNotBlank() &&
            experiencia.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos del profesional") },
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
                value = especialidad,
                onValueChange = { especialidad = it },
                label = { Text("Especialidad") },
                enabled = modoEdicion,
                isError = showErrors && especialidad.isBlank(),
                supportingText = {
                    if (showErrors && especialidad.isBlank())
                        Text("Este campo es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estudios,
                onValueChange = { estudios = it },
                label = { Text("Estudios") },
                enabled = modoEdicion,
                isError = showErrors && estudios.isBlank(),
                supportingText = {
                    if (showErrors && estudios.isBlank())
                        Text("Este campo es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it },
                label = { Text("Cédula profesional") },
                enabled = modoEdicion,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = showErrors && cedula.isBlank(),
                supportingText = {
                    if (showErrors && cedula.isBlank())
                        Text("Este campo es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = experiencia,
                onValueChange = { experiencia = it },
                label = { Text("Experiencia profesional") },
                enabled = modoEdicion,
                isError = showErrors && experiencia.isBlank(),
                supportingText = {
                    if (showErrors && experiencia.isBlank())
                        Text("Este campo es obligatorio")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 5
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
                            val perfil = ProfesionalProfile(
                                especialidad = especialidad.trim(),
                                estudios = estudios.trim(),
                                cedula = cedula.trim(),
                                experiencia = experiencia.trim()
                            )
                            userViewModel.updateProfesionalProfile(perfil) {
                                userViewModel.loadProfesionalProfile()
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