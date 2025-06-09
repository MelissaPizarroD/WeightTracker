package com.isoft.weighttracker.feature.reporteAvance.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
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

            Log.d(TAG, "💾 Guardando reporte: ${reporteConId.id} - ${reporteConId.fechaCreacion}")
            docRef.set(reporteConId).await()
            Log.d(TAG, "✅ Reporte guardado exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar reporte", e)
            false
        }
    }

    // ✅ NUEVA FUNCIÓN: Obtener historial con forzar desde servidor
    suspend fun obtenerHistorial(forzarDesdeServidor: Boolean = false): List<ReporteAvance> {
        return try {
            Log.d(TAG, "=== OBTENIENDO HISTORIAL ===")
            Log.d(TAG, "Forzar desde servidor: $forzarDesdeServidor")

            val uid = auth.currentUser?.uid ?: ""
            Log.d(TAG, "Usuario ID: $uid")

            // ✅ IMPORTANTE: Especificar la fuente de datos
            val source = if (forzarDesdeServidor) Source.SERVER else Source.DEFAULT

            val snapshot = userCollection()
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get(source)
                .await()

            Log.d(TAG, "📊 Documentos encontrados: ${snapshot.documents.size}")
            Log.d(TAG, "📊 Metadata - isFromCache: ${snapshot.metadata.isFromCache}")

            val reportes = snapshot.documents.mapNotNull { document ->
                try {
                    val reporte = document.toObject(ReporteAvance::class.java)
                    if (reporte != null) {
                        Log.d(TAG, "📄 Reporte: ${reporte.id}")
                        Log.d(TAG, "   - Fecha: ${reporte.fechaCreacion}")
                        Log.d(TAG, "   - Tipo: ${reporte.tipoReporte}")
                        Log.d(TAG, "   - Retroalimentaciones: ${reporte.retroalimentaciones.size}")

                        // ✅ NUEVO: Log detallado de retroalimentaciones
                        reporte.retroalimentaciones.forEach { retro ->
                            Log.d(TAG, "     💬 Retro: ${retro.contenido.take(30)}... (${retro.fecha})")
                        }
                    } else {
                        Log.w(TAG, "❌ Documento ${document.id} no se pudo convertir a ReporteAvance")
                    }
                    reporte
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al convertir documento ${document.id} a ReporteAvance", e)
                    null
                }
            }

            Log.d(TAG, "=== TOTAL REPORTES HISTORIAL: ${reportes.size} ===")

            // ✅ NUEVO: Log resumen de retroalimentaciones
            val reportesConRetro = reportes.count { it.retroalimentaciones.isNotEmpty() }
            Log.d(TAG, "📊 Reportes con retroalimentaciones: $reportesConRetro")

            reportes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener historial", e)
            emptyList()
        }
    }

    // ✅ MANTENER compatibilidad con versión original
    suspend fun obtenerHistorial(): List<ReporteAvance> {
        return obtenerHistorial(forzarDesdeServidor = false)
    }

    suspend fun obtenerReporte(id: String): ReporteAvance? {
        return try {
            Log.d(TAG, "=== OBTENIENDO REPORTE INDIVIDUAL ===")
            Log.d(TAG, "Reporte ID: $id")

            val doc = userCollection().document(id).get().await()
            Log.d(TAG, "📊 Documento existe: ${doc.exists()}")
            Log.d(TAG, "📊 Metadata - isFromCache: ${doc.metadata.isFromCache}")

            val reporte = doc.toObject(ReporteAvance::class.java)
            if (reporte != null) {
                Log.d(TAG, "✅ Reporte obtenido: ${reporte.id}")
                Log.d(TAG, "   - Retroalimentaciones: ${reporte.retroalimentaciones.size}")
                reporte.retroalimentaciones.forEach { retro ->
                    Log.d(TAG, "     💬 ${retro.contenido.take(50)}...")
                }
            } else {
                Log.w(TAG, "❌ No se pudo convertir documento a ReporteAvance")
            }

            reporte
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener reporte", e)
            null
        }
    }

    suspend fun agregarRetroalimentacion(idReporte: String, retro: Retroalimentacion): Boolean {
        return try {
            Log.d(TAG, "=== AGREGANDO RETROALIMENTACIÓN (USUARIO ACTUAL) ===")
            Log.d(TAG, "Reporte ID: $idReporte")
            Log.d(TAG, "Contenido: ${retro.contenido.take(50)}...")

            val reporteActual = obtenerReporte(idReporte) ?: return false
            Log.d(TAG, "📄 Reporte encontrado con ${reporteActual.retroalimentaciones.size} retroalimentaciones existentes")

            val nuevasRetro = reporteActual.retroalimentaciones + retro
            val reporteActualizado = reporteActual.copy(retroalimentaciones = nuevasRetro)

            Log.d(TAG, "💾 Guardando reporte con ${nuevasRetro.size} retroalimentaciones...")

            userCollection().document(idReporte).set(reporteActualizado).await()
            Log.d(TAG, "✅ Retroalimentación agregada exitosamente")

            // ✅ VERIFICACIÓN: Confirmar que se guardó correctamente
            val verificacion = obtenerReporte(idReporte)
            Log.d(TAG, "🔍 Verificación: ${verificacion?.retroalimentaciones?.size} retroalimentaciones en BD")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al agregar retroalimentación", e)
            false
        }
    }

    suspend fun obtenerReporteDeUsuario(reporteId: String, usuarioId: String): ReporteAvance? {
        return try {
            Log.d(TAG, "=== OBTENIENDO REPORTE DE USUARIO ===")
            Log.d(TAG, "Reporte ID: $reporteId")
            Log.d(TAG, "Usuario ID: $usuarioId")

            val doc = db.collection("users")
                .document(usuarioId)
                .collection("reportes_avance")
                .document(reporteId)
                .get()
                .await()

            Log.d(TAG, "📊 Documento existe: ${doc.exists()}")

            val reporte = doc.toObject(ReporteAvance::class.java)
            if (reporte != null) {
                Log.d(TAG, "✅ Reporte del usuario obtenido: ${reporte.id}")
                Log.d(TAG, "   - Retroalimentaciones: ${reporte.retroalimentaciones.size}")
            } else {
                Log.w(TAG, "❌ No se pudo obtener reporte del usuario")
            }
            reporte
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener reporte del usuario", e)
            null
        }
    }

    suspend fun agregarRetroalimentacionAUsuario(reporteId: String, usuarioId: String, retro: Retroalimentacion): Boolean {
        return try {
            Log.d(TAG, "=== AGREGANDO RETROALIMENTACIÓN A USUARIO ===")
            Log.d(TAG, "Reporte ID: $reporteId")
            Log.d(TAG, "Usuario ID: $usuarioId")
            Log.d(TAG, "Contenido: ${retro.contenido.take(50)}...")

            val reporteActual = obtenerReporteDeUsuario(reporteId, usuarioId)
            if (reporteActual == null) {
                Log.e(TAG, "❌ No se encontró el reporte para actualizar")
                return false
            }

            Log.d(TAG, "📄 Reporte encontrado con ${reporteActual.retroalimentaciones.size} retroalimentaciones existentes")

            val nuevasRetro = reporteActual.retroalimentaciones + retro
            val reporteActualizado = reporteActual.copy(retroalimentaciones = nuevasRetro)

            Log.d(TAG, "💾 Guardando reporte con ${nuevasRetro.size} retroalimentaciones...")

            db.collection("users")
                .document(usuarioId)
                .collection("reportes_avance")
                .document(reporteId)
                .set(reporteActualizado)
                .await()

            Log.d(TAG, "✅ Retroalimentación agregada exitosamente")

            // ✅ VERIFICACIÓN: Confirmar que se guardó correctamente
            val verificacion = obtenerReporteDeUsuario(reporteId, usuarioId)
            Log.d(TAG, "🔍 Verificación: ${verificacion?.retroalimentaciones?.size} retroalimentaciones en BD")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al agregar retroalimentación al usuario", e)
            false
        }
    }

    suspend fun obtenerReportesDeUsuario(usuarioId: String): List<ReporteAvance> {
        return try {
            Log.d(TAG, "=== OBTENIENDO REPORTES DE USUARIO ===")
            Log.d(TAG, "Usuario ID: $usuarioId")

            val snapshot = db.collection("users")
                .document(usuarioId)
                .collection("reportes_avance")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(TAG, "📊 Documentos encontrados para usuario: ${snapshot.documents.size}")
            Log.d(TAG, "📊 Metadata - isFromCache: ${snapshot.metadata.isFromCache}")

            val reportes = snapshot.documents.mapNotNull { document ->
                try {
                    val reporte = document.toObject(ReporteAvance::class.java)
                    if (reporte != null) {
                        Log.d(TAG, "📄 Reporte usuario: ${reporte.id}")
                        Log.d(TAG, "   - Fecha: ${reporte.fechaCreacion}")
                        Log.d(TAG, "   - Retroalimentaciones: ${reporte.retroalimentaciones.size}")
                        reporte.retroalimentaciones.forEach { retro ->
                            Log.d(TAG, "     💬 Retro: ${retro.contenido.take(30)}...")
                        }
                    }
                    reporte
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al convertir documento a ReporteAvance", e)
                    null
                }
            }

            Log.d(TAG, "=== TOTAL REPORTES USUARIO: ${reportes.size} ===")
            val reportesConRetro = reportes.count { it.retroalimentaciones.isNotEmpty() }
            Log.d(TAG, "📊 Reportes con retroalimentaciones: $reportesConRetro")

            reportes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener reportes del usuario", e)
            emptyList()
        }
    }

    suspend fun obtenerReportesPorTipo(tipoReporte: com.isoft.weighttracker.feature.reporteAvance.model.TipoReporte): List<ReporteAvance> {
        return try {
            Log.d(TAG, "=== OBTENIENDO REPORTES POR TIPO ===")
            Log.d(TAG, "Tipo: ${tipoReporte.name}")

            val snapshot = userCollection()
                .whereEqualTo("tipoReporte", tipoReporte.name)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(TAG, "📊 Documentos encontrados: ${snapshot.documents.size}")

            val reportes = snapshot.documents.mapNotNull {
                try {
                    it.toObject(ReporteAvance::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al convertir documento", e)
                    null
                }
            }

            Log.d(TAG, "=== REPORTES POR TIPO: ${reportes.size} ===")
            reportes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener reportes por tipo", e)
            emptyList()
        }
    }

    // ✅ NUEVA FUNCIÓN: Diagnóstico completo
    suspend fun diagnosticoCompleto(): String {
        val uid = auth.currentUser?.uid ?: "NO_USER"
        val diagnostico = StringBuilder()

        diagnostico.appendLine("=== DIAGNÓSTICO COMPLETO ===")
        diagnostico.appendLine("Usuario ID: $uid")
        diagnostico.appendLine("Timestamp: ${System.currentTimeMillis()}")

        try {
            // Obtener todos los reportes directamente
            val snapshot = userCollection()
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get(Source.SERVER) // Forzar desde servidor
                .await()

            diagnostico.appendLine("Total documentos: ${snapshot.documents.size}")
            diagnostico.appendLine("Desde cache: ${snapshot.metadata.isFromCache}")

            snapshot.documents.forEachIndexed { index, doc ->
                diagnostico.appendLine("\n--- Documento $index ---")
                diagnostico.appendLine("ID: ${doc.id}")
                diagnostico.appendLine("Existe: ${doc.exists()}")

                try {
                    val reporte = doc.toObject(ReporteAvance::class.java)
                    if (reporte != null) {
                        diagnostico.appendLine("Tipo: ${reporte.tipoReporte}")
                        diagnostico.appendLine("Fecha creación: ${reporte.fechaCreacion}")
                        diagnostico.appendLine("Retroalimentaciones: ${reporte.retroalimentaciones.size}")

                        reporte.retroalimentaciones.forEach { retro ->
                            diagnostico.appendLine("  - Retro fecha: ${retro.fecha}")
                            diagnostico.appendLine("  - Retro contenido: ${retro.contenido.take(100)}")
                        }
                    } else {
                        diagnostico.appendLine("ERROR: No se pudo convertir a ReporteAvance")
                    }
                } catch (e: Exception) {
                    diagnostico.appendLine("ERROR conversión: ${e.message}")
                }
            }

        } catch (e: Exception) {
            diagnostico.appendLine("ERROR obtención: ${e.message}")
        }

        val resultado = diagnostico.toString()
        Log.d(TAG, resultado)
        return resultado
    }
}