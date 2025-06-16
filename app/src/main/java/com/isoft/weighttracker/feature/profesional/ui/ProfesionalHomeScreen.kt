package com.isoft.weighttracker.feature.profesional.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.profesional.viewmodel.ProfesionalViewModel
import com.isoft.weighttracker.shared.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalHomeScreen(navController: NavController, role: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    val viewModel: ProfesionalViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val usuarios by viewModel.usuariosAsociados.collectAsState()
    val profesionalProfile by userViewModel.profesionalProfile.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    var cargando by remember { mutableStateOf(true) }
    var mostrarAviso by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val menuItems = listOf(
        "Perfil Profesional" to "datosProfesional",
        "Solicitudes de Planes" to "solicitudesPlanes/$role",
        "Planes Creados" to "planesCreados/$role",
        "Reportes de Avance" to "reporteAvance/$role"
    )

    val perfilCompleto = profesionalProfile?.let {
        it.especialidad.isNotBlank() && it.estudios.isNotBlank() && it.cedula.isNotBlank() && it.experiencia.isNotBlank()
    } ?: false

    LaunchedEffect(Unit) {
        userViewModel.loadUser()
        userViewModel.loadProfesionalProfile()
        viewModel.cargarUsuariosAsociados(role)
    }

    LaunchedEffect(profesionalProfile) {
        if (cargando) {
            kotlinx.coroutines.delay(1500)
            cargando = false
            if (!perfilCompleto) mostrarAviso = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // ✅ NUEVA SECCIÓN: Header del perfil con foto, nombre y email
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto de perfil
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            val userPhotoUrl = currentUser?.photoUrl
                            if (userPhotoUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(userPhotoUrl)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar por defecto",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Información del usuario
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val userName = currentUser?.name ?: "Usuario"
                            val userEmail = currentUser?.email ?: ""

                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Badge del rol
                            Surface(
                                modifier = Modifier.padding(top = 4.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                val roleEmoji = when(role) {
                                    "entrenador" -> "💪"
                                    "nutricionista" -> "🥗"
                                    else -> "👨‍⚕️"
                                }
                                val roleText = when(role) {
                                    "entrenador" -> "Entrenador"
                                    "nutricionista" -> "Nutricionista"
                                    else -> "Profesional"
                                }
                                Text(
                                    text = "$roleEmoji $roleText",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // Menú principal
                menuItems.forEach { (label, route) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = false,
                        onClick = {
                            navController.navigate(route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                // Espaciado y botón separado para cerrar sesión
                Spacer(modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = {
                        Text(
                            "Cerrar sesión",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel Profesional") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.cargarUsuariosAsociados(role) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                        }
                    }
                )
            }

        ) { innerPadding ->
            if (cargando) {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
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
                            "Cargando perfil...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (mostrarAviso) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    navController.navigate("datosProfesional")
                }

                Box(
                    modifier = Modifier
                        .padding(innerPadding)
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
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "¡Completa tu perfil profesional!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Para usar todas las funciones necesitas completar tu perfil profesional primero",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Te redirigiremos en un momento...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "👋 ¡Hola ${currentUser?.name ?: "Profesional"}!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    profesionalProfile?.especialidad.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    profesionalProfile?.idProfesional?.let { codigo ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🆔 Tu Código Profesional",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            codigo,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(onClick = {
                                            clipboard.setText(AnnotatedString(codigo))
                                            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                                        }
                                    }
                                    Text(
                                        "Comparte este código con usuarios para que te encuentren.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (role != "nutricionista") {
                        item {
                            Card(
                                onClick = { navController.navigate("solicitudesPlanes/$role") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("📝", style = MaterialTheme.typography.headlineLarge)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Solicitudes de Planes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Revisa y crea planes para tus usuarios",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            onClick = { navController.navigate("reporteAvance/$role") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📊", style = MaterialTheme.typography.headlineLarge)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Reportes de Avance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Ve el progreso de tus usuarios",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }

                    item {
                        Text(
                            "👥 Usuarios Asociados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (usuarios.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🤝",
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Aún no tienes usuarios asociados",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Los usuarios aparecerán cuando se asocien contigo",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        items(usuarios) { usuario ->
                            UsuarioCard(usuario)
                        }
                    }
                }
            }
        }
    }

    LogoutDialog(
        show = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        onConfirm = {
            showLogoutDialog = false
            userViewModel.clearUser()
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    )
}

@Composable
fun UsuarioCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodySmall)
            Text("Rol: ${user.role}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LogoutDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro de que querés salir de la aplicación?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}