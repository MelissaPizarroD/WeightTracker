package com.isoft.weighttracker.feature.planes.model

/**
 * Modelo para representar los alimentos de consumo ocasional y moderado
 * que se permiten en el plan nutricional
 */
data class ConsumoOcasional(
    var embutidosCarnesGrasas: Boolean = false, // Si se permiten embutidos y carnes grasas ocasionalmente
    var dulcesSnacksRefrescos: Boolean = false, // Si se permiten dulces, snacks y refrescos ocasionalmente
    var mantequillaMargarinaBolleria: Boolean = false, // Si se permiten mantequilla, margarina y bollería ocasionalmente
    var observaciones: String = "" // Observaciones sobre la frecuencia y condiciones del consumo ocasional
) {
    // Constructor vacío requerido por Firebase
    constructor() : this(false, false, false, "")
}