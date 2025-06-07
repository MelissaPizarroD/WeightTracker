package com.isoft.weighttracker.feature.comidas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.isoft.weighttracker.feature.comidas.model.Comida
import com.isoft.weighttracker.feature.comidas.viewmodel.ComidaViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialComidasScreen(
    navController: NavController,
    viewModel: ComidaViewModel = viewModel()
) {
    val comidas by viewModel.comidas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comidasExpandibles = remember { mutableStateMapOf<Long, Boolean>() }
    var comidaAEliminar by remember { mutableStateOf<Comida?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarComidas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Comidas") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("registrarComida") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar comida")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (comidas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay comidas registradas.", style = MaterialTheme.typography.bodyMedium)
                            Text("Toca ➕ para agregar 🍽️", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(comidas) { comida ->
                        val expandido = comidasExpandibles[comida.fecha] ?: false
                        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                        val fechaStr = sdf.format(Date(comida.fecha))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { comidasExpandibles[comida.fecha] = !expandido },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "🍽️ ${comida.comidaDelDia} - ${comida.comida}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🕓 $fechaStr", style = MaterialTheme.typography.bodySmall)

                                if (expandido) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("🔥 Calorías: ${comida.calorias}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = {
                                            val json = URLEncoder.encode(Gson().toJson(comida), "UTF-8")
                                            navController.navigate("registrarComida?comida=$json")
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        }

                                        IconButton(onClick = { comidaAEliminar = comida }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación
        comidaAEliminar?.let { comida ->
            AlertDialog(
                onDismissRequest = { comidaAEliminar = null },
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                title = { Text("¿Eliminar comida?") },
                text = { Text("Esta acción eliminará el registro. ¿Deseas continuar?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            comidaAEliminar = null
                            scope.launch {
                                val eliminada = viewModel.eliminarComida(comida.id ?: "")
                                if (eliminada) {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Comida eliminada",
                                        actionLabel = "Deshacer",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.registrarNuevaComida(comida) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Comida restaurada ✔️")
                                            }
                                        }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Error al eliminar 😢")
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { comidaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}