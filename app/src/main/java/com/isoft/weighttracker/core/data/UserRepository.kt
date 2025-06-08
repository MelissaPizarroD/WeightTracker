package com.isoft.weighttracker.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.core.model.ProfesionalProfile
import com.isoft.weighttracker.core.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createUserIfNotExists(): Boolean {
        val user = auth.currentUser ?: return false
        val uid = user.uid
        val userRef = db.collection("users").document(uid)
        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val newUser = User(
                uid = uid,
                name = user.displayName ?: "",
                email = user.email ?: "",
                role = "",
                photoUrl = user.photoUrl?.toString()
            )
            userRef.set(newUser).await()
        }

        return true
    }

    suspend fun getUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(uid).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun updateRole(role: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        db.collection("users").document(uid).update("role", role).await()
        return true
    }

    /**
     * ✅ NUEVO METODO PARA ASOCIAR PROFESIONALES
     */
    suspend fun setProfessionalForUser(tipo: String, profesionalUid: String) {
        val uid = auth.currentUser?.uid ?: return
        val fieldPath = "profesionales.$tipo"

        db.collection("users")
            .document(uid)
            .update(fieldPath, profesionalUid)
            .await()
    }

    suspend fun setProfessionalId(userId: String, professionalId: String): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .update("idProfesional", professionalId)
                .await()
            Log.d("UserRepo", "✅ ID profesional asignado: $professionalId a $userId")
            true
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Error asignando ID profesional: ${e.message}")
            false
        }
    }

    // 🔷 Persona Profile
    suspend fun getPersonaProfile(): PersonaProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid)
            .collection("personaProfile").document("info").get().await()

        return doc.toObject(PersonaProfile::class.java)
    }

    suspend fun updatePersonaProfile(profile: PersonaProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        db.collection("users").document(uid)
            .collection("personaProfile").document("info")
            .set(profile).await()
        return true
    }

    suspend fun actualizarEstadoRecordatorio(activo: Boolean): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        val map = mapOf("recordatorioActivo" to activo)

        db.collection("users")
            .document(uid)
            .collection("personaProfile")
            .document("info")
            .set(map, SetOptions.merge()) // 🔁 Solo actualiza ese campo
            .await()

        return true
    }

    // 🔷 Profesional Profile
    suspend fun getProfesionalProfile(): ProfesionalProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid)
            .collection("profesionalProfile").document("info").get().await()

        return doc.toObject(ProfesionalProfile::class.java)
    }

    suspend fun updateProfesionalProfile(profile: ProfesionalProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        db.collection("users").document(uid)
            .collection("profesionalProfile").document("info")
            .set(profile).await()
        return true
    }

    //funcion para que profesionales obtengan clientes
    suspend fun getUsuariosAsociados(tipo: String, uidProfesional: String): List<User> {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("profesionales.$tipo", uidProfesional)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error al obtener usuarios asociados ($tipo): ${e.message}", e)
            emptyList()
        }
    }

    fun signOut() {
        auth.signOut()
    }
}