package com.isoft.weighttracker.feature.planes.utils

import com.isoft.weighttracker.feature.planes.model.SolicitudPlan
import com.isoft.weighttracker.feature.planes.ui.EjercicioSimple
import com.isoft.weighttracker.feature.planes.ui.SesionEntrenamiento

/**
 * Utilidades para crear y gestionar planes de entrenamiento
 */
object PlanEntrenamientoUtils {

    // ===== FUNCIONES DE CLASIFICACIÓN =====

    /**
     * Clasifica el IMC según estándares médicos
     */
    fun clasificarIMC(imc: Float): String {
        return when {
            imc < 18.5 -> "Bajo peso"
            imc < 25.0 -> "Normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidad"
        }
    }

    /**
     * Determina el grupo muscular trabajado según el nombre del ejercicio
     */
    fun determinarMusculoTrabajado(nombreEjercicio: String): String {
        val nombre = nombreEjercicio.lowercase()
        return when {
            // Tren superior
            nombre.contains("press") || nombre.contains("pecho") || nombre.contains("bench") -> "Pecho"
            nombre.contains("pull") || nombre.contains("espalda") || nombre.contains("dorsales") -> "Espalda"
            nombre.contains("hombro") || nombre.contains("shoulder") -> "Hombros"
            nombre.contains("bicep") -> "Bíceps"
            nombre.contains("tricep") -> "Tríceps"

            // Tren inferior
            nombre.contains("squat") || nombre.contains("sentadilla") || nombre.contains("pierna") -> "Piernas"
            nombre.contains("cuadricep") -> "Cuádriceps"
            nombre.contains("femoral") || nombre.contains("isquio") -> "Isquiotibiales"
            nombre.contains("gluteo") -> "Glúteos"
            nombre.contains("pantorrilla") || nombre.contains("gemelo") -> "Pantorrillas"

            // Core
            nombre.contains("abdomen") || nombre.contains("core") || nombre.contains("plancha") -> "Core"

            else -> "General"
        }
    }

    /**
     * Determina el tipo de ejercicio según el nombre
     */
    fun determinarTipoEjercicio(nombreEjercicio: String): String {
        val nombre = nombreEjercicio.lowercase()
        return when {
            nombre.contains("correr") || nombre.contains("caminar") || nombre.contains("cardio") -> "Cardio"
            nombre.contains("press") || nombre.contains("squat") || nombre.contains("peso") -> "Fuerza"
            nombre.contains("plancha") || nombre.contains("burpee") || nombre.contains("funcional") -> "Funcional"
            nombre.contains("estiramiento") || nombre.contains("yoga") || nombre.contains("flexibilidad") -> "Flexibilidad"
            else -> "Fuerza"
        }
    }

    /**
     * Determina el equipamiento necesario según el ejercicio
     */
    fun determinarEquipamiento(nombreEjercicio: String): String {
        val nombre = nombreEjercicio.lowercase()
        return when {
            nombre.contains("mancuerna") -> "Mancuernas"
            nombre.contains("barra") -> "Barra"
            nombre.contains("peso corporal") || nombre.contains("flexiones") || nombre.contains("plancha") -> "Peso corporal"
            nombre.contains("máquina") || nombre.contains("maquina") -> "Máquina"
            nombre.contains("banda") || nombre.contains("elastica") -> "Bandas elásticas"
            nombre.contains("kettlebell") -> "Kettlebells"
            nombre.contains("trx") -> "TRX"
            else -> "Libre"
        }
    }

    // ===== FUNCIONES DE MAPEO =====

    /**
     * Mapea el equipamiento del usuario a texto legible
     */
    fun mapearEquipamientoUsuario(equipoUsuario: String): String {
        return when (equipoUsuario) {
            "GIMNASIO_COMPLETO" -> "Gimnasio completo"
            "MANCUERNAS" -> "Mancuernas"
            "PESAS_LIBRES" -> "Pesas libres"
            "BANDAS_ELASTICAS" -> "Bandas elásticas"
            "SOLO_PESO_CORPORAL" -> "Peso corporal"
            else -> equipoUsuario
        }
    }

    /**
     * Mapea el objetivo a tipo de ejercicio para compatibilidad
     */
    fun mapearObjetivoATipoEjercicio(objetivo: String): String {
        return when (objetivo) {
            "PERDER_GRASA" -> "Cardio"
            "GANAR_MUSCULO" -> "Fuerza"
            "FUERZA" -> "Fuerza"
            "RESISTENCIA" -> "Cardio"
            "TONIFICAR" -> "Mixto"
            "REHABILITACION" -> "Funcional"
            else -> "Mixto"
        }
    }

    /**
     * Determina el nivel de dificultad según la experiencia
     */
    fun determinarNivelDificultad(experiencia: String): String {
        return when (experiencia) {
            "PRINCIPIANTE" -> "PRINCIPIANTE"
            "INTERMEDIO" -> "INTERMEDIO"
            "AVANZADO" -> "AVANZADO"
            else -> "INTERMEDIO"
        }
    }

