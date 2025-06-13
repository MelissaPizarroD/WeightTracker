package com.isoft.weighttracker.feature.planes.model

import com.google.firebase.firestore.Exclude
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
    var objetivoNutricion: String = "",
    var nivelActividad: String = "",
    var restricciones: List<String> = emptyList(),
    var restriccionesOtras: String = "",
    var restriccionesMedicas: String = "",

    // CAMPOS ESPECÍFICOS DE ENTRENAMIENTO
    var objetivoEntrenamiento: String = "",
    var experienciaPrevia: String = "",
    var disponibilidadSemanal: String = "",
    var equipamientoDisponible: List<String> = emptyList(),

    // ID del plan creado (cuando el profesional complete la solicitud)
    var planCreado: String? = null,
    var fechaCompletada: Long? = null,

    // ✅ NUEVOS CAMPOS PARA RECHAZO
    var motivoRechazo: String? = null,
    var fechaRechazo: Long? = null
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
        fechaCompletada = null,
        motivoRechazo = null,
        fechaRechazo = null
    )

    // ✅ FIXED: Helper functions con @Exclude para evitar errores de Firebase
    @Exclude
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

    @Exclude
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

    @Exclude
    fun getExperienciaPreviaTexto(): String {
        return when (experienciaPrevia) {
            "PRINCIPIANTE" -> "Principiante (menos de 6 meses)"
            "INTERMEDIO" -> "Intermedio (6 meses - 2 años)"
            "AVANZADO" -> "Avanzado (más de 2 años)"
            else -> experienciaPrevia
        }
    }

    @Exclude
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

    @Exclude
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
}