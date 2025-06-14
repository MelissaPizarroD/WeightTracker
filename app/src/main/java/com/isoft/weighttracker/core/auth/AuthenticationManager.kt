package com.isoft.weighttracker.core.auth

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.isoft.weighttracker.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.util.UUID

class AuthenticationManager(
    private val context: Context,
    private val activity: ComponentActivity // ✅ Agregamos referencia a la Activity
) {
    private val auth = Firebase.auth

    sealed interface AuthResponse {
        data object Success : AuthResponse
        data class Error(val message: String) : AuthResponse
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun signInWithGoogle(): Flow<AuthResponse> = callbackFlow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val credentialManager = CredentialManager.create(context)
            // ✅ CAMBIO PRINCIPAL: Usar activity en lugar de context para getCredential
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )

                    Firebase.auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                trySend(AuthResponse.Success)
                            } else {
                                trySend(AuthResponse.Error(it.exception?.message ?: "Error"))
                            }
                        }

                } catch (e: GoogleIdTokenParsingException) {
                    trySend(AuthResponse.Error(e.message ?: "Parsing error"))
                }
            }
        } catch (e: Exception) {
            trySend(AuthResponse.Error(e.message ?: "Unexpected error"))
        }

        awaitClose()
    }
}