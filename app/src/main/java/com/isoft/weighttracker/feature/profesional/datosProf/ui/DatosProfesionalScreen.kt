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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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

    // ✅ VALIDACIONES EN TIEMPO REAL
    val especialidadEsValida = especialidad.trim().length >= 3
    val especialidadMensaje = when {
        especialidad.isBlank() -> null
        especialidad.trim().length < 3 -> "Mínimo 3 caracteres"
        else -> null
    }

    val estudiosEsValido = estudios.trim().length >= 10
    val estudiosMensaje = when {
        estudios.isBlank() -> null
        estudios.trim().length < 10 -> "Describe con más detalle (mín. 10 caracteres)"
        else -> null
    }

    val cedulaEsValida = cedula.length >= 6 && cedula.all { it.isDigit() }
    val cedulaMensaje = when {
        cedula.isBlank() -> null
        cedula.length < 6 -> "Mínimo 6 dígitos"
        !cedula.all { it.isDigit() } -> "Solo números permitidos"
        else -> null
    }

    val experienciaEsValida = experiencia.trim().length >= 20
    val experienciaMensaje = when {
        experiencia.isBlank() -> null
        experiencia.trim().length < 20 -> "Describe con más detalle tu experiencia (mín. 20 caracteres)"
        else -> null
    }

    val isFormValid = especialidadEsValida && estudiosEsValido && cedulaEsValida && experienciaEsValida

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

            // ✅ CAMPO ESPECIALIDAD CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = especialidad,
                onValueChange = { newValue ->
                    // Limitar a 100 caracteres y filtrar caracteres especiales problemáticos
                    especialidad = newValue.take(100).filter { it.isLetter() || it.isWhitespace() || it in ",.-" }
                },
                label = { Text("Especialidad") },
                placeholder = { Text("Ej: Nutrición Deportiva, Entrenamiento Funcional") },
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = especialidadMensaje != null,
                supportingText = {
                    especialidadMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (especialidad.isNotBlank() && especialidadEsValida) {
                            Text(
                                text = "✓ Especialidad válida",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${especialidad.length}/100 caracteres",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (especialidad.isNotBlank()) {
                        if (especialidadEsValida) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (especialidadMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ CAMPO ESTUDIOS CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = estudios,
                onValueChange = { newValue ->
                    estudios = newValue.take(200)
                },
                label = { Text("Estudios / Certificaciones") },
                placeholder = { Text("Ej: Licenciatura en Nutrición - Universidad XYZ, Certificación ACSM") },
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                minLines = 2,
                maxLines = 4,
                isError = estudiosMensaje != null,
                supportingText = {
                    estudiosMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (estudios.isNotBlank() && estudiosEsValido) {
                            Text(
                                text = "✓ Información completa",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${estudios.length}/200 caracteres",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (estudios.isNotBlank()) {
                        if (estudiosEsValido) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (estudiosMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ CAMPO CÉDULA CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = cedula,
                onValueChange = { newValue ->
                    // Solo números, máximo 15 dígitos
                    cedula = newValue.filter { it.isDigit() }.take(15)
                },
                label = { Text("Cédula Profesional") },
                placeholder = { Text("Ej: 12345678") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = cedulaMensaje != null,
                supportingText = {
                    cedulaMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (cedula.isNotBlank() && cedulaEsValida) {
                            Text(
                                text = "✓ Cédula válida",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${cedula.length}/15 dígitos",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (cedula.isNotBlank()) {
                        if (cedulaEsValida) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (cedulaMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ CAMPO EXPERIENCIA CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = experiencia,
                onValueChange = { newValue ->
                    experiencia = newValue.take(500)
                },
                label = { Text("Experiencia Profesional") },
                placeholder = { Text("Ej: 5 años trabajando en nutrición clínica y deportiva. He atendido más de 200 pacientes...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                enabled = modoEdicion,
                isError = experienciaMensaje != null,
                supportingText = {
                    experienciaMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (experiencia.isNotBlank() && experienciaEsValida) {
                            Text(
                                text = "✓ Experiencia bien detallada",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${experiencia.length}/500 caracteres",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (experiencia.isNotBlank()) {
                        if (experienciaEsValida) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (experienciaMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ INDICADOR DE PROGRESO DEL FORMULARIO
            if (modoEdicion) {
                val camposCompletos = listOf(especialidadEsValida, estudiosEsValido, cedulaEsValida, experienciaEsValida).count { it }
                val totalCampos = 4

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Completitud del perfil",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "$camposCompletos/$totalCampos",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = camposCompletos.toFloat() / totalCampos,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (camposCompletos < totalCampos) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Completa todos los campos para crear tu perfil profesional",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ TIPS PARA PROFESIONALES
            if (modoEdicion && !isFormValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "💡 Tips para un perfil exitoso:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Sé específico en tu especialidad\n" +
                                    "• Menciona certificaciones relevantes\n" +
                                    "• Describe casos de éxito en tu experiencia\n" +
                                    "• Usa términos técnicos apropiados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

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
                                            Toast.makeText(context, "✅ Perfil profesional guardado exitosamente", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, "Por favor completa correctamente todos los campos", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = isFormValid && !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isSaving) "Guardando..." else "Guardar Perfil")
                    }
                }
            } else {
                // Botón para editar
                Button(
                    onClick = { modoEdicion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar perfil profesional")
                }
            }
        }
    }
}