package com.isoft.weighttracker.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.isoft.weighttracker.core.model.PersonaProfile
import com.isoft.weighttracker.core.model.ProfesionalProfile
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.feature.antropometria.model.Antropometria
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

    // ✅ ACTUALIZADO - Ahora actualiza PersonaProfile en vez de User
    suspend fun setProfessionalForUser(tipo: String, profesionalUid: String) {
        val uid = auth.currentUser?.uid ?: return
        val fieldPath = "profesionales.$tipo"

        db.collection("users")
            .document(uid)
            .collection("personaProfile")
            .document("info")
            .update(fieldPath, profesionalUid)
            .await()
    }

    // ✅ ACTUALIZADO - Ahora actualiza ProfesionalProfile en vez de User
    suspend fun setProfessionalId(userId: String, professionalId: String): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .collection("profesionalProfile")
                .document("info")
                .update("idProfesional", professionalId)
                .await()
            Log.d("UserRepo", "✅ ID profesional asignado: $professionalId a $userId")
            true
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Error asignando ID profesional: ${e.message}")
            false
        }
    }

    // ✅ NUEVO - Generar y asignar ID profesional automáticamente
    suspend fun generateAndSetProfessionalId(userId: String): String {
        val nuevoId = generateUniqueCode()
        setProfessionalId(userId, nuevoId)
        return nuevoId
    }

    // ✅ NUEVO - Generar código único
    private suspend fun generateUniqueCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            val code = (1..6).map { chars.random() }.joinToString("")

            // Verificar que el código no exista en ProfesionalProfile
            val existing = db.collectionGroup("profesionalProfile")
                .whereEqualTo("idProfesional", code)
                .get()
                .await()

            if (existing.isEmpty) {
                Log.d("UserRepo", "🆔 Código único generado: $code")
                return code
            }

            attempts++
            Log.w("UserRepo", "⚠️ Código $code ya existe, intento $attempts/$maxAttempts")
        }

        throw Exception("No se pudo generar código único después de $maxAttempts intentos")
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
        return try {
            val uid = auth.currentUser?.uid ?: return false

            // ✅ Si no tiene ID, generarlo automáticamente
            val finalProfile = if (profile.idProfesional.isNullOrBlank()) {
                val nuevoId = generateUniqueCode()
                Log.d("UserRepo", "✅ Generando nuevo ID profesional: $nuevoId")
                profile.copy(idProfesional = nuevoId)
            } else {
                profile
            }

            db.collection("users").document(uid)
                .collection("profesionalProfile").document("info")
                .set(finalProfile).await()

            Log.d("UserRepo", "✅ Perfil profesional actualizado correctamente")
            true
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Error actualizando perfil profesional: ${e.message}", e)
            false
        }
    }

    suspend fun getProfesionalProfileByUserId(userId: String): ProfesionalProfile? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("profesionalProfile").document("info").get().await()

            doc.toObject(ProfesionalProfile::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error obteniendo ProfesionalProfile del usuario $userId", e)
            null
        }
    }

    // ✅ ACTUALIZADO - Busca en PersonaProfile en vez de User
    suspend fun getUsuariosAsociados(tipo: String, uidProfesional: String): List<User> {
        return try {
            // Buscar en PersonaProfile collection donde el profesional esté asociado
            val querySnapshot = db.collectionGroup("personaProfile")
                .whereEqualTo("profesionales.$tipo", uidProfesional)
                .get()
                .await()

            val usuarios = mutableListOf<User>()

            for (document in querySnapshot.documents) {
                // Obtener el userId desde el path del documento
                val userId = document.reference.parent.parent?.id
                if (userId != null) {
                    // Obtener el User correspondiente
                    val userDoc = db.collection("users").document(userId).get().await()
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        usuarios.add(user)
                    }
                }
            }

            usuarios
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error al obtener usuarios asociados ($tipo): ${e.message}", e)
            emptyList()
        }
    }

    // Metodo para obtener PersonaProfile de cualquier usuario (para profesionales)
    suspend fun getPersonaProfileByUserId(userId: String): PersonaProfile? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("personaProfile").document("info").get().await()

            doc.toObject(PersonaProfile::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error obteniendo PersonaProfile del usuario $userId", e)
            null
        }
    }

    // Metodo para obtener antropometría más reciente de cualquier usuario
    suspend fun getAntropometriaRecienteByUserId(userId: String): Antropometria? {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("antropometria")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Antropometria::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error obteniendo antropometría del usuario $userId", e)
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
}