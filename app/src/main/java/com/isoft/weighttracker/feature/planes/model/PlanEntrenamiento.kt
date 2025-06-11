package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

data class Ejercicio(
    var nombreEjercicio: String = "",
    var musculoTrabajado: String = "Superior", // "Superior", "Intermedio", "Inferior"
    var repeticiones: String = "3x12", // "2x10", "4x15", "3x12", "1x20"
    var pesoRecomendado: Float = 0f,
    var observaciones: String = ""
) {
    constructor() : this("", "Superior", "3x12", 0f, "")
}

data class PlanEntrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var nombreProfesional: String = "",
    var estado: EstadoPlan = EstadoPlan.ACTIVO,

    // Configuración del plan
    var tipoEjercicio: String = "Cardio", // "Cardio", "Fuerza", "Resistencia"
    var lugarRealizacion: String = "gimnasio", // "casa", "gimnasio"
    var materialesSugeridos: String = "",
    var frecuencia: String = "3 veces por semana", // "2 veces por semana", "3 veces por semana", "4 o mas veces"
    var dificultad: String = "Medio", // "Facil", "Medio", "Dificil"
    var duracionEstimada: Int = 0, // En segundos (para completar por entrenador)

    // Lista de ejercicios
    var ejercicios: List<Ejercicio> = emptyList(),

    // Metadatos
    var fechaActivacion: Long? = null,
    var fechaDesactivacion: Long? = null,
    var observaciones: String = ""
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        fechaCreacion = System.currentTimeMillis(),
        usuarioId = "",
        profesionalId = "",
        nombreProfesional = "",
        estado = EstadoPlan.ACTIVO,
        tipoEjercicio = "Cardio",
        lugarRealizacion = "gimnasio",
        materialesSugeridos = "",
        frecuencia = "3 veces por semana",
        dificultad = "Medio",
        duracionEstimada = 0,
        ejercicios = emptyList(),
        fechaActivacion = null,
        fechaDesactivacion = null,
        observaciones = ""
    )
}