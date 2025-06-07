package com.isoft.weighttracker.feature.actividadfisica.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.actividadfisica.model.ActividadFisica
import com.isoft.weighttracker.feature.actividadfisica.model.RegistroPasos
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ActividadFisicaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registrarActividad(actividad: ActividadFisica): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = db.collection("users").document(uid).collection("actividadFisica")

        return try {
            if (actividad.id != null) {
                ref.document(actividad.id).set(actividad).await()
            } else {
                val newDoc = ref.document()
                val nuevaActividad = actividad.copy(id = newDoc.id)
                newDoc.set(nuevaActividad).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerActividades(): List<ActividadFisica> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users")
            .document(uid)
            .collection("actividadFisica")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(ActividadFisica::class.java)
    }

    suspend fun actualizarActividad(actividad: ActividadFisica): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val id = actividad.id ?: return false

        return try {
            db.collection("users")
                .document(uid)
                .collection("actividadFisica")
                .document(id)
                .set(actividad)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarActividad(id: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users")
                .document(uid)
                .collection("actividadFisica")
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registrarPasosHoy(pasos: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val fechaHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val ref = db.collection("users").document(uid).collection("registroPasos")

        return try {
            // Ver si ya existe uno para hoy
            val snapshot = ref
                .whereGreaterThanOrEqualTo("fecha", fechaHoy)
                .whereLessThan("fecha", fechaHoy + 86_400_000) // +1 día
                .get()
                .await()

            val existing = snapshot.documents.firstOrNull()
            if (existing != null) {
                val id = existing.id
                ref.document(id).set(RegistroPasos(id = id, fecha = fechaHoy, pasos = pasos)).await()
            } else {
                val newDoc = ref.document()
                ref.document(newDoc.id).set(
                    RegistroPasos(
                        id = newDoc.id,
                        fecha = fechaHoy,
                        pasos = pasos
                    )
                ).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerHistorialPasos(): List<RegistroPasos> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users")
            .document(uid)
            .collection("registroPasos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(RegistroPasos::class.java)
    }
}