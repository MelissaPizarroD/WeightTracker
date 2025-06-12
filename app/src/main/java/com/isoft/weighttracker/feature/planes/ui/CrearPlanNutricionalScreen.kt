package com.isoft.weighttracker.feature.planes.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.planes.model.*
import com.isoft.weighttracker.feature.planes.ui.components.CategoriaAlimentoCard
import com.isoft.weighttracker.feature.planes.ui.components.ConsumoOcasionalCard
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlanNutricionalScreen(
    navController: NavController,
    solicitud: SolicitudPlan,
    userViewModel: UserViewModel = viewModel(),
    planesViewModel: PlanesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by planesViewModel.isLoading.collectAsState()
    val mensaje by planesViewModel.mensaje.collectAsState()

    // Estados para el plan nutricional
    var frecuencia by remember { mutableStateOf("Lunes a Sábado") }
    var repeticion by remember { mutableStateOf("diaria") }
    var observacionesGenerales by remember { mutableStateOf("") }

    // Estados para las categorías de alimentos (SIN ACEITE DE OLIVA)
    var patatasArrozPanPasta by remember { mutableStateOf(CategoriaAlimento()) }
    var verdurasHortalizas by remember { mutableStateOf(CategoriaAlimento()) }
    var frutas by remember { mutableStateOf(CategoriaAlimento()) }
    var lecheDerivados by remember { mutableStateOf(CategoriaAlimento()) }
    var pescados by remember { mutableStateOf(CategoriaAlimento()) }
    var carnesMagrasAvesHuevos by remember { mutableStateOf(CategoriaAlimento()) }
    var legumbres by remember { mutableStateOf(CategoriaAlimento()) }
    var frutoSecos by remember { mutableStateOf(CategoriaAlimento()) }
    var consumoOcasional by remember { mutableStateOf(ConsumoOcasional()) }

    // ✅ NUEVOS: Estados para datos del usuario
    var personaProfile by remember { mutableStateOf<PersonaProfile?>(null) }
    var antropometriaReciente by remember { mutableStateOf<Antropometria?>(null) }

    // Estados para expandir/contraer cards
    var expandedCards by remember { mutableStateOf(setOf<String>()) }

    // ✅ NUEVO: Estado para diálogo de confirmación
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // ✅ AGREGADO: Cargar usuario profesional
    LaunchedEffect(Unit) {
        userViewModel.loadUser() // ← Para que aparezca el profesional
    }

    // ✅ ACTUALIZADO: Cargar datos del usuario que solicitó el plan
    LaunchedEffect(solicitud.usuarioId) {
        scope.launch {
            try {
                val userRepo = UserRepository()
                // Cargar datos del usuario que solicitó el plan
                personaProfile = userRepo.getPersonaProfileByUserId(solicitud.usuarioId)
                antropometriaReciente =
                    userRepo.getAntropometriaRecienteByUserId(solicitud.usuarioId)
            } catch (e: Exception) {
                Log.e("CrearPlanNutricional", "Error cargando datos del usuario", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Plan Nutricional") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ ACTUALIZADA: Información del usuario CON antecedentes médicos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Información del Usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Datos básicos
                    Text(
                        "👤 Nombre: ${solicitud.nombreUsuario}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "📧 Email: ${solicitud.emailUsuario}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // ✅ DATOS MÉDICOS Y ANTROPOMÉTRICOS
                    personaProfile?.let { profile ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "📋 Datos Médicos y Físicos:",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            "🎂 Edad: ${profile.edad} años",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "⚧ Sexo: ${profile.sexo.replaceFirstChar { it.uppercase() }}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "📏 Estatura: ${profile.estatura} cm",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Antropometría reciente
                        antropometriaReciente?.let { antro ->
                            Text(
                                "⚖️ Peso actual: ${antro.peso} kg (IMC: ${"%.1f".format(antro.imc)})",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ✅ ANTECEDENTES MÉDICOS - IMPORTANTE PARA NUTRICIÓN
                        Text(
                            "🏥 Antecedentes Médicos:",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = if (profile.antecedentesMedicos.isNotBlank()) {
                                    profile.antecedentesMedicos
                                } else {
                                    "Sin antecedentes médicos registrados"
                                },
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (profile.antecedentesMedicos.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }

                        // ✅ ADVERTENCIA ESPECIAL PARA NUTRICIONISTAS
                        if (profile.antecedentesMedicos.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            ) {
                                Text(
                                    text = "⚠️ IMPORTANTE: Considera los antecedentes médicos al diseñar el plan nutricional. Consulta con médico si es necesario.",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "💬 Solicitud:",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        solicitud.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Configuración del plan
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚙️ Configuración del Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ CORREGIDO: Menú desplegable para frecuencia
                    var expandedFrecuencia by remember { mutableStateOf(false) }
                    val opcionesFrecuencia =
                        listOf("Lunes a Viernes", "Lunes a Sábado", "Lunes a Domingo")

                    Text(
                        "Frecuencia:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedFrecuencia,
                        onExpandedChange = { expandedFrecuencia = !expandedFrecuencia }
                    ) {
                        OutlinedTextField(
                            value = frecuencia,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Seleccionar frecuencia") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedFrecuencia,
                            onDismissRequest = { expandedFrecuencia = false }
                        ) {
                            opcionesFrecuencia.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        frecuencia = opcion
                                        expandedFrecuencia = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ CORREGIDO: Menú desplegable para repetición
                    var expandedRepeticion by remember { mutableStateOf(false) }
                    val opcionesRepeticion = listOf("diaria", "cada 2 días", "cada 3 días")

                    Text(
                        "Repetición:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedRepeticion,
                        onExpandedChange = { expandedRepeticion = !expandedRepeticion }
                    ) {
                        OutlinedTextField(
                            value = repeticion,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Seleccionar repetición") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRepeticion)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedRepeticion,
                            onDismissRequest = { expandedRepeticion = false }
                        ) {
                            opcionesRepeticion.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        repeticion = opcion
                                        expandedRepeticion = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Grupos de Alimentos
            Text(
                "🍽️ Grupos de Alimentos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // ✅ CORREGIDO: Categorías sin duplicados y sin aceite de oliva

            // 🥔 Patatas, arroz, pan, pasta integral
            CategoriaAlimentoCard(
                titulo = "🥔 Patatas, arroz, pan, pasta integral",
                categoria = patatasArrozPanPasta,
                onCategoriaChanged = { patatasArrozPanPasta = it },
                isExpanded = expandedCards.contains("patatas"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "patatas"
                    } else {
                        expandedCards - "patatas"
                    }
                },
                tipoFrecuencia = "DIARIA",
                tiposEspecificos = listOf("Pasta", "Arroz", "Pan", "Patatas"),
                frecuenciaSugerida = "4-6 raciones",
                showValidation = true
            )

            // 🥗 Verduras y hortalizas
            CategoriaAlimentoCard(
                titulo = "🥗 Verduras y hortalizas",
                categoria = verdurasHortalizas,
                onCategoriaChanged = { verdurasHortalizas = it },
                isExpanded = expandedCards.contains("verduras"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "verduras"
                    } else {
                        expandedCards - "verduras"
                    }
                },
                tipoFrecuencia = "DIARIA",
                frecuenciaSugerida = "≥ 2 raciones",
                showValidation = true
            )

            // 🍎 Frutas
            CategoriaAlimentoCard(
                titulo = "🍎 Frutas",
                categoria = frutas,
                onCategoriaChanged = { frutas = it },
                isExpanded = expandedCards.contains("frutas"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "frutas"
                    } else {
                        expandedCards - "frutas"
                    }
                },
                tipoFrecuencia = "DIARIA",
                frecuenciaSugerida = "≥ 3 raciones",
                showValidation = true
            )

            // 🥛 Leche y derivados
            CategoriaAlimentoCard(
                titulo = "🥛 Leche y derivados",
                categoria = lecheDerivados,
                onCategoriaChanged = { lecheDerivados = it },
                isExpanded = expandedCards.contains("leche"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "leche"
                    } else {
                        expandedCards - "leche"
                    }
                },
                tipoFrecuencia = "DIARIA",
                tiposEspecificos = listOf("Leche", "Yogur", "Queso curado", "Queso fresco"),
                frecuenciaSugerida = "2-4 raciones",
                unidadMedida = "ml o g",
                showValidation = true
            )

            // 🐟 Pescados
            CategoriaAlimentoCard(
                titulo = "🐟 Pescados",
                categoria = pescados,
                onCategoriaChanged = { pescados = it },
                isExpanded = expandedCards.contains("pescados"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "pescados"
                    } else {
                        expandedCards - "pescados"
                    }
                },
                tipoFrecuencia = "SEMANAL",
                frecuenciaSugerida = "3-4 raciones por semana",
                showValidation = true
            )

            // 🍗 Carnes magras, aves y huevos
            CategoriaAlimentoCard(
                titulo = "🍗 Carnes magras, aves y huevos",
                categoria = carnesMagrasAvesHuevos,
                onCategoriaChanged = { carnesMagrasAvesHuevos = it },
                isExpanded = expandedCards.contains("carnes"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "carnes"
                    } else {
                        expandedCards - "carnes"
                    }
                },
                tipoFrecuencia = "SEMANAL",
                frecuenciaSugerida = "3-4 raciones por semana",
                conAlternar = true,
                showValidation = true
            )

            // 🫘 Legumbres
            CategoriaAlimentoCard(
                titulo = "🫘 Legumbres",
                categoria = legumbres,
                onCategoriaChanged = { legumbres = it },
                isExpanded = expandedCards.contains("legumbres"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "legumbres"
                    } else {
                        expandedCards - "legumbres"
                    }
                },
                tipoFrecuencia = "SEMANAL",
                frecuenciaSugerida = "2-4 raciones por semana",
                showValidation = true
            )

            // 🥜 Frutos secos
            CategoriaAlimentoCard(
                titulo = "🥜 Frutos secos",
                categoria = frutoSecos,
                onCategoriaChanged = { frutoSecos = it },
                isExpanded = expandedCards.contains("frutos"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "frutos"
                    } else {
                        expandedCards - "frutos"
                    }
                },
                tipoFrecuencia = "SEMANAL",
                frecuenciaSugerida = "3-7 raciones por semana",
                pesoDefecto = 25.0,
                showValidation = true
            )

            // ❌ Consumo ocasional y moderado
            ConsumoOcasionalCard(
                consumoOcasional = consumoOcasional,
                onConsumoChanged = { consumoOcasional = it },
                isExpanded = expandedCards.contains("ocasional"),
                onExpandChanged = { expanded ->
                    expandedCards = if (expanded) {
                        expandedCards + "ocasional"
                    } else {
                        expandedCards - "ocasional"
                    }
                }
            )

            // Observaciones generales
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📝 Observaciones Generales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = observacionesGenerales,
                        onValueChange = { observacionesGenerales = it },
                        label = { Text("Observaciones adicionales del plan") },
                        placeholder = { Text("Recomendaciones especiales, notas importantes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }

            // Botón Guardar con confirmación
            Button(
                onClick = {
                    // ✅ VALIDACIONES PREVIAS ANTES DE MOSTRAR DIÁLOGO
                    val profesionalId = currentUser?.uid ?: ""
                    currentUser?.name ?: ""

                    if (profesionalId.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Error: No se puede identificar al profesional",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    // Validar que hay al menos una categoría activa (SIN ACEITE)
                    val categoriasActivas = listOf(
                        "Cereales" to patatasArrozPanPasta,
                        "Verduras y hortalizas" to verdurasHortalizas,
                        "Frutas" to frutas,
                        "Leche y derivados" to lecheDerivados,
                        "Pescados" to pescados,
                        "Carnes, aves y huevos" to carnesMagrasAvesHuevos,
                        "Legumbres" to legumbres,
                        "Frutos secos" to frutoSecos
                    ).filter { it.second.activo }

                    if (categoriasActivas.isEmpty()) {
                        Toast.makeText(
                            context,
                            "⚠️ Debes activar al menos una categoría de alimentos",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    // Validar que las categorías activas tengan datos completos
                    val erroresValidacion = mutableListOf<String>()

                    categoriasActivas.forEach { (nombreCategoria, categoria) ->
                        var tieneErrores = false

                        // Validar raciones según tipo de frecuencia (SIN ACEITE)
                        when {
                            // Categorías diarias
                            nombreCategoria in listOf(
                                "Cereales",
                                "Verduras y hortalizas",
                                "Frutas",
                                "Leche y derivados"
                            ) -> {
                                if (categoria.racionesPorDia <= 0) {
                                    tieneErrores = true
                                }
                            }
                            // Categorías semanales
                            nombreCategoria in listOf(
                                "Pescados",
                                "Carnes, aves y huevos",
                                "Legumbres",
                                "Frutos secos"
                            ) -> {
                                if (categoria.racionesPorSemana <= 0) {
                                    tieneErrores = true
                                }
                            }
                        }

                        // Validar peso por ración
                        if (categoria.pesoPorRacion <= 0) {
                            tieneErrores = true
                        }

                        if (tieneErrores) {
                            erroresValidacion.add(nombreCategoria)
                        }
                    }

                    // Mostrar errores si los hay
                    if (erroresValidacion.isNotEmpty()) {
                        val mensajeError = when {
                            erroresValidacion.size == 1 ->
                                "⚠️ Complete los datos de: ${erroresValidacion.first()}\n(raciones y peso por ración)"

                            erroresValidacion.size <= 3 ->
                                "⚠️ Complete los datos de:\n• ${erroresValidacion.joinToString("\n• ")}\n(raciones y peso por ración)"

                            else ->
                                "⚠️ Complete los datos de ${erroresValidacion.size} categorías\n(raciones y peso por ración)"
                        }

                        Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // ✅ Si todas las validaciones pasan, mostrar diálogo de confirmación
                    mostrarDialogoConfirmacion = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Plan Nutricional")
            }
        }
    }

// ✅ NUEVO: Diálogo de confirmación
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = {
                Text(
                    "🥗 Confirmar Plan Nutricional",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("¿Estás seguro de enviar este plan nutricional a:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "👤 ${solicitud.nombreUsuario}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Mostrar categorías activas en el diálogo
                    val categoriasActivas = mutableListOf<String>()
                    if (patatasArrozPanPasta.activo) categoriasActivas.add("Cereales")
                    if (verdurasHortalizas.activo) categoriasActivas.add("Verduras")
                    if (frutas.activo) categoriasActivas.add("Frutas")
                    if (lecheDerivados.activo) categoriasActivas.add("Lácteos")
                    if (pescados.activo) categoriasActivas.add("Pescados")
                    if (carnesMagrasAvesHuevos.activo) categoriasActivas.add("Carnes/Huevos")
                    if (legumbres.activo) categoriasActivas.add("Legumbres")
                    if (frutoSecos.activo) categoriasActivas.add("Frutos secos")

                    if (categoriasActivas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "📋 Incluye: ${categoriasActivas.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false

                        // ✅ CREAR Y ENVIAR EL PLAN (SIN ACEITE)
                        scope.launch {
                            val profesionalId = currentUser?.uid ?: ""
                            val nombreProfesional = currentUser?.name ?: ""

                            val plan = PlanNutricional(
                                usuarioId = solicitud.usuarioId,
                                profesionalId = profesionalId,
                                nombreProfesional = nombreProfesional,
                                frecuencia = frecuencia,
                                repeticion = repeticion,
                                patatasArrozPanPasta = patatasArrozPanPasta,
                                verdurasHortalizas = verdurasHortalizas,
                                frutas = frutas,
                                // ❌ QUITADO: aceiteOliva = aceiteOliva,
                                lecheDerivados = lecheDerivados,
                                pescados = pescados,
                                carnesMagrasAvesHuevos = carnesMagrasAvesHuevos,
                                legumbres = legumbres,
                                frutoSecos = frutoSecos,
                                consumoOcasional = consumoOcasional,
                                observacionesGenerales = observacionesGenerales
                            )

                            planesViewModel.crearPlanNutricional(plan) { exito ->
                                if (exito) {
                                    // Marcar solicitud como completada
                                    planesViewModel.completarSolicitud(solicitud.id)
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                ) {
                    Text("✅ Sí, enviar plan")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacion = false }
                ) {
                    Text("❌ Cancelar")
                }
            }
        )
    }
}