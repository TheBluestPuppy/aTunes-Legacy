package com.example.atuneslegacy.data.auth

import com.example.atuneslegacy.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.StateFlow

class AuthHelper(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient = SupabaseProvider.client
) {
    val sessionStatus = supabaseClient.auth.sessionStatus

    suspend fun signUpWithEmail(emailInput: String, passwordInput: String) {
        supabaseClient.auth.signUpWith(Email) {
            email = emailInput
            password = passwordInput
        }
    }

    suspend fun signInWithEmail(emailInput: String, passwordInput: String) {
        supabaseClient.auth.signInWith(Email) {
            email = emailInput
            password = passwordInput
        }
    }

    suspend fun signInWithGoogle() {
        supabaseClient.auth.signInWith(Google)
    }

    suspend fun signInWithGoogleIdToken(idTokenInput: String, rawAccessToken: String? = null) {
        supabaseClient.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.IDToken) {
            idToken = idTokenInput
            provider = Google
            accessToken = rawAccessToken
        }
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    fun getCurrentUser(): UserInfo? {
        return supabaseClient.auth.currentUserOrNull()
    }

    fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentSessionOrNull() != null
    }
}
