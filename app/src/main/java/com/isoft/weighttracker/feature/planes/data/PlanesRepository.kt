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
        private const val COLLECTION_SOLICITUDES = "solicitudesPlan"
        private const val COLLECTION_PLANES_NUTRICION = "planesNutricion"
        private const val COLLECTION_PLANES_ENTRENAMIENTO = "planesEntrenamiento"
    }

    // ===== SOLICITUDES DE PLANES =====

    suspend fun enviarSolicitudPlan(
        profesionalId: String,
        tipoPlan: TipoPlan,
        descripcion: String,
        nombreUsuario: String,
        emailUsuario: String
    ): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false

            val solicitud = SolicitudPlan(
                usuarioId = userId,
                profesionalId = profesionalId,
                tipoPlan = tipoPlan,
                descripcion = descripcion,
                nombreUsuario = nombreUsuario,
                emailUsuario = emailUsuario
            )

            Log.d(TAG, "📝 Enviando solicitud de ${tipoPlan.name} a $profesionalId")

            db.collection(COLLECTION_SOLICITUDES)
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

            val snapshot = db.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("usuarioId", userId)
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

            val snapshot = db.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("profesionalId", profesionalId)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            val solicitudes = snapshot.documents.mapNotNull {
                it.toObject(SolicitudPlan::class.java)
            }

            Log.d(TAG, "📋 Solicitudes pendientes para profesional: ${solicitudes.size}")
            solicitudes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitudes del profesional", e)
            emptyList()
        }
    }

    suspend fun marcarSolicitudComoCompletada(solicitudId: String, planCreadoId: String): Boolean {
        return try {
            db.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .update(mapOf(
                    "estado" to EstadoSolicitud.COMPLETADA.name,
                    "planCreado" to planCreadoId,
                    "fechaCompletada" to System.currentTimeMillis()
                ))
                .await()

            Log.d(TAG, "✅ Solicitud marcada como completada: $solicitudId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marcando solicitud como completada", e)
            false
        }
    }

    // ===== PLANES NUTRICIONALES =====

    suspend fun crearPlanNutricional(plan: PlanNutricional): String? {
        return try {
            Log.d(TAG, "🥗 Creando plan nutricional para ${plan.usuarioId}")

            db.collection(COLLECTION_PLANES_NUTRICION)
                .document(plan.id)
                .set(plan)
                .await()

            Log.d(TAG, "✅ Plan nutricional creado: ${plan.id}")
            plan.id
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando plan nutricional", e)
            null
        }
    }

    suspend fun obtenerPlanesNutricionUsuario(): List<PlanNutricional> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            val snapshot = db.collection(COLLECTION_PLANES_NUTRICION)
                .whereEqualTo("usuarioId", userId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(PlanNutricional::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo planes nutricionales del usuario", e)
            emptyList()
        }
    }

    suspend fun activarPlanNutricional(planId: String): Boolean {
        return try {
            // Primero desactivar todos los planes activos del usuario
            val userId = auth.currentUser?.uid ?: return false
            desactivarTodosLosPlanesNutricionales(userId)

            // Activar el plan seleccionado
            db.collection(COLLECTION_PLANES_NUTRICION)
                .document(planId)
                .update(mapOf(
                    "estado" to EstadoPlan.ACTIVO.name,
                    "fechaActivacion" to System.currentTimeMillis()
                ))
                .await()

            Log.d(TAG, "✅ Plan nutricional activado: $planId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error activando plan nutricional", e)
            false
        }
    }

    suspend fun desactivarPlanNutricional(planId: String): Boolean {
        return try {
            db.collection(COLLECTION_PLANES_NUTRICION)
                .document(planId)
                .update(mapOf(
                    "estado" to EstadoPlan.INACTIVO.name,
                    "fechaDesactivacion" to System.currentTimeMillis()
                ))
                .await()

            Log.d(TAG, "✅ Plan nutricional desactivado: $planId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando plan nutricional", e)
            false
        }
    }

    private suspend fun desactivarTodosLosPlanesNutricionales(userId: String) {
        try {
            val snapshot = db.collection(COLLECTION_PLANES_NUTRICION)
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("estado", EstadoPlan.ACTIVO.name)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.update(mapOf(
                    "estado" to EstadoPlan.INACTIVO.name,
                    "fechaDesactivacion" to System.currentTimeMillis()
                ))
            }

            Log.d(TAG, "✅ Desactivados ${snapshot.documents.size} planes nutricionales")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando planes nutricionales", e)
        }
    }

    // ===== PLANES DE ENTRENAMIENTO =====

    suspend fun crearPlanEntrenamiento(plan: PlanEntrenamiento): String? {
        return try {
            Log.d(TAG, "💪 Creando plan de entrenamiento para ${plan.usuarioId}")

            db.collection(COLLECTION_PLANES_ENTRENAMIENTO)
                .document(plan.id)
                .set(plan)
                .await()

            Log.d(TAG, "✅ Plan de entrenamiento creado: ${plan.id}")
            plan.id
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando plan de entrenamiento", e)
            null
        }
    }

    suspend fun obtenerPlanesEntrenamientoUsuario(): List<PlanEntrenamiento> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            val snapshot = db.collection(COLLECTION_PLANES_ENTRENAMIENTO)
                .whereEqualTo("usuarioId", userId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(PlanEntrenamiento::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo planes de entrenamiento del usuario", e)
            emptyList()
        }
    }

    suspend fun activarPlanEntrenamiento(planId: String): Boolean {
        return try {
            // Primero desactivar todos los planes activos del usuario
            val userId = auth.currentUser?.uid ?: return false
            desactivarTodosLosPlanesEntrenamiento(userId)

            // Activar el plan seleccionado
            db.collection(COLLECTION_PLANES_ENTRENAMIENTO)
                .document(planId)
                .update(mapOf(
                    "estado" to EstadoPlan.ACTIVO.name,
                    "fechaActivacion" to System.currentTimeMillis()
                ))
                .await()

            Log.d(TAG, "✅ Plan de entrenamiento activado: $planId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error activando plan de entrenamiento", e)
            false
        }
    }

    suspend fun desactivarPlanEntrenamiento(planId: String): Boolean {
        return try {
            db.collection(COLLECTION_PLANES_ENTRENAMIENTO)
                .document(planId)
                .update(mapOf(
                    "estado" to EstadoPlan.INACTIVO.name,
                    "fechaDesactivacion" to System.currentTimeMillis()
                ))
                .await()

            Log.d(TAG, "✅ Plan de entrenamiento desactivado: $planId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando plan de entrenamiento", e)
            false
        }
    }

    private suspend fun desactivarTodosLosPlanesEntrenamiento(userId: String) {
        try {
            val snapshot = db.collection(COLLECTION_PLANES_ENTRENAMIENTO)
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("estado", EstadoPlan.ACTIVO.name)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.update(mapOf(
                    "estado" to EstadoPlan.INACTIVO.name,
                    "fechaDesactivacion" to System.currentTimeMillis()
                ))
            }

            Log.d(TAG, "✅ Desactivados ${snapshot.documents.size} planes de entrenamiento")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando planes de entrenamiento", e)
        }
    }
}