    /**
     * Determina el lugar de realización basado en equipamiento
     */
    fun determinarLugarRealizacion(equipamientoDisponible: List<String>): String {
        return when {
            equipamientoDisponible.contains("GIMNASIO_COMPLETO") -> "GIMNASIO"
            equipamientoDisponible.contains("SOLO_PESO_CORPORAL") -> "CASA"
            equipamientoDisponible.isEmpty() -> "FLEXIBLE"
            else -> "FLEXIBLE"
        }
    }

    // ===== FUNCIONES DE GENERACIÓN =====

    /**
     * Genera sesiones iniciales basadas en la frecuencia semanal
     */
    fun generarSesionesIniciales(frecuencia: String): List<SesionEntrenamiento> {
        val numeroSesiones = when (frecuencia) {
            "DOS_DIAS" -> 2
            "TRES_DIAS" -> 3
            "CUATRO_DIAS" -> 4
            "CINCO_DIAS" -> 5
            "SEIS_DIAS" -> 6
            else -> 3
        }

        val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val tiposSesionRotacion = listOf("FUERZA", "CARDIO", "MIXTO")

        return (1..numeroSesiones).map { i ->
            SesionEntrenamiento(
                dia = diasSemana.getOrNull(i - 1) ?: "Día $i",
                nombre = when (i) {
                    1 -> "Tren Superior"
                    2 -> "Tren Inferior"
                    3 -> "Cardio"
                    4 -> "Cuerpo Completo"
                    5 -> "Funcional"
                    6 -> "Recuperación Activa"
                    else -> "Sesión $i"
                },
                tipoSesion = when (i) {
                    1, 2, 4 -> "FUERZA"
                    3, 6 -> "CARDIO"
                    5 -> "MIXTO"
                    else -> tiposSesionRotacion[i % tiposSesionRotacion.size]
                },
                duracionMinutos = when (i) {
                    6 -> 30 // Recuperación activa más corta
                    else -> 60
                }
            )
        }
    }

    /**
     * Genera progresión automática basada en objetivo y duración
     */
    fun generarProgresion(objetivo: String, duracionSemanas: String): String {
        val semanas = duracionSemanas.toIntOrNull() ?: 8

        return when (objetivo) {
            "GANAR_MUSCULO" -> buildString {
                appendLine("PROGRESIÓN DE FUERZA Y MASA MUSCULAR:")
                appendLine("• Semanas 1-2: Adaptación anatómica (60-70% 1RM)")
                appendLine("• Semanas 3-${semanas-2}: Hipertrofia (70-85% 1RM)")
                appendLine("• Semana ${semanas-1}: Intensificación (85-90% 1RM)")
                appendLine("• Semana $semanas: Deload (50-60% 1RM)")
                appendLine("Aumentar peso 2.5-5% cuando puedas completar todas las series/reps")
            }

            "FUERZA" -> buildString {
                appendLine("PROGRESIÓN DE FUERZA:")
                appendLine("• Semanas 1-2: Base técnica (70-80% 1RM)")
                appendLine("• Semanas 3-${semanas-2}: Desarrollo fuerza (80-90% 1RM)")
                appendLine("• Semana ${semanas-1}: Pico de fuerza (90-95% 1RM)")
                appendLine("• Semana $semanas: Recuperación (60-70% 1RM)")
                appendLine("Aumentar intensidad 5-10% cada 2 semanas en movimientos principales")
            }

            "PERDER_GRASA" -> buildString {
                appendLine("PROGRESIÓN PARA PÉRDIDA DE GRASA:")
                appendLine("• Mantener intensidad de fuerza constante")
                appendLine("• Aumentar volumen cardio progresivamente:")
                appendLine("  - Semanas 1-2: 150 min/sem intensidad moderada")
                appendLine("  - Semanas 3-4: 200 min/sem intensidad moderada")
                appendLine("  - Semanas 5+: Agregar 2-3 sesiones HIIT/semana")
                appendLine("Priorizar recuperación para mantener masa muscular")
            }

            "RESISTENCIA" -> buildString {
                appendLine("PROGRESIÓN DE RESISTENCIA:")
                appendLine("• Aumentar volumen 10% semanal máximo")
                appendLine("• Alternar semanas de volumen y intensidad")
                appendLine("• Incluir 1 día de recuperación activa por semana")
                appendLine("Monitorear frecuencia cardíaca para evitar sobreentrenamiento")
            }

            else -> "Progresión gradual cada 1-2 semanas según respuesta individual. " +
                    "Aumentar carga cuando técnica sea perfecta y puedas completar todas las repeticiones."
        }
    }

