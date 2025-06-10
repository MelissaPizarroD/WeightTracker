package com.isoft.weighttracker.feature.reporteAvance.model

import java.util.UUID

data class Retroalimentacion(
    var id: String = UUID.randomUUID().toString(),
    var fecha: Long = System.currentTimeMillis(),
    var idProfesional: String = "",
    var contenido: String = "",

    // ✅ NUEVOS CAMPOS para mostrar datos del profesional
    var nombreProfesional: String = "",
    var rolProfesional: String = "", // "nutricionista", "entrenador", etc.
    var emailProfesional: String = "" // opcional, por si quieres mostrarlo
) {
    // Constructor sin argumentos para Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        fecha = System.currentTimeMillis(),
        idProfesional = "",
        contenido = "",
        nombreProfesional = "",
        rolProfesional = "",
        emailProfesional = ""
    )
}