package com.example.atuneslegacy.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    // Replace with actual Supabase project credentials or build config
    var supabaseUrl: String = "https://your-supabase-project.supabase.co"
    var supabaseKey: String = "your-supabase-anon-key"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
