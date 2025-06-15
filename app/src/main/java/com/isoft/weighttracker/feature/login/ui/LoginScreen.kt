package com.isoft.weighttracker.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.R
import com.isoft.weighttracker.core.auth.AuthenticationManager
import com.isoft.weighttracker.feature.login.viewmodel.LoginViewModel
import com.isoft.weighttracker.feature.login.viewmodel.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val authState = viewModel.authState.collectAsState()
    val navigationEvent = viewModel.navigationEvent.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser

    // Si el usuario ya está autenticado, revisamos si tiene rol y lo redirigimos
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.checkSession()
        }
    }

    // Navegación según el evento emitido por el viewmodel
    LaunchedEffect(navigationEvent.value) {
        when (val event = navigationEvent.value) {
            is NavigationEvent.SelectRole -> {
                navController.navigate("selectRole") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.clearNavigation()
            }

            is NavigationEvent.GoToHome -> {
                navController.navigate("home/${event.role}") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.clearNavigation()
            }

            null -> Unit
        }
    }

    // Solo mostrar la UI si no hay sesión
    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de la app
                    Image(
                        painter = painterResource(id = R.drawable.app_icon), // ⚠️ Necesitas agregar este archivo
                        contentDescription = "App Icon",
                        modifier = Modifier.size(120.dp)
                    )

                    // Título de la app - debajo de la imagen
                    Text(
                        text = "Weight Tracker",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(
                        text = "Tu compañero ideal para el seguimiento de peso y nutrición",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    // Botón de Google
                    ElevatedButton(
                        onClick = { viewModel.loginWithGoogle() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.goog),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continuar con Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Estados de autenticación
                    Spacer(modifier = Modifier.height(24.dp))

                    when (val state = authState.value) {
                        is AuthenticationManager.AuthResponse.Success -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡Inicio de sesión exitoso! 🎉",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        is AuthenticationManager.AuthResponse.Error -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Error: ${state.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        null -> Unit
                    }
                }
            }
        }
    }
}