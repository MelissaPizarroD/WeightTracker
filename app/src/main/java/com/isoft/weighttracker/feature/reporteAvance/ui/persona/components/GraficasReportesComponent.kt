package com.isoft.weighttracker.feature.reporteAvance.ui.persona.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// Función helper para abreviar nombres de tipos de reporte
private fun TipoReporte.displayName(esCompacto: Boolean = false): String {
    return if (esCompacto) {
        when (this) {
            TipoReporte.DIARIO -> "Diario"
            TipoReporte.SEMANAL -> "Semanal"
            TipoReporte.QUINCENAL -> "15 días"
            TipoReporte.MENSUAL -> "Mensual"
        }
    } else {
        when (this) {
            TipoReporte.DIARIO -> "Diario (último día)"
            TipoReporte.SEMANAL -> "Semanal (últimos 7 días)"
            TipoReporte.QUINCENAL -> "Quincenal (últimos 15 días)"
            TipoReporte.MENSUAL -> "Mensual (últimos 30 días)"
        }
    }
}

@Composable
fun GraficaDistribucionTipos(
    reportes: List<ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val distribucion = reportes.groupBy { it.tipoReporte }
        .mapValues { it.value.size }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Distribución por Tipo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (distribucion.isNotEmpty()) {
                val total = distribucion.values.sum()

                distribucion.forEach { (tipo, cantidad) ->
                    val porcentaje = (cantidad.toFloat() / total) * 100f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tipo.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(20.dp)
                                .background(
                                    Color.Gray.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(porcentaje / 100f)
                                    .background(
                                        when (tipo) {
                                            TipoReporte.DIARIO -> Color(0xFF4CAF50)
                                            TipoReporte.SEMANAL -> Color(0xFF2196F3)
                                            TipoReporte.QUINCENAL -> Color(0xFFFF9800)
                                            TipoReporte.MENSUAL -> Color(0xFF9C27B0)
                                        },
                                        RoundedCornerShape(10.dp)
                                    )
                            )
                        }

                        Text(
                            "$cantidad (%.0f%%)".format(porcentaje),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            } else {
                Text(
                    "No hay datos para mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GraficaEvolucionPeso(
    reportes: List<ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val datosAntropometria = reportes
        .mapNotNull { reporte ->
            reporte.antropometria.firstOrNull()?.let { ant ->
                Pair(reporte.fechaFin, ant.peso)
            }
        }
        .sortedBy { it.first }

    if (datosAntropometria.isEmpty()) {
        Card(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay datos de peso disponibles")
            }
        }
        return
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📈 Evolución del Peso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            GraficaLineas(
                datos = datosAntropometria,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colorLinea = Color(0xFF2196F3),
                mostrarPuntos = true
            )

            // Estadísticas
            val pesoInicial = datosAntropometria.first().second
            val pesoActual = datosAntropometria.last().second
            val diferencia = pesoActual - pesoInicial

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaPeso("Inicial", pesoInicial)
                EstadisticaPeso("Actual", pesoActual)
                EstadisticaPeso(
                    "Cambio",
                    diferencia,
                    color = if (diferencia < 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun GraficaBarrasActividad(
    reportes: List<ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val datosActividad = reportes.takeLast(7).map { reporte ->
        Triple(
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(reporte.fechaFin)),
            reporte.caloriasQuemadas,
            reporte.pasosTotales
        )
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🔥 Actividad Reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Selector de métrica
            var metricaSeleccionada by remember { mutableStateOf(0) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { metricaSeleccionada = 0 },
                    label = { Text("Calorías") },
                    selected = metricaSeleccionada == 0,
                    leadingIcon = if (metricaSeleccionada == 0) {
                        { Text("🔥", fontSize = 12.sp) }
                    } else null
                )
                FilterChip(
                    onClick = { metricaSeleccionada = 1 },
                    label = { Text("Pasos") },
                    selected = metricaSeleccionada == 1,
                    leadingIcon = if (metricaSeleccionada == 1) {
                        { Text("🚶‍♂️", fontSize = 12.sp) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título de lo que se está mostrando
            Text(
                if (metricaSeleccionada == 0) "Calorías Quemadas por Periodo" else "Pasos Totales por Periodo",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gráfica mejorada con labels
            GraficaBarrasConLabels(
                datos = datosActividad.map {
                    Triple(
                        it.first, // fecha
                        if (metricaSeleccionada == 0) it.second.toFloat() else it.third.toFloat(), // valor
                        if (metricaSeleccionada == 0) "cal" else "pasos" // unidad
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colorBarra = if (metricaSeleccionada == 0) Color(0xFFFF5722) else Color(0xFF4CAF50),
                metricaSeleccionada = metricaSeleccionada
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Resumen de la métrica actual
            val valorTotal = datosActividad.sumOf {
                if (metricaSeleccionada == 0) it.second else it.third
            }
            val valorPromedio = if (datosActividad.isNotEmpty()) valorTotal / datosActividad.size else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaActividad(
                    titulo = "Total",
                    valor = if (metricaSeleccionada == 0) "%,d cal".format(valorTotal) else "%,d pasos".format(valorTotal),
                    color = if (metricaSeleccionada == 0) Color(0xFFFF5722) else Color(0xFF4CAF50)
                )
                EstadisticaActividad(
                    titulo = "Promedio",
                    valor = if (metricaSeleccionada == 0) "%,d cal".format(valorPromedio) else "%,d pasos".format(valorPromedio),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GraficaBarrasConLabels(
    datos: List<Triple<String, Float, String>>, // fecha, valor, unidad
    modifier: Modifier = Modifier,
    colorBarra: Color = Color.Blue,
    metricaSeleccionada: Int
) {
    Canvas(modifier = modifier) {
        if (datos.isEmpty()) return@Canvas

        val padding = 40.dp.toPx()
        val labelHeight = 30.dp.toPx() // Espacio para labels abajo
        val graphWidth = size.width - 2 * padding
        val graphHeight = size.height - 2 * padding - labelHeight
        val baseY = padding + graphHeight // Línea base donde empiezan las barras

        val maxValue = datos.maxOf { it.second }
        if (maxValue == 0f) return@Canvas

        val barWidth = graphWidth / datos.size * 0.6f
        val barSpacing = graphWidth / datos.size * 0.4f

        // Dibujar línea base
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, baseY),
            end = Offset(size.width - padding, baseY),
            strokeWidth = 1.dp.toPx()
        )

        datos.forEachIndexed { index, (fecha, valor, unidad) ->
            val barHeight = (valor / maxValue) * graphHeight
            val x = padding + index * (barWidth + barSpacing)
            val y = baseY - barHeight // Las barras van hacia arriba desde la base

            // Dibujar barra (desde la base hacia arriba)
            drawRect(
                color = colorBarra,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // Dibujar valor encima de la barra
            if (barHeight > 15.dp.toPx()) {
                drawContext.canvas.nativeCanvas.apply {
                    val valueText = if (metricaSeleccionada == 0) {
                        if (valor >= 1000) "%.1fk".format(valor / 1000) else "%.0f".format(valor)
                    } else {
                        if (valor >= 1000) "%.1fk".format(valor / 1000) else "%.0f".format(valor)
                    }

                    drawText(
                        valueText,
                        x + barWidth / 2,
                        y - 8.dp.toPx(),
                        Paint().apply {
                            color = colorBarra.toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }

            // Dibujar fecha debajo de la línea base
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    fecha,
                    x + barWidth / 2,
                    baseY + 20.dp.toPx(),
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 9.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
private fun EstadisticaActividad(
    titulo: String,
    valor: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GraficaCircularProgreso(
    reportes: List<ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val progresoPromedio = reportes
        .mapNotNull { it.progresoMeta?.porcentajeProgreso }
        .average()
        .takeIf { !it.isNaN() } ?: 0.0

    val progresoAnimado by animateFloatAsState(
        targetValue = (progresoPromedio / 100f).toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "progreso"
    )

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎯 Progreso de Meta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Círculo de fondo
                    drawCircle(
                        color = Color(0xFFE0E0E0),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Arco de progreso
                    val sweepAngle = 360f * progresoAnimado
                    drawArc(
                        color = when {
                            progresoPromedio >= 80 -> Color(0xFF4CAF50)
                            progresoPromedio >= 50 -> Color(0xFFFF9800)
                            else -> Color(0xFFf44336)
                        },
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                }

                Text(
                    "${progresoPromedio.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        progresoPromedio >= 80 -> Color(0xFF4CAF50)
                        progresoPromedio >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFf44336)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when {
                    progresoPromedio >= 80 -> "¡Excelente progreso!"
                    progresoPromedio >= 50 -> "Buen avance"
                    progresoPromedio > 0 -> "Sigue así"
                    else -> "Comienza tu meta"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GraficaComparativaReportes(
    reportes: List<ReporteAvance>,
    modifier: Modifier = Modifier
) {
    val ultimosDatos = reportes.takeLast(5).map { reporte ->
        mapOf(
            "fecha" to SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(reporte.fechaFin)),
            "calorias" to reporte.caloriasQuemadas,
            "pasos" to reporte.pasosTotales,
            "peso" to (reporte.antropometria.firstOrNull()?.peso ?: 0f)
        )
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Comparativa de Reportes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (ultimosDatos.size >= 2) {
                val actual = ultimosDatos.last()
                val anterior = ultimosDatos[ultimosDatos.size - 2]

                ComparacionMetrica(
                    titulo = "Calorías Quemadas",
                    valorActual = actual["calorias"] as Int,
                    valorAnterior = anterior["calorias"] as Int,
                    icono = "🔥"
                )

                ComparacionMetrica(
                    titulo = "Pasos Totales",
                    valorActual = actual["pasos"] as Int,
                    valorAnterior = anterior["pasos"] as Int,
                    icono = "🚶‍♂️"
                )

                val pesoActual = actual["peso"] as Float
                val pesoAnterior = anterior["peso"] as Float
                if (pesoActual > 0 && pesoAnterior > 0) {
                    ComparacionMetrica(
                        titulo = "Peso Corporal",
                        valorActual = pesoActual,
                        valorAnterior = pesoAnterior,
                        icono = "⚖️",
                        unidad = "kg",
                        esDecimal = true
                    )
                }
            } else {
                Text(
                    "Necesitas al menos 2 reportes para comparar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GraficaLineas(
    datos: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier,
    colorLinea: Color = Color.Blue,
    mostrarPuntos: Boolean = false
) {
    Canvas(modifier = modifier) {
        if (datos.size < 2) return@Canvas

        val padding = 40.dp.toPx()
        val graphWidth = size.width - 2 * padding
        val graphHeight = size.height - 2 * padding

        val minY = datos.minOf { it.second }
        val maxY = datos.maxOf { it.second }
        val rangeY = maxY - minY

        val path = Path()

        datos.forEachIndexed { index, (_, valor) ->
            val x = padding + (index * graphWidth / (datos.size - 1))
            val y = padding + graphHeight - ((valor - minY) / rangeY * graphHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            if (mostrarPuntos) {
                drawCircle(
                    color = colorLinea,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        drawPath(
            path = path,
            color = colorLinea,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun GraficaBarras(
    datos: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    colorBarra: Color = Color.Blue,
    unidad: String = ""
) {
    Canvas(modifier = modifier) {
        if (datos.isEmpty()) return@Canvas

        val padding = 40.dp.toPx()
        val graphWidth = size.width - 2 * padding
        val graphHeight = size.height - 2 * padding

        val maxValue = datos.maxOf { it.second }
        val barWidth = graphWidth / datos.size * 0.7f
        val barSpacing = graphWidth / datos.size * 0.3f

        datos.forEachIndexed { index, (_, valor) ->
            val barHeight = (valor / maxValue) * graphHeight
            val x = padding + index * (barWidth + barSpacing)
            val y = padding + graphHeight - barHeight

            drawRect(
                color = colorBarra,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun EstadisticaPeso(
    titulo: String,
    valor: Float,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${if (valor >= 0 && titulo == "Cambio") "+" else ""}%.1f kg".format(valor),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparacionMetrica(
    titulo: String,
    valorActual: Number,
    valorAnterior: Number,
    icono: String,
    unidad: String = "",
    esDecimal: Boolean = false
) {
    val diferencia = valorActual.toFloat() - valorAnterior.toFloat()
    val porcentajeCambio = if (valorAnterior.toFloat() != 0f) {
        (diferencia / valorAnterior.toFloat()) * 100
    } else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icono, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (esDecimal) "%.1f %s".format(valorActual.toFloat(), unidad)
                else "%,d %s".format(valorActual.toInt(), unidad),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = when {
                    diferencia > 0 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    diferencia < 0 -> Color(0xFFf44336).copy(alpha = 0.1f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    when {
                        diferencia > 0 -> "↗ +%.1f%%".format(abs(porcentajeCambio))
                        diferencia < 0 -> "↘ -%.1f%%".format(abs(porcentajeCambio))
                        else -> "→ 0%"
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        diferencia > 0 -> Color(0xFF4CAF50)
                        diferencia < 0 -> Color(0xFFf44336)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}