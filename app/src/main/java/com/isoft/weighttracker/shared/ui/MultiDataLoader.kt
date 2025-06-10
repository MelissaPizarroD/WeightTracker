// VERÉ SI LO IMPLEMENTO LOL
package com.isoft.weighttracker.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.delay

/**
 * Estado de carga para múltiples datos
 */
data class LoadingState<T>(
    val isLoading: Boolean = true,
    val data: T? = null,
    val error: String? = null,
    val isRequired: Boolean = true
) {
    val isReady: Boolean get() = !isLoading && (data != null || !isRequired)
    val hasError: Boolean get() = error != null
    val needsData: Boolean get() = !isLoading && isRequired && data == null
}

/**
 * Configuración para validaciones específicas
 */
data class ValidationConfig(
    val loadingText: String = "Cargando datos...",
    val errorTitle: String = "Datos requeridos",
    val errorMessage: String = "Necesitas completar algunos datos para usar esta función.",
    val actionText: String = "Completar datos",
    val actionRoute: String = "datosPersonales"
)

/**
 * Composable para cargar múltiples datos con validaciones
 */
@Composable
fun <T1, T2, T3, T4, T5> MultiDataLoader(
    navController: NavController,
    data1: LoadingState<T1>,
    data2: LoadingState<T2>? = null,
    data3: LoadingState<T3>? = null,
    data4: LoadingState<T4>? = null,
    data5: LoadingState<T5>? = null,
    config: ValidationConfig = ValidationConfig(),
    loadingDelay: Long = 1500,
    content: @Composable (T1, T2?, T3?, T4?, T5?) -> Unit
) {
    var initialLoadingComplete by remember { mutableStateOf(false) }

    // Lista de todos los datos no nulos
    val allData = listOfNotNull(data1, data2, data3, data4, data5)

    // Estados de validación
    val isAnyLoading = allData.any { it.isLoading }
    val hasErrors = allData.any { it.hasError }
    val missingRequired = allData.any { it.needsData }
    val allReady = allData.all { it.isReady }

    // Manejo del loading inicial
    LaunchedEffect(isAnyLoading) {
        if (!isAnyLoading && !initialLoadingComplete) {
            delay(loadingDelay)
            initialLoadingComplete = true
        }
    }

    when {
        !initialLoadingComplete || isAnyLoading -> {
            LoadingScreen(config.loadingText)
        }
        hasErrors -> {
            ErrorScreen(
                navController = navController,
                config = config,
                errors = allData.mapNotNull { it.error }
            )
        }
        missingRequired -> {
            RequiredDataScreen(
                navController = navController,
                config = config
            )
        }
        allReady -> {
            content(
                data1.data!!,
                data2?.data,
                data3?.data,
                data4?.data,
                data5?.data
            )
        }
    }
}

/**
 * Hook para PersonaProfile con estado de carga
 */
@Composable
fun usePersonaProfileLoading(
    userViewModel: UserViewModel
): LoadingState<PersonaProfile> {
    val personaState = userViewModel.personaProfile.collectAsState()
    val persona = personaState.value
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val isComplete = persona != null &&
            persona.estatura > 0f &&
            persona.sexo.isNotBlank() &&
            persona.edad > 0

    LaunchedEffect(Unit) {
        try {
            userViewModel.loadPersonaProfile()
        } catch (e: Exception) {
            error = "Error cargando perfil personal: ${e.message}"
        }
    }

    LaunchedEffect(persona) {
        if (isLoading) {
            delay(1000)
            isLoading = false
        }
    }

    return LoadingState(
        isLoading = isLoading,
        data = if (isComplete) persona else null,
        error = error,
        isRequired = true
    )
}

/**
 * Hook genérico para cualquier dato con estado de carga
 */
@Composable
fun <T> useDataLoading(
    loader: suspend () -> T?,
    validator: (T?) -> Boolean = { it != null },
    isRequired: Boolean = true,
    errorMessage: String? = null
): LoadingState<T> {
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<T?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = loader()
            data = result
            if (!validator(result) && isRequired) {
                error = errorMessage ?: "Datos requeridos no encontrados"
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            delay(800)
            isLoading = false
        }
    }

    return LoadingState(
        isLoading = isLoading,
        data = if (validator(data)) data else null,
        error = error,
        isRequired = isRequired
    )
}

// Pantallas de estado
@Composable
private fun LoadingScreen(loadingText: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                loadingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RequiredDataScreen(
    navController: NavController,
    config: ValidationConfig
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    config.errorTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    config.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(config.actionRoute) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(config.actionText)
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    navController: NavController,
    config: ValidationConfig,
    errors: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Error cargando datos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                errors.forEach { error ->
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigateUp() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Volver")
                }
            }
        }
    }
}