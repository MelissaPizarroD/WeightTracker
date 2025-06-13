package com.isoft.weighttracker.feature.planes.utils

import com.isoft.weighttracker.feature.planes.ui.EjercicioSimple
import com.isoft.weighttracker.feature.planes.ui.SesionEntrenamiento

/**
 * Utilidades para convertir entre diferentes formatos de ejercicios
 * Mantiene compatibilidad con el modelo legacy
 */
object EjercicioMapper {

    /**
     * Convierte las sesiones del formulario al modelo nuevo de SesionEntrenamiento
     */
    fun convertirASesionesReales(sesionesFormulario: List<SesionEntrenamiento>): List<com.isoft.weighttracker.feature.planes.model.SesionEntrenamiento> {
        return sesionesFormulario.map { sesionForm ->
            com.isoft.weighttracker.feature.planes.model.SesionEntrenamiento(
                nombre = sesionForm.nombre,
                dia = sesionForm.dia,
                tipoSesion = sesionForm.tipoSesion,
                objetivo = PlanEntrenamientoUtils.determinarObjetivoSesion(sesionForm.tipoSesion),
                duracionMinutos = sesionForm.duracionMinutos,
                intensidad = determinarIntensidad(sesionForm.tipoSesion),
                gruposMusculares = PlanEntrenamientoUtils.extraerGruposMusculares(sesionForm.ejercicios),
                calentamiento = generarCalentamiento(sesionForm.tipoSesion),
                ejercicios = convertirEjerciciosFormulario(sesionForm.ejercicios),
                enfriamiento = generarEnfriamiento(sesionForm.tipoSesion),
                notas = sesionForm.notas,
                activa = true
            )
        }
    }

    /**
     * Convierte ejercicios del formulario al modelo nuevo
     */
    fun convertirEjerciciosFormulario(ejerciciosForm: List<EjercicioSimple>): List<com.isoft.weighttracker.feature.planes.model.Ejercicio> {
        return ejerciciosForm.map { ejercicioForm ->
            com.isoft.weighttracker.feature.planes.model.Ejercicio(
                nombre = ejercicioForm.nombre,
                series = ejercicioForm.series,
                repeticiones = ejercicioForm.repeticiones,
                peso = ejercicioForm.peso,
                descanso = ejercicioForm.descanso,
                musculoTrabajado = PlanEntrenamientoUtils.determinarMusculoTrabajado(ejercicioForm.nombre),
                tipoEjercicio = PlanEntrenamientoUtils.determinarTipoEjercicio(ejercicioForm.nombre),
                equipamiento = PlanEntrenamientoUtils.determinarEquipamiento(ejercicioForm.nombre),
                tecnica = generarTecnicaBasica(ejercicioForm.nombre),
                observaciones = ejercicioForm.notas
            )
        }
    }

    /**
     * Convierte sesiones a la lista plana de ejercicios (compatibilidad legacy)
     */
    fun convertirSesionesAEjerciciosLegacy(sesiones: List<SesionEntrenamiento>): List<com.isoft.weighttracker.feature.planes.model.Ejercicio> {
        val ejerciciosLegacy = mutableListOf<com.isoft.weighttracker.feature.planes.model.Ejercicio>()

        sesiones.forEach { sesion ->
            sesion.ejercicios.forEach { ejercicioSimple ->
                if (ejercicioSimple.nombre.isNotBlank()) {
                    ejerciciosLegacy.add(
                        com.isoft.weighttracker.feature.planes.model.Ejercicio(
                            nombre = ejercicioSimple.nombre,
                            series = ejercicioSimple.series,
                            repeticiones = ejercicioSimple.repeticiones,
                            peso = ejercicioSimple.peso,
                            descanso = ejercicioSimple.descanso,
                            musculoTrabajado = PlanEntrenamientoUtils.determinarMusculoTrabajado(ejercicioSimple.nombre),
                            tipoEjercicio = PlanEntrenamientoUtils.determinarTipoEjercicio(ejercicioSimple.nombre),
                            equipamiento = PlanEntrenamientoUtils.determinarEquipamiento(ejercicioSimple.nombre),
                            tecnica = generarTecnicaBasica(ejercicioSimple.nombre),
                            observaciones = buildString {
                                append("${sesion.dia} - ${sesion.nombre}")
                                if (ejercicioSimple.notas.isNotEmpty()) {
                                    append(": ${ejercicioSimple.notas}")
                                }
                            }
                        )
                    )
                }
            }
        }

        return ejerciciosLegacy
    }

    // ===== FUNCIONES HELPER PRIVADAS =====

    private fun determinarIntensidad(tipoSesion: String): String {
        return when (tipoSesion) {
            "FUERZA" -> "ALTA"
            "CARDIO" -> "MEDIA"
            "MIXTO" -> "ALTA"
            "FUNCIONAL" -> "MEDIA"
            "ESTIRAMIENTO" -> "BAJA"
            else -> "MEDIA"
        }
    }

    private fun generarCalentamiento(tipoSesion: String): String {
        return when (tipoSesion) {
            "FUERZA" -> "5-10 min calentamiento dinámico + movilidad articular específica"
            "CARDIO" -> "5 min calentamiento progresivo + activación cardiovascular"
            "MIXTO" -> "10 min calentamiento completo + movilidad articular"
            "FUNCIONAL" -> "5-10 min movilidad dinámica + activación core"
            "ESTIRAMIENTO" -> "3-5 min calentamiento suave"
            else -> "5-10 minutos de calentamiento general"
        }
    }

    private fun generarEnfriamiento(tipoSesion: String): String {
        return when (tipoSesion) {
            "FUERZA" -> "5-10 min estiramiento estático + relajación"
            "CARDIO" -> "5 min vuelta a la calma + estiramiento cardiovascular"
            "MIXTO" -> "10 min estiramiento completo + relajación"
            "FUNCIONAL" -> "5-10 min estiramiento + movilidad"
            "ESTIRAMIENTO" -> "5 min relajación profunda"
            else -> "5-10 minutos de estiramiento y relajación"
        }
    }

    private fun generarTecnicaBasica(nombreEjercicio: String): String {
        val nombre = nombreEjercicio.lowercase()

        return when {
            nombre.contains("press") && nombre.contains("banca") ->
                "Mantener escápulas retraídas, descenso controlado, empuje explosivo"

            nombre.contains("squat") || nombre.contains("sentadilla") ->
                "Pies separados ancho hombros, descenso controlado, rodillas alineadas"

            nombre.contains("deadlift") || nombre.contains("peso muerto") ->
                "Espalda neutra, barra cerca del cuerpo, empuje con piernas"

            nombre.contains("row") || nombre.contains("remo") ->
                "Escápulas retraídas, codos cerca del cuerpo, contracción en espalda"

            nombre.contains("pull") && nombre.contains("up") ->
                "Agarre firme, descenso controlado, elevación completa"

            nombre.contains("plancha") || nombre.contains("plank") ->
                "Cuerpo recto, core activado, respiración controlada"

            nombre.contains("lunges") || nombre.contains("zancada") ->
                "Paso amplio, descenso vertical, rodilla no toca suelo"

            else -> "Mantener técnica correcta, movimiento controlado, respiración adecuada"
        }
    }
}