package com.isoft.weighttracker.feature.selectRole.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectRoleScreen(
    onRoleSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Quién eres?",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { onRoleSelected("persona") }) {
            Text("Persona")
        }
        Button(onClick = { onRoleSelected("nutricionista") }) {
            Text("Nutricionista")
        }
        Button(onClick = { onRoleSelected("entrenador") }) {
            Text("Entrenador")
        }
    }
}