    /**
     * Extrae adaptaciones necesarias basadas en la solicitud
     */
    fun extraerAdaptaciones(solicitud: SolicitudPlan): String {
        val adaptaciones = mutableListOf<String>()

        // Basado en experiencia
        when (solicitud.experienciaPrevia) {
            "PRINCIPIANTE" -> adaptaciones.add("Enfoque en técnica y adaptación progresiva. Comenzar con pesos ligeros.")
            "AVANZADO" -> adaptaciones.add("Periodización avanzada. Considerar técnicas de intensidad.")
        }

        // Basado en descripción (búsqueda de palabras clave)
        val descripcion = solicitud.descripcion.lowercase()
        if (descripcion.contains("lesión") || descripcion.contains("dolor")) {
            adaptaciones.add("⚠️ REVISAR: Posibles lesiones mencionadas en descripción")
        }

        if (descripcion.contains("espalda")) {
            adaptaciones.add("Considerar ejercicios de fortalecimiento de core y espalda baja")
        }

        if (descripcion.contains("rodilla")) {
            adaptaciones.add("Evitar impacto excesivo en rodillas, preferir ejercicios de cadena cerrada")
        }

        if (descripcion.contains("tiempo") || descripcion.contains("poco tiempo")) {
            adaptaciones.add("Priorizar ejercicios compuestos para maximizar eficiencia")
        }

        return if (adaptaciones.isEmpty()) {
            "Sin adaptaciones especiales requeridas"
        } else {
            adaptaciones.joinToString(". ")
        }
    }

    // ===== FUNCIONES DE CÁLCULO =====

    /**
     * Calcula la duración promedio de las sesiones
     */
    fun calcularDuracionPromedio(sesiones: List<SesionEntrenamiento>): Int {
        return if (sesiones.isEmpty()) 60 else sesiones.map { it.duracionMinutos }.average().toInt()
    }

    /**
     * Extrae todos los materiales necesarios de las sesiones
     */
    fun extraerMaterialesDeEjercicios(sesiones: List<SesionEntrenamiento>): String {
        val materiales = mutableSetOf<String>()

        sesiones.forEach { sesion ->
            when (sesion.tipoSesion) {
                "FUERZA" -> materiales.addAll(listOf("Pesas", "Barras", "Mancuernas"))
                "CARDIO" -> materiales.addAll(listOf("Cardio", "Espacio libre"))
                "MIXTO" -> materiales.addAll(listOf("Pesas", "Cardio", "Funcional"))
                "FUNCIONAL" -> materiales.addAll(listOf("Peso corporal", "Bandas", "Accesorios"))
            }

            // Agregar equipamiento específico de ejercicios
            sesion.ejercicios.forEach { ejercicio ->
                val equipo = determinarEquipamiento(ejercicio.nombre)
                if (equipo != "Libre") {
                    materiales.add(equipo)
                }
            }
        }

        return materiales.joinToString(", ")
    }

    /**
     * Extrae equipamiento necesario basado en sesiones y solicitud
     */
    fun extraerEquipamientoNecesario(sesiones: List<SesionEntrenamiento>, solicitud: SolicitudPlan): List<String> {
        val equipamiento = mutableSetOf<String>()

        // Extraer de ejercicios
        sesiones.flatMap { it.ejercicios }.forEach { ejercicio ->
            equipamiento.add(determinarEquipamiento(ejercicio.nombre))
        }

        // Agregar equipamiento disponible del usuario
        solicitud.equipamientoDisponible.forEach { equipo ->
            equipamiento.add(mapearEquipamientoUsuario(equipo))
        }

        return equipamiento.toList()
    }

    // ===== FUNCIONES DE CONVERSIÓN =====

    /**
     * Convierte frecuencia enum a texto legible
     */
    fun getFrecuenciaTexto(frecuencia: String): String {
        return when (frecuencia) {
            "DOS_DIAS" -> "2 días por semana"
            "TRES_DIAS" -> "3 días por semana"
            "CUATRO_DIAS" -> "4 días por semana"
            "CINCO_DIAS" -> "5 días por semana"
            "SEIS_DIAS" -> "6 días por semana"
            else -> "3 días por semana"
        }
    }

    /**
     * Determina objetivo de sesión según tipo
     */
    fun determinarObjetivoSesion(tipoSesion: String): String {
        return when (tipoSesion) {
            "FUERZA" -> "Desarrollo de fuerza y masa muscular"
            "CARDIO" -> "Mejora cardiovascular y quema de calorías"
            "MIXTO" -> "Entrenamiento integral fuerza y cardio"
            "FUNCIONAL" -> "Mejora de movimientos funcionales"
            "ESTIRAMIENTO" -> "Flexibilidad y recuperación"
            else -> "Entrenamiento completo"
        }
    }

    /**
     * Extrae grupos musculares de una lista de ejercicios
     */
    fun extraerGruposMusculares(ejercicios: List<EjercicioSimple>): List<String> {
        return ejercicios.map { determinarMusculoTrabajado(it.nombre) }.distinct()
    }
}