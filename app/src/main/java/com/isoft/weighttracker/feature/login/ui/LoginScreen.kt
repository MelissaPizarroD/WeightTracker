package com.isoft.weighttracker.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.isoft.weighttracker.R
import com.isoft.weighttracker.core.auth.AuthenticationManager
import com.isoft.weighttracker.feature.login.viewmodel.LoginViewModel
import com.isoft.weighttracker.feature.login.viewmodel.NavigationEvent

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = { viewModel.loginWithGoogle() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.goog),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Inicia Sesión con Google")
            }

            when (val state = authState.value) {
                is AuthenticationManager.AuthResponse.Success -> {
                    Text(text = "Inicio de sesión exitoso 🎉")
                }

                is AuthenticationManager.AuthResponse.Error -> {
                    Text(text = "Error: ${state.message}")
                }

                null -> Unit
            }
        }
    }
}