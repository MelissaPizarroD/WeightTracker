package com.isoft.weighttracker.feature.metas.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.metas.model.Meta
import kotlinx.coroutines.tasks.await

class MetasRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getMetasCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("metas")

    suspend fun obtenerMetas(): List<Meta> {
        return try {
            val querySnapshot = getMetasCollection()
                .orderBy("fechaInicio", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Meta::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun guardarMeta(meta: Meta): Boolean {
        return try {
            getMetasCollection().add(meta).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Nuevo metodo que devuelve la meta con el ID asignado
    suspend fun guardarMetaConId(meta: Meta): Meta? {
        return try {
            val documentReference = getMetasCollection().add(meta).await()
            val id = documentReference.id

            // 👉 Nueva meta con el id incluido
            val metaConId = meta.copy(id = id)

            // ✅ Actualizamos el documento con su propio id
            documentReference.set(metaConId).await()

            metaConId
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarMeta(meta: Meta): Boolean {
        return try {
            val id = meta.id ?: return false
            getMetasCollection()
                .document(id)
                .set(meta.copy(id = null)) // No guardamos el ID en el documento
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarMeta(id: String): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun marcarMetaComoCumplida(id: String): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update(
                    mapOf(
                        "cumplida" to true,
                        "activa" to false
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun marcarMetaComoVencida(id: String): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update(
                    mapOf(
                        "activa" to false,
                        "vencida" to true
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun detenerMeta(id: String): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update("activa", false)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun reactivarMeta(id: String): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update("activa", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun extenderFechaLimite(id: String, nuevaFecha: Long): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update("fechaLimite", nuevaFecha)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarFechaLimite(id: String, nuevaFecha: Long): Boolean {
        return try {
            getMetasCollection()
                .document(id)
                .update("fechaLimite", nuevaFecha)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerMetaActiva(): Meta? {
        return try {
            val querySnapshot = getMetasCollection()
                .whereEqualTo("cumplida", false)
                .whereEqualTo("activa", true)
                .orderBy("fechaInicio", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Meta::class.java)?.copy(id = doc.id)
            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener meta activa", e)
            null
        }

    }
}