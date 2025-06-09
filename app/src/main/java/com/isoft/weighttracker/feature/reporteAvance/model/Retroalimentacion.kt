package com.isoft.weighttracker.feature.reporteAvance.model

import java.util.UUID

data class Retroalimentacion(
    val id: String = UUID.randomUUID().toString(),
    val fecha: Long = System.currentTimeMillis(),
    val idProfesional: String = "",
    val contenido: String = "",

    // ✅ NUEVOS CAMPOS para mostrar datos del profesional
    val nombreProfesional: String = "",
    val rolProfesional: String = "", // "nutricionista", "entrenador", etc.
    val emailProfesional: String = "" // opcional, por si quieres mostrarlo
)