package com.isoft.weighttracker.feature.planes.model

import java.util.UUID

enum class TipoPlan {
    ENTRENAMIENTO, NUTRICION
}

enum class EstadoSolicitud {
    PENDIENTE, EN_PROGRESO, COMPLETADA, RECHAZADA
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
        planCreado = null,
        fechaCompletada = null
    )
}