package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

enum class TipoPlan {
    ENTRENAMIENTO, NUTRICION
}

enum class EstadoSolicitud {
    PENDIENTE, EN_PROGRESO, COMPLETADA, RECHAZADA
}

// ENUMS para los campos adicionales
enum class ObjetivoNutricional {
    PERDER_PESO, MANTENER_PESO, SUBIR_PESO, MEJORAR_COMPOSICION, CONTROL_MEDICO
}

enum class NivelActividad {
    SEDENTARIO, LIGERO, MODERADO, INTENSO
}

enum class RestriccionAlimentaria {
    SIN_LACTOSA, SIN_GLUTEN, VEGETARIANO, VEGANO, RESTRICCIONES_MEDICAS
}

// NUEVOS ENUMS para entrenamiento
enum class ObjetivoEntrenamiento {
    PERDER_GRASA, GANAR_MUSCULO, FUERZA, RESISTENCIA, TONIFICAR, REHABILITACION
}

enum class ExperienciaPrevia {
    PRINCIPIANTE, INTERMEDIO, AVANZADO
}

enum class DisponibilidadSemanal {
    DOS_DIAS, TRES_DIAS, CUATRO_DIAS, CINCO_DIAS, SEIS_DIAS
}

enum class EquipamientoDisponible {
    GIMNASIO_COMPLETO, MANCUERNAS, PESAS_LIBRES, BANDAS_ELASTICAS, SOLO_PESO_CORPORAL
}

data class SolicitudPlan(
    var id: String = UUID.randomUUID().toString(),
    var fechaSolicitud: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var tipoPlan: TipoPlan = TipoPlan.ENTRENAMIENTO,
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    var descripcion: String = "",
    var observaciones: String = "",

    // Datos del usuario para mostrar al profesional
    var nombreUsuario: String = "",
    var emailUsuario: String = "",

    // CAMPOS ESPECÍFICOS DE NUTRICIÓN
    var objetivoNutricion: String = "", // ObjetivoNutricional como String
    var nivelActividad: String = "", // NivelActividad como String
    var restricciones: List<String> = emptyList(), // Lista de RestriccionAlimentaria como String
    var restriccionesOtras: String = "", // Campo libre para alergias/intolerancias específicas
    var restriccionesMedicas: String = "", // Campo libre para restricciones médicas específicas

    // CAMPOS ESPECÍFICOS DE ENTRENAMIENTO
    var objetivoEntrenamiento: String = "", // Objetivo del entrenamiento
    var experienciaPrevia: String = "", // Nivel de experiencia
    var disponibilidadSemanal: String = "", // Días disponibles para entrenar
    var equipamientoDisponible: List<String> = emptyList(), // Equipamiento que tiene

    // ID del plan creado (cuando el profesional complete la solicitud)
    var planCreado: String? = null,
    var fechaCompletada: Long? = null
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        fechaSolicitud = System.currentTimeMillis(),
        usuarioId = "",
        profesionalId = "",
        tipoPlan = TipoPlan.ENTRENAMIENTO,
        estado = EstadoSolicitud.PENDIENTE,
        descripcion = "",
        observaciones = "",
        nombreUsuario = "",
        emailUsuario = "",
        objetivoNutricion = "",
        nivelActividad = "",
        restricciones = emptyList(),
        restriccionesOtras = "",
        restriccionesMedicas = "",
        objetivoEntrenamiento = "",
        experienciaPrevia = "",
        disponibilidadSemanal = "",
        equipamientoDisponible = emptyList(),
        planCreado = null,
        fechaCompletada = null
    )

    // Helper functions para obtener texto legible
    fun getObjetivoNutricionTexto(): String {
        return when (objetivoNutricion) {
            "PERDER_PESO" -> "Perder peso"
            "MANTENER_PESO" -> "Mantener peso actual"
            "SUBIR_PESO" -> "Subir de peso/masa muscular"
            "MEJORAR_COMPOSICION" -> "Mejorar composición corporal"
            "CONTROL_MEDICO" -> "Control médico específico"
            else -> objetivoNutricion
        }
    }

    fun getObjetivoEntrenamientoTexto(): String {
        return when (objetivoEntrenamiento) {
            "PERDER_GRASA" -> "Perder grasa corporal"
            "GANAR_MUSCULO" -> "Ganar masa muscular"
            "FUERZA" -> "Aumentar fuerza"
            "RESISTENCIA" -> "Mejorar resistencia"
            "TONIFICAR" -> "Tonificar y definir"
            "REHABILITACION" -> "Rehabilitación/terapéutico"
            else -> objetivoEntrenamiento
        }
    }

    fun getExperienciaPreviaTexto(): String {
        return when (experienciaPrevia) {
            "PRINCIPIANTE" -> "Principiante (menos de 6 meses)"
            "INTERMEDIO" -> "Intermedio (6 meses - 2 años)"
            "AVANZADO" -> "Avanzado (más de 2 años)"
            else -> experienciaPrevia
        }
    }

    fun getDisponibilidadSemanalTexto(): String {
        return when (disponibilidadSemanal) {
            "DOS_DIAS" -> "2 días por semana"
            "TRES_DIAS" -> "3 días por semana"
            "CUATRO_DIAS" -> "4 días por semana"
            "CINCO_DIAS" -> "5 días por semana"
            "SEIS_DIAS" -> "6 días por semana"
            else -> disponibilidadSemanal
        }
    }

    fun getEquipamientoTexto(): String {
        if (equipamientoDisponible.isEmpty()) return "No especificado"

        return equipamientoDisponible.joinToString(", ") { equipo ->
            when (equipo) {
                "GIMNASIO_COMPLETO" -> "Gimnasio completo"
                "MANCUERNAS" -> "Mancuernas"
                "PESAS_LIBRES" -> "Pesas libres"
                "BANDAS_ELASTICAS" -> "Bandas elásticas"
                "SOLO_PESO_CORPORAL" -> "Solo peso corporal"
                else -> equipo
            }
        }
    }

    fun getNivelActividadTexto(): String {
        return when (nivelActividad) {
            "SEDENTARIO" -> "Sedentario (poco ejercicio)"
            "LIGERO" -> "Ligero (1-3 días/semana)"
            "MODERADO" -> "Moderado (3-5 días/semana)"
            "INTENSO" -> "Intenso (6-7 días/semana)"
            else -> nivelActividad
        }
    }

    fun getRestriccionesTexto(): String {
        val textoRestricciones = mutableListOf<String>()

        // Agregar restricciones predefinidas
        restricciones.forEach { restriccion ->
            when (restriccion) {
                "SIN_LACTOSA" -> textoRestricciones.add("Sin lactosa")
                "SIN_GLUTEN" -> textoRestricciones.add("Sin gluten")
                "VEGETARIANO" -> textoRestricciones.add("Vegetariano")
                "VEGANO" -> textoRestricciones.add("Vegano")
                "RESTRICCIONES_MEDICAS" -> textoRestricciones.add("Restricciones médicas")
            }
        }

        // Agregar restricciones específicas si las hay
        if (restriccionesOtras.isNotEmpty()) {
            textoRestricciones.add("Otras: $restriccionesOtras")
        }

        if (restriccionesMedicas.isNotEmpty()) {
            textoRestricciones.add("Médicas: $restriccionesMedicas")
        }

        return if (textoRestricciones.isEmpty()) "Ninguna" else textoRestricciones.joinToString(", ")
    }
}