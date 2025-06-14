package com.isoft.weighttracker.feature.asociar.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.model.User
import kotlinx.coroutines.tasks.await

class AsociarProfesionalRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ ACTUALIZADO - Busca idProfesional en ProfesionalProfile
    suspend fun buscarProfesionalPorCodigo(codigo: String, tipo: String): User? {
        return try {
            Log.d("AsociarRepo", "🔍 Buscando profesional: código=$codigo, tipo=$tipo")

            // Primero buscar en ProfesionalProfile por idProfesional
            val profesionalProfileSnapshot = db.collectionGroup("profesionalProfile")
                .whereEqualTo("idProfesional", codigo.trim().uppercase())
                .get()
                .await()

            if (profesionalProfileSnapshot.isEmpty) {
                Log.w("AsociarRepo", "❌ No se encontró profesional con código: $codigo")
                return null
            }

            // Obtener el userId desde el path del documento
            val profesionalProfileDoc = profesionalProfileSnapshot.documents.first()
            val userId = profesionalProfileDoc.reference.parent.parent?.id

            if (userId == null) {
                Log.e("AsociarRepo", "❌ No se pudo obtener userId del profesional")
                return null
            }

            // Verificar que el User tenga el role correcto
            val userDoc = db.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            if (user != null && user.role == tipo) {
                Log.d("AsociarRepo", "✅ Profesional encontrado: ${user.name} - ${user.email}")
                return user
            } else {
                Log.w("AsociarRepo", "❌ Profesional encontrado pero role incorrecto. Esperado: $tipo, Actual: ${user?.role}")
                return null
            }

        } catch (e: Exception) {
            Log.e("AsociarRepo", "❌ Error buscando profesional: ${e.message}")
            null
        }
    }

    // ✅ ACTUALIZADO - Ahora actualiza PersonaProfile
    suspend fun asociarProfesional(userUid: String, tipo: String, professionalUid: String): Boolean {
        val campo = "profesionales.$tipo"
        return try {
            Log.d("AsociarRepo", "🔗 Asociando: usuario=$userUid, tipo=$tipo, profesional=$professionalUid")

            db.collection("users")
                .document(userUid)
                .collection("personaProfile")
                .document("info")
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