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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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

    // ✅ VALIDACIONES EN TIEMPO REAL
    val edadInt = edad.toIntOrNull()
    val estaturaFloat = estatura.toFloatOrNull()

    // Validaciones individuales con mensajes específicos
    val edadEsValida = edadInt != null && edadInt in 18..120
    val edadMensaje = when {
        edad.isBlank() -> null
        edadInt == null -> "Ingresa solo números"
        edadInt < 18 -> "La edad mínima es 18 años"
        edadInt > 120 -> "La edad máxima es 120 años"
        else -> null
    }

    val sexoEsValido = sexo.lowercase() in listOf("masculino", "femenino")
    val sexoMensaje = if (sexo.isNotBlank() && !sexoEsValido) "Selecciona una opción válida" else null

    val estaturaEsValida = estaturaFloat != null && estaturaFloat in 50f..250f
    val estaturaMensaje = when {
        estatura.isBlank() -> null
        estaturaFloat == null -> "Ingresa solo números (ej: 170.5)"
        estaturaFloat < 50f -> "La estatura mínima es 50 cm"
        estaturaFloat > 250f -> "La estatura máxima es 250 cm"
        else -> null
    }

    val antecedentesEsValido = antecedentes.isNotBlank()
    val antecedentesMensaje = if (antecedentes.isBlank() && showErrors) "Este campo es obligatorio" else null

    val isFormValid = edadEsValida && sexoEsValido && estaturaEsValida && antecedentesEsValido

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
            // ✅ CAMPO EDAD CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = edad,
                onValueChange = { newValue ->
                    // Solo permitir números y máximo 3 dígitos
                    val filtered = newValue.filter { it.isDigit() }.take(3)
                    edad = filtered
                },
                label = { Text("Edad") },
                placeholder = { Text("Ej: 25") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = edadMensaje != null,
                supportingText = {
                    edadMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (edad.isNotBlank() && edadEsValida) {
                            Text(
                                text = "✓ Edad válida",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (edad.isNotBlank()) {
                        if (edadEsValida) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (edadMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ CAMPO SEXO CON VALIDACIÓN
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

                sexoMensaje?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            } else {
                Text(
                    text = sexo.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ CAMPO ESTATURA CON VALIDACIÓN MEJORADA
            OutlinedTextField(
                value = estatura,
                onValueChange = { newValue ->
                    // Permitir números y un solo punto decimal
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                        .let { value ->
                            // Evitar múltiples puntos
                            val dotCount = value.count { it == '.' }
                            if (dotCount <= 1) value else value.dropLast(1)
                        }
                        .take(6) // Máximo 6 caracteres (ej: 250.50)
                    estatura = filtered
                },
                label = { Text("Estatura (cm)") },
                placeholder = { Text("Ej: 170.5") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = modoEdicion,
                isError = estaturaMensaje != null,
                supportingText = {
                    estaturaMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        if (estatura.isNotBlank() && estaturaEsValida) {
                            Text(
                                text = "✓ Estatura válida",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (estatura.isNotBlank()) {
                        if (estaturaEsValida) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Válido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (estaturaMensaje != null) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // ✅ CAMPO ANTECEDENTES CON VALIDACIÓN
            OutlinedTextField(
                value = antecedentes,
                onValueChange = { antecedentes = it },
                label = { Text("Antecedentes médicos") },
                placeholder = { Text("Describe cualquier condición médica relevante o escribe 'Ninguno'") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                enabled = modoEdicion,
                maxLines = 5,
                isError = antecedentesMensaje != null,
                supportingText = {
                    antecedentesMensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        Text(
                            text = "${antecedentes.length}/500 caracteres",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // ✅ INDICADOR DE PROGRESO DEL FORMULARIO
            if (modoEdicion) {
                val camposCompletos = listOf(edadEsValida, sexoEsValido, estaturaEsValida, antecedentesEsValido).count { it }
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
                                "Progreso del formulario",
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
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (modoEdicion) {
                // ✅ ROW CON BOTONES CANCELAR Y GUARDAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Restaurar valores originales si existe un perfil previo
                            personaProfile?.let {
                                edad = it.edad.toString()
                                sexo = it.sexo
                                estatura = it.estatura.toString()
                                antecedentes = it.antecedentesMedicos
                                modoEdicion = false
                                showErrors = false
                            } ?: run {
                                // Si no hay perfil previo, volver a la pantalla anterior
                                navController.navigateUp()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (!isFormValid) {
                                showErrors = true
                                Toast.makeText(context, "Por favor corrige los errores antes de continuar", Toast.LENGTH_LONG).show()
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
                        modifier = Modifier.weight(1f)
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