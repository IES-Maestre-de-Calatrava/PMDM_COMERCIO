package es.maestre.juntosjc.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://lxmkwegowscwhgrfsqcw.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_dp1PvfDxB0PuMv_hL2mKlA_XO5Yb0d1"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
