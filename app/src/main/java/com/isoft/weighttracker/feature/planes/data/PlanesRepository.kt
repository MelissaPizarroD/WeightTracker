package com.isoft.weighttracker.feature.planes.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.planes.model.*
import kotlinx.coroutines.tasks.await

class PlanesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "PlanesRepository"
    }

    // ===== SOLICITUDES DE PLANES (POR USUARIO) =====

    suspend fun enviarSolicitudPlan(
        profesionalId: String,
        tipoPlan: TipoPlan,
        descripcion: String,
        nombreUsuario: String,
        emailUsuario: String,
        // PARÁMETROS DE NUTRICIÓN
        objetivoNutricion: String = "",
        nivelActividad: String = "",
        restricciones: List<String> = emptyList(),
        restriccionesOtras: String = "",
        restriccionesMedicas: String = "",
        // PARÁMETROS DE ENTRENAMIENTO
        objetivoEntrenamiento: String = "",
        experienciaPrevia: String = "",
        disponibilidadSemanal: String = "",
        equipamientoDisponible: List<String> = emptyList()
    ): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false

            val solicitud = SolicitudPlan(
                usuarioId = userId,
                profesionalId = profesionalId,
                tipoPlan = tipoPlan,
                descripcion = descripcion,
                nombreUsuario = nombreUsuario,
                emailUsuario = emailUsuario,
                // CAMPOS ESPECÍFICOS
                objetivoNutricion = objetivoNutricion,
                nivelActividad = nivelActividad,
                restricciones = restricciones,
                restriccionesOtras = restriccionesOtras,
                restriccionesMedicas = restriccionesMedicas,
                objetivoEntrenamiento = objetivoEntrenamiento,
                experienciaPrevia = experienciaPrevia,
                disponibilidadSemanal = disponibilidadSemanal,
                equipamientoDisponible = equipamientoDisponible
            )

            Log.d(TAG, "📝 Enviando solicitud de ${tipoPlan.name} a $profesionalId")
            Log.d(TAG, "👤 Usuario: $nombreUsuario ($emailUsuario)")

            // ✅ Guardar en subcoleción del usuario
            db.collection("users").document(userId)
                .collection("solicitudesPlan")
                .document(solicitud.id)
                .set(solicitud)
                .await()

            Log.d(TAG, "✅ Solicitud enviada: ${solicitud.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando solicitud", e)
            false
        }
    }

    suspend fun obtenerSolicitudesPendientesUsuario(): List<SolicitudPlan> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            // ✅ Obtener de subcoleción del usuario
            val snapshot = db.collection("users").document(userId)
                .collection("solicitudesPlan")
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(SolicitudPlan::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitudes del usuario", e)
            emptyList()
        }
    }

    suspend fun obtenerSolicitudesPendientesProfesional(): List<SolicitudPlan> {
        return try {
            val profesionalId = auth.currentUser?.uid ?: return emptyList()

            Log.d(TAG, "🔍 Buscando solicitudes para profesional: $profesionalId")

            // ✅ Buscar en todas las colecciones de usuarios
            val usuarios = db.collection("users").get().await()
            val solicitudes = mutableListOf<SolicitudPlan>()

            for (usuarioDoc in usuarios.documents) {
                try {
                    val solicitudesSnapshot = usuarioDoc.reference
                        .collection("solicitudesPlan")
                        .whereEqualTo("profesionalId", profesionalId)
                        .get()
                        .await()

                    val solicitudesUsuario = solicitudesSnapshot.documents.mapNotNull {
                        it.toObject(SolicitudPlan::class.java)
                    }

                    solicitudes.addAll(solicitudesUsuario)
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error procesando usuario ${usuarioDoc.id}: ${e.message}")
                }
            }

            Log.d(TAG, "📋 Encontradas ${solicitudes.size} solicitudes totales")
            solicitudes.sortedByDescending { it.fechaSolicitud }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitudes del profesional", e)
            emptyList()
        }
    }

    suspend fun marcarSolicitudComoCompletada(solicitudId: String, planId: String): Boolean {
        return try {
            // Buscar la solicitud en todos los usuarios
            val usuarios = db.collection("users").get().await()

            for (usuarioDoc in usuarios.documents) {
                try {
                    val solicitudDoc = usuarioDoc.reference
                        .collection("solicitudesPlan")
                        .document(solicitudId)

                    val solicitudSnapshot = solicitudDoc.get().await()
                    if (solicitudSnapshot.exists()) {
                        solicitudDoc.update(
                            mapOf(
                                "estado" to EstadoSolicitud.COMPLETADA,
                                "planCreado" to planId,
                                "fechaCompletada" to System.currentTimeMillis()
                            )
                        ).await()

                        Log.d(TAG, "✅ Solicitud $solicitudId marcada como completada")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error buscando en usuario ${usuarioDoc.id}: ${e.message}")
                }
            }

            Log.w(TAG, "⚠️ No se encontró la solicitud $solicitudId")
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marcando solicitud como completada", e)
            false
        }
    }

    /**
     * Rechaza una solicitud específica con un motivo
     */
    suspend fun rechazarSolicitud(solicitudId: String, motivoRechazo: String): Boolean {
        return try {
            Log.d(TAG, "🚫 Rechazando solicitud: $solicitudId")
            Log.d(TAG, "📝 Motivo: $motivoRechazo")

            // Buscar la solicitud en todos los usuarios
            val usuarios = db.collection("users").get().await()

            for (usuarioDoc in usuarios.documents) {
                try {
                    val solicitudDoc = usuarioDoc.reference
                        .collection("solicitudesPlan")
                        .document(solicitudId)

                    val solicitudSnapshot = solicitudDoc.get().await()
                    if (solicitudSnapshot.exists()) {
                        // Actualizar el estado y agregar el motivo de rechazo
                        solicitudDoc.update(
                            mapOf(
                                "estado" to EstadoSolicitud.RECHAZADA,
                                "motivoRechazo" to motivoRechazo,
                                "fechaRechazo" to System.currentTimeMillis()
                            )
                        ).await()

                        Log.d(TAG, "✅ Solicitud rechazada exitosamente")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error procesando usuario ${usuarioDoc.id}: ${e.message}")
                }
            }

            Log.w(TAG, "⚠️ No se encontró la solicitud: $solicitudId")
            false

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error rechazando solicitud", e)
            false
        }
    }

    /**
     * Obtiene una solicitud específica por ID
     */
    suspend fun obtenerSolicitudPorId(solicitudId: String): SolicitudPlan? {
        return try {
            Log.d(TAG, "🔍 Buscando solicitud: $solicitudId")

            // Buscar la solicitud en todos los usuarios
            val usuarios = db.collection("users").get().await()

            for (usuarioDoc in usuarios.documents) {
                try {
                    val solicitudDoc = usuarioDoc.reference
                        .collection("solicitudesPlan")
                        .document(solicitudId)
                        .get()
                        .await()

                    if (solicitudDoc.exists()) {
                        val solicitud = solicitudDoc.toObject(SolicitudPlan::class.java)
                        Log.d(TAG, "✅ Solicitud encontrada: ${solicitud?.nombreUsuario}")
                        return solicitud
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error procesando usuario ${usuarioDoc.id}: ${e.message}")
                }
            }

            Log.w(TAG, "⚠️ No se encontró la solicitud: $solicitudId")
            null

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitud por ID", e)
            null
        }
    }

    // ===== PLANES NUTRICIONALES =====
    //---NO DISPONIBLES---//

    // ===== PLANES DE ENTRENAMIENTO =====

    suspend fun crearPlanEntrenamiento(plan: PlanEntrenamiento): String? {
        return try {
            val planRef = db.collection("users")
                .document(plan.usuarioId)
                .collection("planesEntrenamiento")
                .document()

            val planConId = plan.copy(id = planRef.id)
            planRef.set(planConId).await()

            Log.d(TAG, "✅ Plan de entrenamiento creado: ${planRef.id}")
            planRef.id
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando plan de entrenamiento", e)
            null
        }
    }

    suspend fun obtenerPlanesEntrenamientoUsuario(): List<PlanEntrenamiento> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            val snapshot = db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(PlanEntrenamiento::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo planes de entrenamiento", e)
            emptyList()
        }
    }

    suspend fun activarPlanEntrenamiento(planId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false

            // Primero desactivar todos los planes
            val planesSnapshot = db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .get()
                .await()

            val batch = db.batch()

            for (doc in planesSnapshot.documents) {
                batch.update(doc.reference, "estado", EstadoPlan.INACTIVO)
            }

            // Activar el plan seleccionado
            val planRef = db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .document(planId)

            batch.update(
                planRef,
                mapOf(
                    "estado" to EstadoPlan.ACTIVO,
                    "fechaActivacion" to System.currentTimeMillis(),
                    "fechaDesactivacion" to null
                )
            )

            batch.commit().await()
            Log.d(TAG, "✅ Plan de entrenamiento $planId activado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error activando plan de entrenamiento", e)
            false
        }
    }

    suspend fun desactivarPlanEntrenamiento(planId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false

            db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .document(planId)
                .update(
                    mapOf(
                        "estado" to EstadoPlan.INACTIVO,
                        "fechaDesactivacion" to System.currentTimeMillis()
                    )
                )
                .await()

            Log.d(TAG, "✅ Plan de entrenamiento $planId desactivado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando plan de entrenamiento", e)
            false
        }
    }

    suspend fun obtenerPlanEntrenamientoPorId(planId: String): PlanEntrenamiento? {
        return try {
            val userId = auth.currentUser?.uid ?: return null

            val doc = db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .document(planId)
                .get()
                .await()

            doc.toObject(PlanEntrenamiento::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo plan de entrenamiento por ID", e)
            null
        }
    }

    suspend fun obtenerPlanesEntrenamientoPorUsuario(userId: String): List<PlanEntrenamiento> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("planesEntrenamiento")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(PlanEntrenamiento::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo planes de entrenamiento del usuario $userId", e)
            emptyList()
        }
    }
}