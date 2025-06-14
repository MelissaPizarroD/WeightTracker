package com.isoft.weighttracker.feature.profesional.datosProf.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val profesionalProfile by userViewModel.profesionalProfile.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

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
                title = { Text("Datos Profesionales") },
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ MOSTRAR CÓDIGO PROFESIONAL
            profesionalProfile?.idProfesional?.let { codigo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "🆔 Tu Código Profesional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                codigo,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(codigo))
                                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar código",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            "Comparte este código con usuarios para que se asocien contigo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Campos del formulario
            OutlinedTextField(
                value = especialidad,
                onValueChange = { especialidad = it },
                label = { Text("Especialidad") },
                placeholder = { Text("Ej: Nutrición Deportiva, Entrenamiento Funcional") },
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = showErrors && especialidad.isBlank(),
                supportingText = {
                    if (showErrors && especialidad.isBlank())
                        Text("Campo requerido")
                }
            )

            OutlinedTextField(
                value = estudios,
                onValueChange = { estudios = it },
                label = { Text("Estudios / Certificaciones") },
                placeholder = { Text("Ej: Lic. en Nutrición, Cert. ACSM") },
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = showErrors && estudios.isBlank(),
                supportingText = {
                    if (showErrors && estudios.isBlank())
                        Text("Campo requerido")
                }
            )

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it.filter { c -> c.isDigit() } },
                label = { Text("Cédula Profesional") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = showErrors && cedula.isBlank(),
                supportingText = {
                    if (showErrors && cedula.isBlank())
                        Text("Campo requerido")
                }
            )

            OutlinedTextField(
                value = experiencia,
                onValueChange = { experiencia = it },
                label = { Text("Experiencia") },
                placeholder = { Text("Ej: 5 años en nutrición clínica y deportiva") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = modoEdicion,
                isError = showErrors && experiencia.isBlank(),
                supportingText = {
                    if (showErrors && experiencia.isBlank())
                        Text("Campo requerido")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            if (modoEdicion) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                scope.launch {
                                    try {
                                        isSaving = true

                                        val profile = ProfesionalProfile(
                                            especialidad = especialidad.trim(),
                                            estudios = estudios.trim(),
                                            cedula = cedula.trim(),
                                            experiencia = experiencia.trim(),
                                            idProfesional = profesionalProfile?.idProfesional // Mantener ID existente
                                        )

                                        userViewModel.updateProfesionalProfile(profile) {
                                            Toast.makeText(context, "✅ Perfil guardado", Toast.LENGTH_SHORT).show()
                                            navController.navigateUp()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            } else {
                                showErrors = true
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            } else {
                // Botón para editar
                Button(
                    onClick = { modoEdicion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar perfil")
                }
            }
        }
    }
}