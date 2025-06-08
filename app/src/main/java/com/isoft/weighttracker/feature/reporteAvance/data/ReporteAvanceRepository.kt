package com.isoft.weighttracker.feature.reporteAvance.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.reporteAvance.model.ReporteAvance
import com.isoft.weighttracker.feature.reporteAvance.model.Retroalimentacion
import kotlinx.coroutines.tasks.await

class ReportesAvanceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ReportesRepository"

    private fun userCollection() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("reportes_avance")

    suspend fun guardarReporte(reporte: ReporteAvance): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val collectionRef = userCollection()
            val docRef = if (reporte.id.isNotEmpty()) {
                collectionRef.document(reporte.id)
            } else {
                collectionRef.document()
            }

            // Asegurarse de que fechaCreacion esté establecida
            val reporteConId = reporte.copy(
                id = docRef.id,
                fechaCreacion = if (reporte.fechaCreacion == 0L) System.currentTimeMillis() else reporte.fechaCreacion
            )

            Log.d(TAG, "Guardando reporte: ${reporteConId.id} - ${reporteConId.fechaCreacion}")
            docRef.set(reporteConId).await()
            Log.d(TAG, "Reporte guardado exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar reporte", e)
            false
        }
    }

    suspend fun obtenerHistorial(): List<ReporteAvance> {
        return try {
            Log.d(TAG, "Obteniendo historial de reportes...")
            val snapshot = userCollection()
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val reportes = snapshot.documents.mapNotNull { document ->
                try {
                    val reporte = document.toObject(ReporteAvance::class.java)
                    Log.d(TAG, "Reporte encontrado: ${reporte?.id} - ${reporte?.fechaCreacion}")
                    reporte
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir documento a ReporteAvance", e)
                    null
                }
            }

            Log.d(TAG, "Total de reportes obtenidos: ${reportes.size}")
            reportes
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener historial", e)
            emptyList()
        }
    }

    suspend fun obtenerReporte(id: String): ReporteAvance? {
        return try {
            Log.d(TAG, "Obteniendo reporte con ID: $id")
            val doc = userCollection().document(id).get().await()
            val reporte = doc.toObject(ReporteAvance::class.java)
            Log.d(TAG, "Reporte obtenido: ${reporte?.id}")
            reporte
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reporte", e)
            null
        }
    }

    suspend fun agregarRetroalimentacion(idReporte: String, retro: Retroalimentacion): Boolean {
        return try {
            val reporteActual = obtenerReporte(idReporte) ?: return false
            val nuevasRetro = reporteActual.retroalimentaciones + retro
            val reporteActualizado = reporteActual.copy(retroalimentaciones = nuevasRetro)

            userCollection().document(idReporte).set(reporteActualizado).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar retroalimentación", e)
            false
        }
    }

    suspend fun obtenerReportesPorTipo(tipoReporte: com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte): List<ReporteAvance> {
        return try {
            val snapshot = userCollection()
                .whereEqualTo("tipoReporte", tipoReporte.name)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(ReporteAvance::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reportes por tipo", e)
            emptyList()
        }
    }
}