package com.isoft.weighttracker.feature.comidas.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.comidas.model.Comida
import kotlinx.coroutines.tasks.await

class ComidasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registrarComida(comida: Comida): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = db.collection("users").document(uid).collection("comidas")

        return try {
            if (comida.id != null) {
                // ✅ Editar: usar el mismo ID
                ref.document(comida.id).set(comida.copy(fecha = System.currentTimeMillis())).await()
            } else {
                // ✅ Crear: generamos ID manualmente y usamos .document(id)
                val newDoc = ref.document()
                val nuevaComidaConId = comida.copy(id = newDoc.id)
                newDoc.set(nuevaComidaConId).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerComidas(): List<Comida> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(uid)
            .collection("comidas")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.toObjects(Comida::class.java)
    }

    suspend fun actualizarComida(comida: Comida): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val id = comida.id ?: return false

        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("comidas")
                .document(id)
                .set(comida)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarComida(comidaId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users")
                .document(uid)
                .collection("comidas")
                .document(comidaId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}