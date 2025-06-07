package com.isoft.weighttracker.feature.asociar.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.model.User
import kotlinx.coroutines.tasks.await

class AsociarProfesionalRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun buscarProfesionalPorCodigo(codigo: String, tipo: String): User? {
        return try {
            Log.d("AsociarRepo", "🔍 Buscando profesional: código=$codigo, tipo=$tipo")

            val result = db.collection("users")
                .whereEqualTo("idProfesional", codigo.trim().uppercase())
                .whereEqualTo("role", tipo)
                .get()
                .await()

            val profesional = result.documents.firstOrNull()?.toObject(User::class.java)

            if (profesional != null) {
                Log.d("AsociarRepo", "✅ Profesional encontrado: ${profesional.name} - ${profesional.email}")
            } else {
                Log.w("AsociarRepo", "❌ No se encontró profesional con código: $codigo")
            }

            profesional
        } catch (e: Exception) {
            Log.e("AsociarRepo", "❌ Error buscando profesional: ${e.message}")
            null
        }
    }

    suspend fun asociarProfesional(userUid: String, tipo: String, professionalUid: String): Boolean {
        val campo = "profesionales.$tipo"
        return try {
            Log.d("AsociarRepo", "🔗 Asociando: usuario=$userUid, tipo=$tipo, profesional=$professionalUid")

            db.collection("users")
                .document(userUid)
                .update(campo, professionalUid)
                .await()

            Log.d("AsociarRepo", "✅ Asociación creada correctamente")
            true
        } catch (e: Exception) {
            Log.e("AsociarRepo", "❌ Error asociando profesional: ${e.message}")
            false
        }
    }
}
