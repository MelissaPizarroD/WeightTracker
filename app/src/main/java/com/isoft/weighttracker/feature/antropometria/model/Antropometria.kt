package com.isoft.weighttracker.feature.antropometria.model

data class Antropometria(
    val id: String? = null,
    val fecha: Long = System.currentTimeMillis(),        // Fecha actual del registro
    val peso: Float = 0f,
    val cintura: Float = 0f,
    val cuello: Float = 0f,
    val cadera: Float? = null,                           // Solo si es mujer
    val imc: Float = 0f,
    val porcentajeGrasa: Float = 0f,                  // Día para próxima medición
)