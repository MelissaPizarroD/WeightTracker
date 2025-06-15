package com.isoft.weighttracker.feature.asociar.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.isoft.weighttracker.feature.asociar.viewmodel.AsociarProfesionalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsociarProfesionalScreen(
    navController: NavController,
    viewModel: AsociarProfesionalViewModel = viewModel()
) {
    // ✅ USAR INFORMACIÓN COMPLETA
    val asociadosCompletos by viewModel.asociadosCompletos.collectAsState()
    val asociados = asociadosCompletos.mapValues { it.value.user } // Para compatibilidad

    //Para pantalla de carga
    val isLoading by viewModel.isLoading.collectAsState()

    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var tipoProfesional by remember { mutableStateOf("entrenador") }
    var codigo by remember { mutableStateOf("") }
    var tipoAEliminar by remember { mutableStateOf<String?>(null) }

    val tieneEntrenador = "entrenador" in asociados
    val tieneNutricionista = "nutricionista" in asociados
    val tieneAmbos = tieneEntrenador && tieneNutricionista

    // Ajustar automáticamente el tipo cuando cambien los asociados
    LaunchedEffect(asociados) {
        tipoProfesional = when {
            tieneEntrenador && !tieneNutricionista -> "nutricionista"
            tieneNutricionista && !tieneEntrenador -> "entrenador"
            else -> "entrenador"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarProfesionalesAsociados()
    }

    LaunchedEffect(estado) {
        estado?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearEstado()
        }
    }

    if (isLoading && asociadosCompletos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando profesionales...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profesionales Asociados") },
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (asociadosCompletos.isNotEmpty()) {
                Text("Tus profesionales asociados:", style = MaterialTheme.typography.titleMedium)

                // ✅ MOSTRAR INFORMACIÓN COMPLETA
                asociadosCompletos.forEach { (tipo, profesionalCompleto) ->
                    val profe = profesionalCompleto.user
                    val profile = profesionalCompleto.profesionalProfile

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header con tipo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${tipo.replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // ✅ CÓDIGO CON BOTÓN COPIAR
                                profile?.idProfesional?.let { codigo ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            codigo,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(codigo))
                                                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copiar código",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Foto de perfil
                            if (!profe.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profe.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            // Información básica
                            Text("👤 ${profe.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("📧 ${profe.email}", style = MaterialTheme.typography.bodyMedium)

                            // ✅ INFORMACIÓN PROFESIONAL
                            profile?.let { prof ->
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))

                                Text("Información Profesional", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))

                                if (prof.especialidad.isNotBlank()) {
                                    Text("🎯 Especialidad: ${prof.especialidad}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (prof.estudios.isNotBlank()) {
                                    Text("🎓 Estudios: ${prof.estudios}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (prof.cedula.isNotBlank()) {
                                    Text("📋 Cédula: ${prof.cedula}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (prof.experiencia.isNotBlank()) {
                                    Text("⭐ Experiencia: ${prof.experiencia}", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { tipoAEliminar = tipo },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Eliminar ${tipo.replaceFirstChar { it.uppercase() }}")
                            }
                        }
                    }
                }
            }

            Divider()

            if (!tieneAmbos) {
                Text("Asociar nuevo profesional", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("entrenador", "nutricionista").forEach { tipo ->
                        val deshabilitado = (tipo == "entrenador" && tieneEntrenador) ||
                                (tipo == "nutricionista" && tieneNutricionista)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .alpha(if (deshabilitado) 0.4f else 1f)
                        ) {
                            RadioButton(
                                selected = tipoProfesional == tipo,
                                onClick = { if (!deshabilitado) tipoProfesional = tipo },
                                enabled = !deshabilitado
                            )
                            Text(tipo.replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código del profesional") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.asociarProfesional(codigo.trim(), tipoProfesional) {
                            codigo = ""
                        }
                    },
                    enabled = codigo.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Asociar ${tipoProfesional.replaceFirstChar { it.uppercase() }}")
                }
            } else {
                Text(
                    "Ya tienes un entrenador y un nutricionista asociados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Confirmación de eliminación
    tipoAEliminar?.let { tipo ->
        AlertDialog(
            onDismissRequest = { tipoAEliminar = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Eliminar profesional?") },
            text = {
                Text("¿Estás seguro de eliminar al ${tipo.replaceFirstChar { it.uppercase() }} asociado? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tipoAEliminar = null
                        viewModel.eliminarAsociacion(tipo)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tipoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}