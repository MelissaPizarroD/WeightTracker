package com.isoft.weighttracker.feature.planes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isoft.weighttracker.feature.planes.model.CategoriaAlimento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaAlimentoCard(
    titulo: String,
    categoria: CategoriaAlimento,
    onCategoriaChanged: (CategoriaAlimento) -> Unit,
    isExpanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    tipoFrecuencia: String = "DIARIA", // "DIARIA" o "SEMANAL"
    tiposEspecificos: List<String>? = null,
    frecuenciaSugerida: String = "",
    unidadMedida: String = "g",
    pesoDefecto: Double = 0.0,
    conAlternar: Boolean = false,
    showValidation: Boolean = false // ✅ NUEVO: Parámetro para mostrar validaciones
) {
    // ✅ VALIDACIÓN: Verificar si la categoría activa tiene errores
    val tieneErrores = if (categoria.activo && showValidation) {
        when (tipoFrecuencia) {
            "DIARIA" -> categoria.racionesPorDia <= 0 || categoria.pesoPorRacion <= 0
            "SEMANAL" -> categoria.racionesPorSemana <= 0 || categoria.pesoPorRacion <= 0
            else -> false
        }
    } else false

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !categoria.activo -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                tieneErrores -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (tieneErrores) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header de la card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Switch(
                        checked = categoria.activo,
                        onCheckedChange = {
                            onCategoriaChanged(categoria.copy(activo = it))
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (frecuenciaSugerida.isNotEmpty()) {
                            Text(
                                text = frecuenciaSugerida,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ✅ INDICADOR DE ERROR
                        if (tieneErrores) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Faltan datos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { onExpandChanged(!isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                    )
                }
            }

            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded && categoria.activo,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Frecuencia personalizada (solo si no es la sugerida)
                    OutlinedTextField(
                        value = categoria.frecuenciaDiaria,
                        onValueChange = {
                            onCategoriaChanged(categoria.copy(frecuenciaDiaria = it))
                        },
                        label = { Text("Frecuencia personalizada") },
                        placeholder = { Text(frecuenciaSugerida) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tipo específico (si aplica)
                    if (!tiposEspecificos.isNullOrEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = categoria.tipoespecifico,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Tipo") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                tiposEspecificos.forEach { tipo ->
                                    DropdownMenuItem(
                                        text = { Text(tipo) },
                                        onClick = {
                                            onCategoriaChanged(categoria.copy(tipoespecifico = tipo))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Row para raciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Raciones por día/semana
                        OutlinedTextField(
                            value = if (tipoFrecuencia == "DIARIA") {
                                if (categoria.racionesPorDia > 0) categoria.racionesPorDia.toString() else ""
                            } else {
                                if (categoria.racionesPorSemana > 0) categoria.racionesPorSemana.toString() else ""
                            },
                            onValueChange = { value ->
                                val numero = value.toIntOrNull() ?: 0
                                if (tipoFrecuencia == "DIARIA") {
                                    onCategoriaChanged(categoria.copy(racionesPorDia = numero))
                                } else {
                                    onCategoriaChanged(categoria.copy(racionesPorSemana = numero))
                                }
                            },
                            label = {
                                Text(if (tipoFrecuencia == "DIARIA") "Raciones/día" else "Raciones/semana")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        // Peso por ración
                        OutlinedTextField(
                            value = if (categoria.pesoPorRacion > 0) categoria.pesoPorRacion.toString() else if (pesoDefecto > 0) pesoDefecto.toString() else "",
                            onValueChange = { value ->
                                val peso = value.toDoubleOrNull() ?: 0.0
                                onCategoriaChanged(categoria.copy(pesoPorRacion = peso))
                            },
                            label = { Text("$unidadMedida/ración") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Checkbox para alternar (solo carnes/huevos)
                    if (conAlternar) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = categoria.alternarConOtros,
                                onCheckedChange = { checked ->
                                    onCategoriaChanged(categoria.copy(alternarConOtros = checked))
                                }
                            )
                            Text("Alternar carnes y huevos")
                        }
                    }

                    // Observaciones específicas de la categoría
                    OutlinedTextField(
                        value = categoria.observaciones,
                        onValueChange = {
                            onCategoriaChanged(categoria.copy(observaciones = it))
                        },
                        label = { Text("Observaciones") },
                        placeholder = { Text("Alimentos específicos que sí/no debe comer de esta categoría...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }
    }
}