package com.isoft.weighttracker.feature.planes.ui.profesional

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.planes.viewmodel.PlanesViewModel
import com.isoft.weighttracker.feature.profesional.viewmodel.ProfesionalViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesCreadosUsuariosScreen(
    navController: NavController,
    role: String,
    planesViewModel: PlanesViewModel = viewModel(),
    profesionalViewModel: ProfesionalViewModel = viewModel()
) {
    val usuarios by profesionalViewModel.usuariosAsociados.collectAsState()

    LaunchedEffect(role) {
        profesionalViewModel.cargarUsuariosAsociados(role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes Creados") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (role == "nutricionista") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "⚠️ Función no disponible",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Los planes creados no están habilitados para nutricionistas en este momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "\uD83D\uDC65 Usuarios con planes creados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (usuarios.isEmpty()) {
                    Text("No hay usuarios asociados todavía.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(usuarios) { usuario ->
                            UsuarioPlanesCard(usuario = usuario) {
                                val encodedName = URLEncoder.encode(usuario.name, StandardCharsets.UTF_8.toString())
                                navController.navigate("planesUsuario/${usuario.uid}/$encodedName")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioPlanesCard(
    usuario: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\uD83D\uDC64 ${usuario.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}