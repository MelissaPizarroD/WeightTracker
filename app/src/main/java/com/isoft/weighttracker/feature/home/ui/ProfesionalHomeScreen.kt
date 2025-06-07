package com.isoft.weighttracker.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalHomeScreen(navController: NavController, role: String) {
    val userViewModel: UserViewModel = viewModel()
    val currentUser by userViewModel.currentUser.collectAsState()

    var usuariosAsociados by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Cargar usuarios asociados desde Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.let {
            val users = obtenerUsuariosAsociados(it.uid, role)
            usuariosAsociados = users
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de ${role.replaceFirstChar { it.uppercase() }}") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "👋 Hola, ${currentUser?.name ?: "profesional"}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (usuariosAsociados.isEmpty()) {
                    Text("Aún no tienes usuarios asociados 🙁")
                } else {
                    Text(
                        "Usuarios asociados:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    usuariosAsociados.forEach { usuario ->
                        UsuarioCard(usuario, role) {
                            navController.navigate("usuarioDetalle/${usuario.uid}?tipo=$role")
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // 🔒 Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UsuarioCard(usuario: User, tipo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("👤 ${usuario.name}", style = MaterialTheme.typography.titleMedium)
            Text(usuario.email ?: "", style = MaterialTheme.typography.bodySmall)
            Text("ID: ${usuario.uid}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

suspend fun obtenerUsuariosAsociados(profesionalUid: String, tipo: String): List<User> {
    val db = FirebaseFirestore.getInstance()
    val fieldKey = when (tipo) {
        "entrenador" -> "profesionales.entrenador"
        "nutricionista" -> "profesionales.nutricionista"
        else -> return emptyList()
    }

    return try {
        val snapshot = db.collection("users")
            .whereEqualTo(fieldKey, profesionalUid)
            .get()
            .await()

        snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}