package com.isoft.weighttracker.feature.asociar.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isoft.weighttracker.core.model.User
import kotlinx.coroutines.tasks.await

class ProfesionalAsociadoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun obtenerAsociados(): Map<String, User> {
        val uid = auth.currentUser?.uid ?: run {
            Log.w("ProfesionalRepo", "❌ No hay usuario autenticado")
            return emptyMap()
        }

        Log.d("ProfesionalRepo", "🔍 Buscando asociados para usuario: $uid")

        try {
            val userSnapshot = db.collection("users").document(uid).get().await()
            Log.d("ProfesionalRepo", "📄 Documento del usuario obtenido: ${userSnapshot.exists()}")

            if (!userSnapshot.exists()) {
                Log.w("ProfesionalRepo", "❌ Documento del usuario no existe")
                return emptyMap()
            }

            val profesionales = userSnapshot.get("profesionales") as? Map<*, *>
            Log.d("ProfesionalRepo", "👥 Campo profesionales: $profesionales")

            if (profesionales == null) {
                Log.w("ProfesionalRepo", "⚠️ Campo profesionales es null")
                return emptyMap()
            }

            if (profesionales.isEmpty()) {
                Log.w("ProfesionalRepo", "⚠️ Campo profesionales está vacío")
                return emptyMap()
            }

            val resultado = mutableMapOf<String, User>()

            for ((tipo, profesionalUid) in profesionales) {
                val tipoStr = tipo as? String ?: continue
                val uidStr = profesionalUid.toString()

                Log.d("ProfesionalRepo", "🔍 Buscando $tipoStr con UID: $uidStr")

                try {
                    val profeSnapshot = db.collection("users")
                        .document(uidStr)
                        .get()
                        .await()

                    Log.d("ProfesionalRepo", "📄 Documento profesional existe: ${profeSnapshot.exists()}")

                    if (!profeSnapshot.exists()) {
                        Log.w("ProfesionalRepo", "❌ No se encontró profesional con UID: $uidStr")
                        continue
                    }

                    val profe = profeSnapshot.toObject(User::class.java)
                    Log.d("ProfesionalRepo", "👤 Profesional deserializado: ${profe?.name} - ${profe?.email}")

                    if (profe != null) {
                        resultado[tipoStr] = profe
                        Log.d("ProfesionalRepo", "✅ Agregado $tipoStr: ${profe.name}")
                    } else {
                        Log.w("ProfesionalRepo", "❌ Error deserializando profesional")
                    }

                } catch (e: Exception) {
                    Log.e("ProfesionalRepo", "❌ Error obteniendo profesional $tipoStr: ${e.message}", e)
                }
            }

            Log.d("ProfesionalRepo", "📊 Total de profesionales encontrados: ${resultado.size}")
            return resultado

        } catch (e: Exception) {
            Log.e("ProfesionalRepo", "❌ Error general en obtenerAsociados: ${e.message}", e)
            return emptyMap()
        }
    }

    suspend fun eliminarAsociacion(tipo: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val campo = "profesionales.$tipo"

        Log.d("ProfesionalRepo", "🗑️ Eliminando campo: $campo del usuario: $uid")

        return try {
            db.collection("users").document(uid).update(campo, null).await()
            Log.d("ProfesionalRepo", "✅ Asociación eliminada correctamente")
            true
        } catch (e: Exception) {
            Log.e("ProfesionalRepo", "❌ Error eliminando asociación: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}