package com.isoft.weighttracker.feature.planes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.isoft.weighttracker.feature.planes.model.ConsumoOcasional

@Composable
fun ConsumoOcasionalCard(
    consumoOcasional: ConsumoOcasional,
    onConsumoChanged: (ConsumoOcasional) -> Unit,
    isExpanded: Boolean,
    onExpandChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header de la card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❌ Consumo ocasional y moderado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onExpandChanged(!isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                    )
                }
            }

            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Marcar los alimentos que se permiten ocasionalmente:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Embutidos y carnes grasas
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = consumoOcasional.embutidosCarnesGrasas,
                            onCheckedChange = { checked ->
                                onConsumoChanged(consumoOcasional.copy(embutidosCarnesGrasas = checked))
                            }
                        )
                        Text("🥓 Embutidos y carnes grasas")
                    }

                    // Dulces, snacks, refrescos
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = consumoOcasional.dulcesSnacksRefrescos,
                            onCheckedChange = { checked ->
                                onConsumoChanged(consumoOcasional.copy(dulcesSnacksRefrescos = checked))
                            }
                        )
                        Text("🍭 Dulces, snacks, refrescos")
                    }

                    // Mantequilla, margarina y bollería
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = consumoOcasional.mantequillaMargarinaBolleria,
                            onCheckedChange = { checked ->
                                onConsumoChanged(consumoOcasional.copy(mantequillaMargarinaBolleria = checked))
                            }
                        )
                        Text("🧈 Mantequilla, margarina y bollería")
                    }

                    // Observaciones para consumo ocasional
                    OutlinedTextField(
                        value = consumoOcasional.observaciones,
                        onValueChange = {
                            onConsumoChanged(consumoOcasional.copy(observaciones = it))
                        },
                        label = { Text("Observaciones sobre consumo ocasional") },
                        placeholder = { Text("Frecuencia permitida, restricciones especiales...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }
    }
}