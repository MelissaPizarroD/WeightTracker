package com.isoft.weighttracker.feature.antropometria.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
import kotlinx.coroutines.tasks.await

class AntropometriaRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerRegistros(): List<Antropometria> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users")
            .document(uid)
            .collection("registros_antropometricos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Antropometria::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun obtenerRegistrosDeUsuario(usuarioId: String): List<Antropometria> {
        val snapshot = db.collection("users")
            .document(usuarioId)
            .collection("registros_antropometricos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Antropometria::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun guardarRegistro(registro: Antropometria) {
        val uid = auth.currentUser?.uid ?: return

        val collectionRef = db.collection("users")
            .document(uid)
            .collection("registros_antropometricos")

        // ✅ Crear un nuevo documento con ID personalizado
        val docRef = collectionRef.document()
        val registroConId = registro.copy(id = docRef.id)

        docRef.set(registroConId).await()
    }

    suspend fun actualizarRegistro(registro: Antropometria): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val id = registro.id ?: return false

        return try {
            db.collection("users")
                .document(uid)
                .collection("registros_antropometricos")
                .document(id)
                .set(registro)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarRegistro(id: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users")
                .document(uid)
                .collection("registros_antropometricos")
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}