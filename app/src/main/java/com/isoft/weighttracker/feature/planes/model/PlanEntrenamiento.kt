package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

// ✅ MODELO SIMPLIFICADO DE EJERCICIO
data class Ejercicio(
    var nombre: String = "",
    var series: String = "",                    // Mantener como String por flexibilidad ("3", "4x4", etc.)
    var repeticiones: String = "",              // String para permitir "12", "8-10", "30 seg"
    var descanso: String = "",                  // En minutos
    var musculoTrabajado: String = "",          // Músculo principal trabajado
    var observaciones: String = ""              // Notas adicionales
) {
    constructor() : this("", "", "", "", "", "")
}

// ✅ MODELO DE SESIÓN SIMPLIFICADO
data class SesionEntrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",                    // Nombre de la sesión
    var dia: String = "",                       // Día de la semana
    var tipoSesion: String = "FUERZA",         // Tipo de sesión
    var duracionMinutos: Int = 60,             // Duración en minutos
    var ejercicios: List<Ejercicio> = emptyList()
) {
    constructor() : this(
        UUID.randomUUID().toString(), "", "", "FUERZA", 60, emptyList()
    )
}

// ✅ MODELO PRINCIPAL SIMPLIFICADO
data class PlanEntrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var nombreProfesional: String = "",
    var estado: EstadoPlan = EstadoPlan.ACTIVO,

    // ✅ INFORMACIÓN BÁSICA DEL PLAN
    var nombrePlan: String = "",
    var objetivo: String = "",
    var duracionSemanas: Int = 8,
    var frecuenciaSemanal: String = "TRES_DIAS",

    // ✅ SESIONES
    var sesiones: List<SesionEntrenamiento> = emptyList(),

    // Metadatos
    var fechaActivacion: Long? = null,
    var fechaDesactivacion: Long? = null
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
        sesiones = emptyList(),
        fechaActivacion = null,
        fechaDesactivacion = null
    )

    // ✅ FUNCIONES HELPER
    fun obtenerTodosLosEjercicios(): List<Ejercicio> {
        return sesiones.flatMap { it.ejercicios }
    }

    fun calcularDuracionTotal(): Int {
        return sesiones.sumOf { it.duracionMinutos }
    }

    fun contarEjerciciosTotales(): Int {
        return sesiones.sumOf { it.ejercicios.size }
    }
}