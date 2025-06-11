package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

data class ComidaDiaria(
    var nombre: String = "",
    var horaSugerida: String = "",
    var contenido: String = "",
    var porciones: String = ""
) {
    constructor() : this("", "", "", "")
}

data class PlanNutricional(
    var id: String = UUID.randomUUID().toString(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var usuarioId: String = "",
    var profesionalId: String = "",
    var nombreProfesional: String = "",
    var estado: EstadoPlan = EstadoPlan.ACTIVO,

    // Configuración del plan
    var frecuencia: String = "Lunes a Sábado", // "Lunes a Sábado", "Lunes a Domingo", "Lunes a Viernes"
    var repeticion: String = "diaria", // "cada 3 dias", "cada 2 dias", "diaria"

    // Comidas del día
    var desayuno: ComidaDiaria = ComidaDiaria(),
    var mediaMañana: ComidaDiaria = ComidaDiaria(),
    var almuerzo: ComidaDiaria = ComidaDiaria(),
    var mediaTarde: ComidaDiaria = ComidaDiaria(),
    var cena: ComidaDiaria = ComidaDiaria(),

    // Restricciones
    var alimentosNoPermitidos: String = "",
    var bebidasNoPermitidas: String = "",

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
        frecuencia = "Lunes a Sábado",
        repeticion = "diaria",
        desayuno = ComidaDiaria(),
        mediaMañana = ComidaDiaria(),
        almuerzo = ComidaDiaria(),
        mediaTarde = ComidaDiaria(),
        cena = ComidaDiaria(),
        alimentosNoPermitidos = "",
        bebidasNoPermitidas = "",
        fechaActivacion = null,
        fechaDesactivacion = null,
        observaciones = ""
    )
}