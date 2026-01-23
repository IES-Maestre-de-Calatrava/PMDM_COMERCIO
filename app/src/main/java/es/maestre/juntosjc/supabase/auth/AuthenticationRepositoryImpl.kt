package es.maestre.juntosjc.supabase.auth

import android.util.Log
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

object AuthenticationRepositoryImpl : AuthenticationRepository {
    // Clase para el inicio de sesion, registtro y eso
    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.e("Auth", "Error signIn: ${e.message}")
            false
        }
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        return try {
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.e("Auth", "Error signUp:  ${e.message}")
            false
        }
    }

    override suspend fun signOut(): Boolean {
        return try {
            SupabaseClient.client.auth.signOut()
            true
        } catch (e: Exception) {
            Log.e("Auth", "Error signOut: ${e.message}")
            false
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentUserOrNull() != null
    }

    override fun getCurrentUserEmail(): String? {
        return SupabaseClient.client.auth.currentUserOrNull()?.email
    }
}