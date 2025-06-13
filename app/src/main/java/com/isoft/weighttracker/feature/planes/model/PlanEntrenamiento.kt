package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

// ✅ MODELO ACTUALIZADO: Más flexible y práctico
data class Ejercicio(
    var nombre: String = "",
    var series: String = "",                    // Ej: "3", "4", etc.
    var repeticiones: String = "",              // Ej: "12", "8-10", "30 seg", etc.
    var peso: String = "",                      // Ej: "20kg", "15-20kg", "peso corporal", etc.
    var descanso: String = "",                  // Ej: "60 seg", "1-2 min", etc.
    var musculoTrabajado: String = "",          // Ej: "Pecho", "Espalda", "Piernas", etc.
    var tipoEjercicio: String = "",             // Ej: "Fuerza", "Cardio", "Estiramiento"
    var equipamiento: String = "",              // Ej: "Mancuernas", "Barra", "Peso corporal"
    var tecnica: String = "",                   // Instrucciones específicas de técnica
    var observaciones: String = ""              // Notas adicionales
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}

// ✅ NUEVO: Modelo para organizar ejercicios por sesiones
data class SesionEntrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",                    // Ej: "Tren Superior", "Cardio HIIT"
    var dia: String = "",                       // Ej: "Lunes", "Día 1"
    var tipoSesion: String = "FUERZA",         // "FUERZA", "CARDIO", "MIXTO", "ESTIRAMIENTO"
    var objetivo: String = "",                  // Objetivo específico de esta sesión
    var duracionMinutos: Int = 60,             // Duración estimada
    var intensidad: String = "MEDIA",          // "BAJA", "MEDIA", "ALTA"
    var gruposMusculares: List<String> = emptyList(), // Ej: ["Pecho", "Tríceps", "Hombros"]
    var calentamiento: String = "",            // Instrucciones de calentamiento
    var ejercicios: List<Ejercicio> = emptyList(),
    var enfriamiento: String = "",             // Instrucciones de enfriamiento/estiramiento
    var notas: String = "",                    // Notas específicas de la sesión
    var activa: Boolean = true                 // Si la sesión está activa en el plan
) {
    constructor() : this(
        UUID.randomUUID().toString(), "", "", "FUERZA", "", 60, "MEDIA",
        emptyList(), "", emptyList(), "", "", true
    )
}

