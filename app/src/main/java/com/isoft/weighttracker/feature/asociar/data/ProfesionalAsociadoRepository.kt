package com.isoft.weighttracker.feature.asociar.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.data.UserRepository
import com.isoft.weighttracker.core.model.User
import com.isoft.weighttracker.core.model.ProfesionalProfile
import kotlinx.coroutines.tasks.await

// ✅ NUEVO DATA CLASS para combinar User + ProfesionalProfile
data class ProfesionalCompleto(
    val user: User,
    val profesionalProfile: ProfesionalProfile?
)

class ProfesionalAsociadoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userRepo = UserRepository()

    // ✅ ACTUALIZADO - Ahora retorna información completa
    suspend fun obtenerAsociadosCompletos(): Map<String, ProfesionalCompleto> {
        val uid = auth.currentUser?.uid ?: run {
            Log.w("ProfesionalRepo", "❌ No hay usuario autenticado")
            return emptyMap()
        }

        Log.d("ProfesionalRepo", "🔍 Buscando asociados completos para usuario: $uid")

        try {
            // Obtener PersonaProfile en vez de User
            val personaProfileSnapshot = db.collection("users")
                .document(uid)
                .collection("personaProfile")
                .document("info")
                .get()
                .await()

            Log.d("ProfesionalRepo", "📄 PersonaProfile obtenido: ${personaProfileSnapshot.exists()}")

            if (!personaProfileSnapshot.exists()) {
                Log.w("ProfesionalRepo", "❌ PersonaProfile no existe")
                return emptyMap()
            }

            val profesionales = personaProfileSnapshot.get("profesionales") as? Map<*, *>
            Log.d("ProfesionalRepo", "👥 Campo profesionales: $profesionales")

            if (profesionales == null || profesionales.isEmpty()) {
                Log.w("ProfesionalRepo", "⚠️ Campo profesionales vacío o null")
                return emptyMap()
            }

            val resultado = mutableMapOf<String, ProfesionalCompleto>()

            for ((tipo, profesionalUid) in profesionales) {
                val tipoStr = tipo as? String ?: continue
                val uidStr = profesionalUid.toString()

                Log.d("ProfesionalRepo", "🔍 Buscando $tipoStr con UID: $uidStr")

                try {
                    // Obtener User
                    val profeSnapshot = db.collection("users")
                        .document(uidStr)
                        .get()
                        .await()

                    if (!profeSnapshot.exists()) {
                        Log.w("ProfesionalRepo", "❌ No se encontró profesional con UID: $uidStr")
                        continue
                    }

                    val profe = profeSnapshot.toObject(User::class.java)
                    if (profe == null) {
                        Log.w("ProfesionalRepo", "❌ Error deserializando User")
                        continue
                    }

                    // ✅ NUEVO - Obtener ProfesionalProfile
                    val profesionalProfile = userRepo.getProfesionalProfileByUserId(uidStr)
                    Log.d("ProfesionalRepo", "📋 ProfesionalProfile obtenido: ${profesionalProfile?.especialidad}")

                    // Crear objeto completo
                    val profesionalCompleto = ProfesionalCompleto(
                        user = profe,
                        profesionalProfile = profesionalProfile
                    )

                    resultado[tipoStr] = profesionalCompleto
                    Log.d("ProfesionalRepo", "✅ Agregado $tipoStr: ${profe.name}")

                } catch (e: Exception) {
                    Log.e("ProfesionalRepo", "❌ Error obteniendo profesional $tipoStr: ${e.message}", e)
                }
            }

            Log.d("ProfesionalRepo", "📊 Total de profesionales encontrados: ${resultado.size}")
            return resultado

        } catch (e: Exception) {
            Log.e("ProfesionalRepo", "❌ Error general en obtenerAsociadosCompletos: ${e.message}", e)
            return emptyMap()
        }
    }

    // ✅ MANTENER MÉTODO ORIGINAL para compatibilidad
    suspend fun obtenerAsociados(): Map<String, User> {
        return obtenerAsociadosCompletos().mapValues { it.value.user }
    }

    // ✅ ACTUALIZADO - Elimina desde PersonaProfile
    suspend fun eliminarAsociacion(tipo: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val campo = "profesionales.$tipo"

        Log.d("ProfesionalRepo", "🗑️ Eliminando campo: $campo del usuario: $uid")

        return try {
            db.collection("users")
                .document(uid)
                .collection("personaProfile")
                .document("info")
                .update(campo, null)
                .await()

            Log.d("ProfesionalRepo", "✅ Asociación eliminada correctamente")
            true
        } catch (e: Exception) {
            Log.e("ProfesionalRepo", "❌ Error eliminando asociación: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}