package com.isoft.weighttracker.feature.reporteAvance.model

import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.metas.model.Meta
import com.isoft.weighttracker.feature.metas.viewmodel.ProgresoMeta

enum class TipoReporte {
    DIARIO, SEMANAL, QUINCENAL, MENSUAL
}

data class ReporteAvance(
    val id: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaInicio: Long = 0L,
    val fechaFin: Long = 0L,
    val tipoReporte: TipoReporte = TipoReporte.SEMANAL,

    val antropometria: List<Antropometria> = emptyList(),
    val metaActiva: Meta? = null,
    val progresoMeta: ProgresoMeta? = null,

    val caloriasConsumidas: Int = 0,
    val caloriasQuemadas: Int = 0,
    val pasosTotales: Int = 0,

    val retroalimentaciones: List<Retroalimentacion> = emptyList()
) {
    // Función helper para obtener datos para gráficas
    fun obtenerDatosGraficas(): Map<String, Any> {
        val datosGraficas = mutableMapOf<String, Any>()

        // Datos de calorías (siempre disponibles)
        datosGraficas["calorias"] = mapOf(
            "consumidas" to caloriasConsumidas,
            "quemadas" to caloriasQuemadas,
            "balance" to (caloriasConsumidas - caloriasQuemadas)
        )

        // Pasos
        datosGraficas["pasos"] = pasosTotales

        // Antropometría (solo si está disponible)
        antropometria.firstOrNull()?.let { ant ->
            datosGraficas["antropometria"] = mapOf(
                "peso" to ant.peso,
                "grasa" to ant.porcentajeGrasa,
                "cintura" to ant.cintura
            )
        }

        // Progreso (solo si está disponible)
        progresoMeta?.porcentajeProgreso?.let { progreso ->
            datosGraficas["progreso"] = progreso
        }

        return datosGraficas
    }
}