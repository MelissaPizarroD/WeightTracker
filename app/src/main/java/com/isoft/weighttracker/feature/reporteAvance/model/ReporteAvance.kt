package com.isoft.weighttracker.feature.reporteAvance.model

import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import com.isoft.weighttracker.feature.metas.model.Meta
import com.isoft.weighttracker.feature.metas.viewmodel.ProgresoMeta

enum class TipoReporte {
    DIARIO, SEMANAL, QUINCENAL, MENSUAL
}

data class ReporteAvance(
    var id: String = "",
    var fechaCreacion: Long = System.currentTimeMillis(),
    var fechaInicio: Long = 0L,
    var fechaFin: Long = 0L,
    var tipoReporte: TipoReporte = TipoReporte.SEMANAL,

    var antropometria: List<Antropometria> = emptyList(),
    var metaActiva: Meta? = null,
    var progresoMeta: ProgresoMeta? = null,

    var caloriasConsumidas: Int = 0,
    var caloriasQuemadas: Int = 0,
    var pasosTotales: Int = 0,

    var retroalimentaciones: List<Retroalimentacion> = emptyList(),

    // ✅ NUEVO: Agregar sexo del usuario para saber qué medidas mostrar
    var sexoUsuario: String = "" // "Masculino" o "Femenino"
) {
    // Constructor sin argumentos para Firebase
    constructor() : this(
        id = "",
        fechaCreacion = System.currentTimeMillis(),
        fechaInicio = 0L,
        fechaFin = 0L,
        tipoReporte = TipoReporte.SEMANAL,
        antropometria = emptyList(),
        metaActiva = null,
        progresoMeta = null,
        caloriasConsumidas = 0,
        caloriasQuemadas = 0,
        pasosTotales = 0,
        retroalimentaciones = emptyList(),
        sexoUsuario = ""
    )

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
            val datosAnt = mutableMapOf<String, Any>(
                "peso" to ant.peso,
                "grasa" to ant.porcentajeGrasa,
                "cintura" to ant.cintura,
                "cuello" to ant.cuello
            )

            // ✅ Agregar cadera solo si existe (mujeres)
            ant.cadera?.let { cadera ->
                datosAnt["cadera"] = cadera
            }

            datosGraficas["antropometria"] = datosAnt
        }

        // Progreso (solo si está disponible)
        progresoMeta?.porcentajeProgreso?.let { progreso ->
            datosGraficas["progreso"] = progreso
        }

        return datosGraficas
    }
}

// No necesitamos redefinir Retroalimentacion aquí ya que existe en otro archivo
// Solo asegúrate de que tu modelo Retroalimentacion tenga constructor sin argumentos