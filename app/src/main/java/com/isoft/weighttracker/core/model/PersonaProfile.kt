package com.isoft.weighttracker.core.model

data class PersonaProfile(
    val edad: Int = 0,
    val sexo: String = "",
    val estatura: Float = 0f,
    val antecedentesMedicos: String = "",
    val frecuenciaMedicion: String = "semanal",
    val horaRecordatorio: Int? = 10,
    val minutoRecordatorio: Int? = 0,
    val recordatorioActivo: Boolean = false,
    val contadorPasosActivo: Boolean = false
)