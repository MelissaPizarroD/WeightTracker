package com.isoft.weighttracker.feature.actividadfisica.model

data class ActividadFisica(
    val id: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val tipo: String = "",
    val duracionMin: Int = 0,
    val caloriasQuemadas: Int = 0
)