// ✅ MODELO PRINCIPAL ACTUALIZADO
data class PlanEntrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var nombreProfesional: String = "",
    var estado: EstadoPlan = EstadoPlan.ACTIVO,

    // ✅ INFORMACIÓN BÁSICA DEL PLAN (Simplificada)
    var nombrePlan: String = "",               // Nombre descriptivo del plan
    var objetivo: String = "",                 // Objetivo principal (PERDER_GRASA, GANAR_MUSCULO, etc.)
    var duracionSemanas: Int = 8,             // Duración del programa en semanas
    var frecuenciaSemanal: String = "TRES_DIAS", // DOS_DIAS, TRES_DIAS, etc.
    var nivelDificultad: String = "INTERMEDIO", // PRINCIPIANTE, INTERMEDIO, AVANZADO
    var tipoPrograma: String = "PERSONALIZADO", // PERSONALIZADO, FUERZA, CARDIO, MIXTO

    // ✅ ESTRUCTURA MODERNA: Sesiones organizadas
    var sesiones: List<SesionEntrenamiento> = emptyList(),

    // ✅ CONFIGURACIÓN ADICIONAL
    var equipamientoNecesario: List<String> = emptyList(), // Lista de equipamiento requerido
    var lugarRealizacion: String = "FLEXIBLE",    // GIMNASIO, CASA, FLEXIBLE
    var adaptaciones: String = "",                // Adaptaciones para lesiones o limitaciones
    var progresion: String = "",                  // Indicaciones de progresión semanal
    var observacionesGenerales: String = "",      // Observaciones generales del plan

    // ✅ COMPATIBILIDAD: Mantenemos campos antiguos para no romper código existente
    @Deprecated("Usar objetivo en su lugar")
    var tipoEjercicio: String = "",
    @Deprecated("Usar lugarRealizacion en su lugar")
    var materialesSugeridos: String = "",
    @Deprecated("Usar frecuenciaSemanal en su lugar")
    var frecuencia: String = "",
    @Deprecated("Usar nivelDificultad en su lugar")
    var dificultad: String = "",
    @Deprecated("Calcular desde sesiones")
    var duracionEstimada: Int = 0,
    @Deprecated("Usar sesiones[].ejercicios en su lugar")
    var ejercicios: List<Ejercicio> = emptyList(),

    // Metadatos
    var fechaActivacion: Long? = null,
    var fechaDesactivacion: Long? = null,
    @Deprecated("Usar observacionesGenerales en su lugar")
    var observaciones: String = ""
) {
    // Constructor vacío para Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        fechaCreacion = System.currentTimeMillis(),
        usuarioId = "",
        profesionalId = "",
        nombreProfesional = "",
        estado = EstadoPlan.ACTIVO,
        nombrePlan = "",
        objetivo = "",
        duracionSemanas = 8,
        frecuenciaSemanal = "TRES_DIAS",
        nivelDificultad = "INTERMEDIO",
        tipoPrograma = "PERSONALIZADO",
        sesiones = emptyList(),
        equipamientoNecesario = emptyList(),
        lugarRealizacion = "FLEXIBLE",
        adaptaciones = "",
        progresion = "",
        observacionesGenerales = "",
        // Campos deprecated para compatibilidad
        tipoEjercicio = "",
        materialesSugeridos = "",
        frecuencia = "",
        dificultad = "",
        duracionEstimada = 0,
        ejercicios = emptyList(),
        fechaActivacion = null,
        fechaDesactivacion = null,
        observaciones = ""
    )

    // ✅ FUNCIONES HELPER ÚTILES

    /**
     * Obtiene todos los ejercicios de todas las sesiones
     */
    fun obtenerTodosLosEjercicios(): List<Ejercicio> {
        return sesiones.flatMap { it.ejercicios }
    }

    /**
     * Calcula la duración total estimada del plan
     */
    fun calcularDuracionTotal(): Int {
        return sesiones.sumOf { it.duracionMinutos }
    }

    /**
     * Obtiene todos los grupos musculares trabajados
     */
    fun obtenerGruposMusculares(): List<String> {
        return sesiones.flatMap { it.gruposMusculares }.distinct()
    }

    /**
     * Cuenta el número total de ejercicios
     */
    fun contarEjercicios(): Int {
        return sesiones.sumOf { it.ejercicios.size }
    }

    /**
     * Obtiene las sesiones activas
     */
    fun obtenerSesionesActivas(): List<SesionEntrenamiento> {
        return sesiones.filter { it.activa }
    }

    /**
     * Texto descriptivo del objetivo
     */
    fun getObjetivoTexto(): String {
        return when (objetivo) {
            "PERDER_GRASA" -> "Perder grasa corporal"
            "GANAR_MUSCULO" -> "Ganar masa muscular"
            "FUERZA" -> "Aumentar fuerza"
            "RESISTENCIA" -> "Mejorar resistencia"
            "TONIFICAR" -> "Tonificar y definir"
            "REHABILITACION" -> "Rehabilitación"
            else -> objetivo
        }
    }

    /**
     * Texto descriptivo de la frecuencia
     */
    fun getFrecuenciaTexto(): String {
        return when (frecuenciaSemanal) {
            "DOS_DIAS" -> "2 días por semana"
            "TRES_DIAS" -> "3 días por semana"
            "CUATRO_DIAS" -> "4 días por semana"
            "CINCO_DIAS" -> "5 días por semana"
            "SEIS_DIAS" -> "6 días por semana"
            else -> frecuenciaSemanal
        }
    }

    /**
     * Migra datos del formato antiguo al nuevo (para compatibilidad)
     */
    fun migrarFormatoAntiguo() {
        if (sesiones.isEmpty() && ejercicios.isNotEmpty()) {
            // Crear una sesión por defecto con los ejercicios antiguos
            val sesionPorDefecto = SesionEntrenamiento(
                nombre = "Sesión Principal",
                dia = "Flexible",
                tipoSesion = when (tipoEjercicio.lowercase()) {
                    "cardio" -> "CARDIO"
                    "fuerza" -> "FUERZA"
                    "resistencia" -> "CARDIO"
                    else -> "MIXTO"
                },
                ejercicios = ejercicios,
                duracionMinutos = duracionEstimada / 60,
                notas = observaciones
            )
            sesiones = listOf(sesionPorDefecto)
        }

        // Migrar otros campos si están vacíos
        if (nombrePlan.isEmpty() && tipoEjercicio.isNotEmpty()) {
            nombrePlan = "Plan de $tipoEjercicio"
        }

        if (observacionesGenerales.isEmpty() && observaciones.isNotEmpty()) {
            observacionesGenerales = observaciones
        }
    }
}