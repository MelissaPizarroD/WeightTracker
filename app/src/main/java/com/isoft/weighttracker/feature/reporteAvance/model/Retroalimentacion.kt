package com.isoft.weighttracker.feature.reporteAvance.model

import java.util.UUID

data class Retroalimentacion(
    val id: String = UUID.randomUUID().toString(), // id interno único para editar/eliminar si necesario
    val fecha: Long = System.currentTimeMillis(),
    val idProfesional: String,
    val contenido: String
)